package org.jetbrains.research.groups.ml_methods.evaluation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;

public abstract class AbstractEvaluationResult implements EvaluationResult {
    private final @NotNull Algorithm evaluatingAlgorithm;

    protected AbstractEvaluationResult(@NotNull Algorithm evaluatingAlgorithm) {
        this.evaluatingAlgorithm = evaluatingAlgorithm;
    }

    @Override
    public Algorithm getAlgorithm() {
        return evaluatingAlgorithm;
    }

    @Override
    public double getGoodPrecision() {
        return (double) getNumberOfFoundGood() / getNumberOfFoundBadAndGood();
    }

    @Override
    public double getGoodRecall() {
        return (double) getNumberOfFoundGood() / getNumberOfGood();
    }

    @Override
    public double getBadPrecision() {
        return (double) (getNumberOfBad() - getNumberOfFoundBad()) / (getNumberOfBadAndGood() - getNumberOfFoundBadAndGood());
    }

    @Override
    public double getBadRecall() {
        return (double) (getNumberOfBad() - getNumberOfFoundBad()) / getNumberOfBad();
    }

    @Override
    public double getMSE() {
        return getErrorSquares().stream().mapToDouble(Double::doubleValue).sum() / getErrorSquares().size();
    }

    private int getNumberOfBadAndGood() {
        return getNumberOfBad() + getNumberOfGood();
    }

    private int getNumberOfFoundBadAndGood() {
        return getNumberOfFoundBad() + getNumberOfFoundGood();
    }
}
