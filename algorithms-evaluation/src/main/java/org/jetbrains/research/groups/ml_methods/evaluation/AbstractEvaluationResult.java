package org.jetbrains.research.groups.ml_methods.evaluation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;

import java.util.EnumMap;
import java.util.function.Supplier;

import static java.lang.Math.pow;

public abstract class AbstractEvaluationResult implements EvaluationResult {
    private final @NotNull EnumMap<Evaluation, Supplier<Double>> evaluationFunctions =
            new EnumMap<>(Evaluation.class);
    private final @NotNull Algorithm evaluatingAlgorithm;

    {
        evaluationFunctions.put(Evaluation.GOOD_PRECISION, this::getGoodPrecision);
        evaluationFunctions.put(Evaluation.GOOD_RECALL, this::getGoodRecall);
        evaluationFunctions.put(Evaluation.BAD_PRECISION, this::getBadPrecision);
        evaluationFunctions.put(Evaluation.BAD_RECALL, this::getBadRecall);
        evaluationFunctions.put(Evaluation.MSE, this::getMSE);
        evaluationFunctions.put(Evaluation.ME, this::getME);
    }

    AbstractEvaluationResult(@NotNull Algorithm evaluatingAlgorithm) {
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
        return getErrors().stream().mapToDouble(Double::doubleValue).map(operand -> pow(operand, 2)).sum() / getErrors().size();
    }

    @Override
    public double getME() {
        return getErrors().stream().mapToDouble(Double::doubleValue).sum() / getErrors().size();
    }

    private int getNumberOfBadAndGood() {
        return getNumberOfBad() + getNumberOfGood();
    }

    private int getNumberOfFoundBadAndGood() {
        return getNumberOfFoundBad() + getNumberOfFoundGood();
    }

    @Override
    public Supplier<Double> getEvaluation(Evaluation evaluation) {
        return evaluationFunctions.get(evaluation);
    }
}
