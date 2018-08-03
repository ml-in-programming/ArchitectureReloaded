package org.jetbrains.research.groups.ml_methods.evaluation;

import java.util.List;

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

    List<Double> getErrorSquares();

    double getMSE();
}
