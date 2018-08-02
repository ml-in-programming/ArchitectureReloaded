package org.jetbrains.research.groups.ml_methods.evaluation;

public class CombinedEvaluationResult extends AbstractEvaluationResult {
    private int numberOfGood = 0;
    private int numberOfBad = 0;
    private int numberOfFoundGood = 0;
    private int numberOfFoundBad = 0;
    private int numberOfFoundOthers = 0;

    public void addResult(EvaluationResult evaluationResult) {
        numberOfGood += evaluationResult.getNumberOfGood();
        numberOfBad += evaluationResult.getNumberOfBad();
        numberOfFoundGood += evaluationResult.getNumberOfFoundGood();
        numberOfFoundBad += evaluationResult.getNumberOfFoundBad();
        numberOfFoundOthers += evaluationResult.getNumberOfFoundOthers();
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
}
