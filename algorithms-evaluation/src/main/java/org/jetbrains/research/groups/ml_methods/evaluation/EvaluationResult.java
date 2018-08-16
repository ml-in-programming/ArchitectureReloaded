package org.jetbrains.research.groups.ml_methods.evaluation;

import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;

import java.util.List;
import java.util.function.Supplier;

public interface EvaluationResult {
    int getNumberOfGood();

    int getNumberOfBad();

    int getNumberOfFoundGood();

    int getNumberOfFoundBad();

    int getNumberOfFoundOthers();

    double getGoodPrecision();

    double getGoodRecall();

    double getBadPrecision();

    double getBadRecall();

    List<Double> getErrors();

    double getMSE();

    double getME();

    Algorithm getAlgorithm();

    Supplier<Double> getEvaluation(Evaluation evaluation);

    enum Evaluation {
        GOOD_PRECISION, GOOD_RECALL, BAD_PRECISION, BAD_RECALL, MSE, ME
    }
}
