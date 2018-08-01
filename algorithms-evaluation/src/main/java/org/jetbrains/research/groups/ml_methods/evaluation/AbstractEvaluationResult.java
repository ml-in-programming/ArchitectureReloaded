package org.jetbrains.research.groups.ml_methods.evaluation;

public abstract class AbstractEvaluationResult implements EvaluationResult {
    private final int numberOfFoundBadAndGood = getNumberOfFoundBad() + getNumberOfFoundGood();
    private final int numberOfBadAndGood = getNumberOfBad() + getNumberOfGood();

    @Override
    public double getGoodPrecision() {
        return getNumberOfFoundGood() / numberOfFoundBadAndGood;
    }

    @Override
    public double getGoodRecall() {
        return getNumberOfFoundGood() / getNumberOfGood();
    }

    @Override
    public double getBadPrecision() {
        return (getNumberOfBad() - getNumberOfFoundBad()) / (numberOfBadAndGood - numberOfFoundBadAndGood);
    }

    @Override
    public double getBadRecall() {
        return (getNumberOfBad() - getNumberOfFoundBad()) / getNumberOfBad();
    }
}
