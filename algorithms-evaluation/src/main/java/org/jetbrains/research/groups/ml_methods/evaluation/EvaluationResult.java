package org.jetbrains.research.groups.ml_methods.evaluation;

public interface EvaluationResult {
    int getNumberOfFoundGood();
    int getNumberOfFoundBad();
    int getNumberOfGood();
    int getNumberOfBad();
}
