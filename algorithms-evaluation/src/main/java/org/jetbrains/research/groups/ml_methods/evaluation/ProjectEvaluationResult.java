package org.jetbrains.research.groups.ml_methods.evaluation;


import org.jetbrains.research.groups.ml_methods.extraction.refactoring.Refactoring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectEvaluationResult implements EvaluationResult {
    private final List<Refactoring> foundGoodRefactorings;
    private final List<Refactoring> goodRefactorings;
    private final List<Refactoring> foundBadRefactorings;
    private final List<Refactoring> badRefactorings;

    public ProjectEvaluationResult(List<Refactoring> foundRefactorings,
                                   List<Refactoring> goodRefactorings,
                                   List<Refactoring> badRefactorings) {
        Set<Refactoring> foundRefactoringsSet = new HashSet<>(foundRefactorings);
        foundGoodRefactorings = new ArrayList<>(goodRefactorings);
        foundBadRefactorings = new ArrayList<>(badRefactorings);
        foundGoodRefactorings.removeIf(refactoring -> !foundRefactoringsSet.contains(refactoring));
        foundBadRefactorings.removeIf(refactoring -> !foundRefactoringsSet.contains(refactoring));
        this.goodRefactorings = new ArrayList<>(goodRefactorings);
        this.badRefactorings = new ArrayList<>(badRefactorings);
    }
}
