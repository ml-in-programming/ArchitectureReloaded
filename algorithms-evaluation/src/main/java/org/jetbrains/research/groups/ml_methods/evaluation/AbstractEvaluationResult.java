package org.jetbrains.research.groups.ml_methods.evaluation;

public abstract class AbstractEvaluationResult implements EvaluationResult {
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
