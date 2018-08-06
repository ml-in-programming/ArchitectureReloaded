package org.jetbrains.research.groups.ml_methods.evaluation;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.Refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectEvaluationResult extends AbstractEvaluationResult {
    private final List<Refactoring> foundGoodRefactorings;
    private final List<Refactoring> goodRefactorings;
    private final List<Refactoring> foundBadRefactorings;
    private final List<Refactoring> badRefactorings;
    private final @NotNull List<Refactoring> foundOthersRefactorings;
    private final @NotNull List<Double> errors = new ArrayList<>();

    ProjectEvaluationResult(List<Refactoring> foundRefactorings,
                            List<Refactoring> goodRefactorings,
                            List<Refactoring> badRefactorings,
                            @NotNull Algorithm evaluatingAlgorithm) {
        super(evaluatingAlgorithm);
        foundGoodRefactorings = new ArrayList<>(foundRefactorings);
        foundBadRefactorings = new ArrayList<>(foundRefactorings);
        foundGoodRefactorings.removeIf(refactoring -> !goodRefactorings.contains(refactoring));
        foundBadRefactorings.removeIf(refactoring -> !badRefactorings.contains(refactoring));
        this.goodRefactorings = new ArrayList<>(goodRefactorings);
        this.badRefactorings = new ArrayList<>(badRefactorings);
        foundOthersRefactorings = new ArrayList<>(foundRefactorings);
        foundOthersRefactorings.removeIf(
            refactoring -> goodRefactorings.contains(refactoring) || badRefactorings.contains(refactoring)
        );
        errors.addAll(foundBadRefactorings.stream()
                .map(value -> value.getAccuracy() - 0)
                .collect(Collectors.toList()));
        errors.addAll(foundGoodRefactorings.stream()
                .map(value -> 1 - value.getAccuracy())
                .collect(Collectors.toList()));
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

    @NotNull
    @Override
    public List<Double> getErrors() {
        return errors;
    }
}
