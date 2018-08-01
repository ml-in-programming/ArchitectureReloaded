package org.jetbrains.research.groups.ml_methods.evaluation;

public interface EvaluationResult {
    int getNumberOfGood();

    int getNumberOfBad();

    int getNumberOfFoundGood();

    int getNumberOfFoundBad();

    int getNumberOfFoundOthers();
}
