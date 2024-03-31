/*
 * Created on 9 Mar 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import static com.tngtech.archunit.core.domain.PackageMatcher.TO_GROUPS;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.tngtech.archunit.core.domain.PackageMatcher;

class ClassNameMatcher {
    private final PackageMatcher packageMatcher;
    private final Optional<String> name;
    private final boolean ignoresClasses;
    private final String pattern;
    ClassNameMatcher(String pattern, boolean ignores, Optional<String> name) {
        super();
        this.pattern = pattern;
        this.packageMatcher = PackageMatcher.of(pattern);
        this.name = name;
        this.ignoresClasses = ignores;
    }

    boolean isIgnored(String qualifiedClassName) {
        return ignoresClasses && packageMatcher.matches(qualifiedClassName);
    }

    Optional<String> toGroup(String qualifiedClassName) {
        final Optional<String> joinedNameParts = packageMatcher.match(qualifiedClassName).map(TO_GROUPS)
                .map(List::stream)
                .map(s -> s.collect(Collectors.joining(":")));
        return joinedNameParts
                .map(parts -> name.map(s -> s + " " + parts)
                .orElse(parts));
    }



    public boolean ignoresClasses() {
        return ignoresClasses;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ignoresClasses, name, pattern);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClassNameMatcher other = (ClassNameMatcher) obj;
        return ignoresClasses == other.ignoresClasses && Objects.equals(name, other.name) && Objects.equals(
                pattern, other.pattern);
    }


}
