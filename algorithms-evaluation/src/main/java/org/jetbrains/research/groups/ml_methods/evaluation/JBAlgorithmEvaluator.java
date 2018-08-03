package org.jetbrains.research.groups.ml_methods.evaluation;

import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class JBAlgorithmEvaluator {
    public static EvaluationResult evaluateDataset(Path datasetPath, Algorithm algorithmToEvaluate) throws IOException {
        CombinedEvaluationResult combinedEvaluationResult = new CombinedEvaluationResult(algorithmToEvaluate);
        for (File rootFolder : Objects.requireNonNull(datasetPath.toFile().listFiles())) {
            ProjectToEvaluate projectToEvaluate = ProjectLoader.loadForEvaluation(rootFolder.toPath());
            combinedEvaluationResult.addResult(AlgorithmEvaluator.evaluate(projectToEvaluate, algorithmToEvaluate));
        }
        return combinedEvaluationResult;
    }
}
