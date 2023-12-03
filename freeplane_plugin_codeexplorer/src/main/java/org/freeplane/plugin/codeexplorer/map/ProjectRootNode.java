/*
 * Created on 1 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import static java.util.Arrays.asList;

import java.io.File;
import java.net.URI;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.codeexplorer.graph.GraphNodeSort;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.domain.Source;
import com.tngtech.archunit.core.domain.properties.HasName;

class ProjectRootNode extends CodeNode implements SubprojectFinder{
    static final String UI_ICON_NAME = "code_project";
    private static final Set<String> ignoredNames = new HashSet<>(asList("classes","target", "bin", "main"));
    private static final Entry<Integer, String> UNKNOWN = new AbstractMap.SimpleEntry<>(-1, ":unknown:");
    private final JavaPackage rootPackage;
    private final Map<String, Map.Entry<Integer, String>> subprojectsByLocation;
    private final Set<String> badLocations;
    private JavaClasses classes;
    static ProjectRootNode asMapRoot(CodeMap map, JavaClasses classes, CodeExplorerConfiguration configuration) {
        ProjectRootNode projectRootNode = new ProjectRootNode(map, classes, configuration);
        map.setRoot(projectRootNode);
        if(projectRootNode.getChildCount() > 20)
            projectRootNode.getChildren()
                .forEach(node -> ((CodeNode)node).memoizeCodeDependencies());
        return projectRootNode;
    }

    private ProjectRootNode(CodeMap map, JavaClasses classes, CodeExplorerConfiguration configuration) {
        super(map, 0);
        this.classes = classes;
        this.rootPackage = classes.getDefaultPackage();
        setID("projectRoot");
        setText(configuration.getProjectName());

        subprojectsByLocation = new LinkedHashMap<>(configuration.getLocations().size());
        configuration.getLocations().stream()
                .forEach(file -> subprojectsByLocation.put(toSourceLocationPath(file),
                        new AbstractMap.SimpleEntry<>(subprojectsByLocation.size(), toSubprojectName(file))));
        badLocations = new HashSet<>();
        map.setSubprojectFinder(this);
        initializeChildNodes();
    }

    private static String toSubprojectName(File file) {
        String name = file.getName();
        if(file.isDirectory()) {
            if(ignoredNames.contains(name)) {
                File parentFile = file.getParentFile();
                if(parentFile != null) {
                    return toSubprojectName(parentFile);
                }
            }
        }
        return name;
    }
    private static String toSourceLocationPath(File file) {
        String path = file.toURI().getRawPath();
        if (file.isDirectory()) {
            return new File(file, "target/classes").isDirectory()
                    ? path + "target/classes/" : path;
        } else
            return "file:" + path + "!/";
    }

    private void initializeChildNodes() {
        List<NodeModel> children = super.getChildrenInternal();
        List<PackageNode> nodes = subprojectsByLocation.values().stream()
                .parallel()
                .map(e ->
        new PackageNode(rootPackage, getMap(), e.getValue(), e.getKey().intValue()))
                .collect(Collectors.toList());
        GraphNodeSort<Integer> childNodes = new GraphNodeSort<>();
        nodes.forEach(node -> {
            childNodes.addNode(node.subprojectIndex);
            DistinctTargetDependencyFilter filter = new DistinctTargetDependencyFilter();
            Map<Integer, Long> referencedSubprojects = node.getOutgoingDependenciesWithKnownTargets()
                    .map(filter::knownDependency)
                    .map(Dependency::getTargetClass)
                    .collect(Collectors.groupingBy(this::subprojectIndexOf, Collectors.counting()));
            referencedSubprojects.entrySet()
            .forEach(e -> childNodes.addEdge(node.subprojectIndex, e.getKey(), e.getValue()));
        });
        Comparator<Set<Integer>> comparingByReversedClassCount = Comparator.comparing(
                indices -> -indices.stream()
                    .map(nodes::get)
                    .mapToLong(PackageNode::getClassCount)
                    .sum()
                );
        List<List<Integer>> orderedPackages = childNodes.sortNodes(comparingByReversedClassCount
                .thenComparing(SubgroupComparator.comparingByName(i -> nodes.get(i).getText())));
        for(int subgroupIndex = 0; subgroupIndex < orderedPackages.size(); subgroupIndex++) {
            for (Integer subprojectIndex : orderedPackages.get(subgroupIndex)) {
                final CodeNode node = nodes.get(subprojectIndex);
                children.add(node);
                node.setParent(this);
            }
        }
    }

    @Override
    HasName getCodeElement() {
        return () -> "root";
    }

    @Override
    Stream<Dependency> getOutgoingDependencies() {
        return Stream.empty();

    }

    @Override
    Stream<Dependency> getIncomingDependencies() {
        return Stream.empty();
    }

    @Override
    String getUIIconName() {
        return UI_ICON_NAME;
    }

    @Override
    public int subprojectIndexOf(JavaClass javaClass) {
        Optional<Source> source = javaClass.getSource();
        Entry<Integer, String> subprojectEntry;
        if(! source.isPresent())
            subprojectEntry = UNKNOWN;
        else {
            URI uri = source.get().getUri();
            String path = uri.getRawPath();
            String classLocation = path != null ?  path : uri.getSchemeSpecificPart();
            String classSourceLocation = classLocation.substring(0, classLocation.length() - javaClass.getName().length() - ".class".length());
            subprojectEntry = subprojectsByLocation.getOrDefault(classSourceLocation, UNKNOWN);
            if(subprojectEntry == UNKNOWN && badLocations.add(classLocation))
                LogUtils.info("Unknown class source location " + uri);
        }
        return subprojectEntry.getKey().intValue();
    }

    @Override
    public Stream<JavaClass> allClasses() {
        return classes.stream();
    }


}
