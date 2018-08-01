package org.jetbrains.research.groups.ml_methods.evaluation;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.Refactoring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectEvaluationResult extends AbstractEvaluationResult {
    private final List<Refactoring> foundGoodRefactorings;
    private final List<Refactoring> goodRefactorings;
    private final List<Refactoring> foundBadRefactorings;
    private final List<Refactoring> badRefactorings;
    private final @NotNull List<Refactoring> foundOthersRefactorings;

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

        foundOthersRefactorings = new ArrayList<>(foundRefactorings);
        foundOthersRefactorings.removeIf(
            refactoring -> goodRefactorings.contains(refactoring) || badRefactorings.contains(refactoring)
        );
    }

    @Override
    public int getNumberOfFoundGood() {
        return foundGoodRefactorings.size();
    }

    @Override
    public int getNumberOfFoundBad() {
        return foundBadRefactorings.size();
    }

    @Override
    public int getNumberOfGood() {
        return goodRefactorings.size();
    }

    @Override
    public int getNumberOfBad() {
        return badRefactorings.size();
    }

    @Override
    public int getNumberOfFoundOthers() {
        return foundOthersRefactorings.size();
    }
}
