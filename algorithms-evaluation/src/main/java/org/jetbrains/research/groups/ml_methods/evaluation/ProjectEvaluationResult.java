package org.jetbrains.research.groups.ml_methods.evaluation;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.Refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.pow;

public class ProjectEvaluationResult extends AbstractEvaluationResult {
    private final List<Refactoring> foundGoodRefactorings;
    private final List<Refactoring> goodRefactorings;
    private final List<Refactoring> foundBadRefactorings;
    private final List<Refactoring> badRefactorings;
    private final @NotNull List<Refactoring> foundOthersRefactorings;
    private final @NotNull List<Double> errorSquares = new ArrayList<>();

    public ProjectEvaluationResult(List<Refactoring> foundRefactorings,
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
        errorSquares.addAll(foundBadRefactorings.stream()
                .map(value -> {
                    double accuracy = value.getAccuracy();
                    return pow(accuracy - 0, 2);
                })
                .collect(Collectors.toList()));
        errorSquares.addAll(foundGoodRefactorings.stream()
                .map(value -> {
                    double accuracy = value.getAccuracy();
                    return pow(1 - accuracy, 2);
                })
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
    public List<Double> getErrorSquares() {
        return errorSquares;
    }
}
