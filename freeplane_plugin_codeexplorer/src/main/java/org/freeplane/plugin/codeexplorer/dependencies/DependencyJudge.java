/*
 * Created on 28 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.dependencies;
import java.util.List;
import java.util.regex.Pattern;

import com.tngtech.archunit.core.domain.Dependency;

/**
 * DependencyMatcher is used to parse and apply rules for dependencies between Java classes.
 * It utilizes a domain-specific language (DSL) to define allowed, forbidden, or ignored dependencies.
 *
 * DSL Format:
 * -> Rules are defined in the format: [type] [originPattern] [direction] [targetPattern]
 * -> Types: allow, forbid, ignore
 * -> Direction: ->^v, ->v, ->^ (representing bidirectional, downward, upward respectively)
 * -> Patterns: follow AspectJ->like syntax for matching package names
 *
 * Example DSL:
 *   allow *.service.* ->^v *.repository.*
 *   forbid *.*.controller*.. ->^ ..model..
 *   ignore ..util.. ->v ..*Helper..
 */
public class DependencyJudge {
    private static final String CLASS_PATTERN = "[\\w\\.\\|\\(\\)\\*\\[\\]]+";

    private static final String DIRECTION_PATTERN = Pattern.quote(DependencyDirection.UP.notation)
            + "|" + Pattern.quote(DependencyDirection.DOWN.notation)
            + "|" + Pattern.quote(DependencyDirection.ANY.notation);

    final static Pattern DEPENDENCY_RULE_PATTERN = Pattern.compile("("
            + DependencyVerdict.ALLOWED.keyword + "|"
            + DependencyVerdict.FORBIDDEN.keyword + "|"
            + DependencyVerdict.IGNORED.keyword + ")\\s+"
            + "(" + CLASS_PATTERN + ")\\s*"
            + "(" + DIRECTION_PATTERN + ")"
            + "\\s*("+ CLASS_PATTERN + ")\\s*$");

    /**
     * Creates an instance of DependencyMatcher based on the provided DSL string.
     *
     * @param dependencyDSL the DSL string defining the rules
     * @return DependencyMatcher instance
     */
    public static DependencyJudge of(String dependencyDSL) {
        List<DependencyRule> rules = DependencyRuleParser.parseDSL(dependencyDSL);
        return new DependencyJudge(rules);
    }

    private List<DependencyRule> rules;

    private DependencyJudge(List<DependencyRule> rules) {
        this.rules = rules;
    }

    /**
     * Evaluates the type of dependency between the origin and target classes in the given direction.
     *
     * @param originClass the fully qualified name of the origin class
     * @param targetClass the fully qualified name of the target class
     * @param direction the direction of the dependency (UP or DOWN)
     * @return DependencyRuleType (ALLOW, FORBID, IGNORE)
     */
    public DependencyVerdict judge(Dependency dependency, boolean goesUp) {
        for (DependencyRule rule : rules) {
            if (rule.matches(dependency, goesUp)) {
                return rule.type;
            }
        }
        return goesUp ? DependencyVerdict.FORBIDDEN : DependencyVerdict.ALLOWED;
    }
}
