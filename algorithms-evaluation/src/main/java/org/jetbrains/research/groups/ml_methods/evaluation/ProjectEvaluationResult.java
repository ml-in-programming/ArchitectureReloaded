package org.jetbrains.research.groups.ml_methods.evaluation;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.CalculatedRefactoring;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.Refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectEvaluationResult extends AbstractEvaluationResult {
    private final List<CalculatedRefactoring> foundGoodRefactorings;
    private final List<Refactoring> goodRefactorings;
    private final List<CalculatedRefactoring> foundBadRefactorings;
    private final List<Refactoring> badRefactorings;
    private final @NotNull List<CalculatedRefactoring> foundOthersRefactorings;
    private final @NotNull List<Double> errors = new ArrayList<>();

    ProjectEvaluationResult(List<CalculatedRefactoring> foundRefactorings,
                            List<Refactoring> goodRefactorings,
                            List<Refactoring> badRefactorings,
                            @NotNull Algorithm evaluatingAlgorithm) {
        super(evaluatingAlgorithm);
        foundGoodRefactorings = new ArrayList<>(foundRefactorings);
        foundBadRefactorings = new ArrayList<>(foundRefactorings);
        foundGoodRefactorings.removeIf(refactoring -> !goodRefactorings.contains(refactoring.getRefactoring()));
        foundBadRefactorings.removeIf(refactoring -> !badRefactorings.contains(refactoring.getRefactoring()));
        this.goodRefactorings = new ArrayList<>(goodRefactorings);
        this.badRefactorings = new ArrayList<>(badRefactorings);
        foundOthersRefactorings = new ArrayList<>(foundRefactorings);
        foundOthersRefactorings.removeIf(
            refactoring -> goodRefactorings.contains(refactoring.getRefactoring()) ||
                    badRefactorings.contains(refactoring.getRefactoring())
        );
        List<Refactoring> foundWithoutAccuracy = foundRefactorings.stream()
                .map(CalculatedRefactoring::getRefactoring)
                .collect(Collectors.toList());
        errors.addAll(foundBadRefactorings.stream()
                .map(value -> value.getAccuracy() - 0)
                .collect(Collectors.toList()));
        errors.addAll(badRefactorings.stream()
                .filter(refactoring -> !foundWithoutAccuracy.contains(refactoring))
                .map(value -> 0.0 - 0)
                .collect(Collectors.toList()));
        errors.addAll(foundGoodRefactorings.stream()
                .map(value -> 1 - value.getAccuracy())
                .collect(Collectors.toList()));
        errors.addAll(goodRefactorings.stream()
                .filter(refactoring -> !foundWithoutAccuracy.contains(refactoring))
                .map(value -> 1 - 0.0)
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
