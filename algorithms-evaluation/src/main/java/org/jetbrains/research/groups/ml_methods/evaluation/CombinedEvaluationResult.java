package org.jetbrains.research.groups.ml_methods.evaluation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;

import java.util.ArrayList;
import java.util.List;

public class CombinedEvaluationResult extends AbstractEvaluationResult {
    private int numberOfGood = 0;
    private int numberOfBad = 0;
    private int numberOfFoundGood = 0;
    private int numberOfFoundBad = 0;
    private int numberOfFoundOthers = 0;
    private final List<Double> errors = new ArrayList<>();

    CombinedEvaluationResult(@NotNull Algorithm evaluatingAlgorithm) {
        super(evaluatingAlgorithm);
    }

    void addResult(EvaluationResult evaluationResult) {
        if (!getAlgorithm().equals(evaluationResult.getAlgorithm())) {
            throw new IllegalArgumentException("Tried to add evaluation result with different algorithm");
        }
        numberOfGood += evaluationResult.getNumberOfGood();
        numberOfBad += evaluationResult.getNumberOfBad();
        numberOfFoundGood += evaluationResult.getNumberOfFoundGood();
        numberOfFoundBad += evaluationResult.getNumberOfFoundBad();
        numberOfFoundOthers += evaluationResult.getNumberOfFoundOthers();
        errors.addAll(evaluationResult.getErrors());
    }

    @Override
    public int getNumberOfGood() {
        return numberOfGood;
    }

    @Override
    public int getNumberOfBad() {
        return numberOfBad;
    }

    @Override
    public int getNumberOfFoundGood() {
        return numberOfFoundGood;
    }

    @Override
    public int getNumberOfFoundBad() {
        return numberOfFoundBad;
    }

    @Override
    public int getNumberOfFoundOthers() {
        return numberOfFoundOthers;
    }

    @Override
    public List<Double> getErrors() {
        return errors;
    }
}
