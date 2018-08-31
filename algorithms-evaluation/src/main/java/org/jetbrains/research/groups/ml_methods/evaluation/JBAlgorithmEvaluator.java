package org.jetbrains.research.groups.ml_methods.evaluation;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;
import org.jetbrains.research.groups.ml_methods.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

class JBAlgorithmEvaluator {
    private static final @NotNull Logger LOGGER =
            Logging.getLogger(AlgorithmsEvaluationApplicationStarter.class);

    static EvaluationResult evaluateDataset(Path datasetPath, Algorithm algorithmToEvaluate,
                                            @Nullable Integer topRefactoringsBound) throws IOException {
        CombinedEvaluationResult combinedEvaluationResult = new CombinedEvaluationResult(algorithmToEvaluate);
        File[] rootFolders = Objects.requireNonNull(datasetPath.toFile().listFiles());
        for (int i = 0; i < rootFolders.length; i++) {
            ProjectToEvaluate projectToEvaluate = ProjectLoader.loadForEvaluation(rootFolders[i].toPath());
            LOGGER.info(i + "/" + rootFolders.length + " projects are evaluated");
            LOGGER.info("Starting evaluation of new project: " + rootFolders[i].getAbsolutePath());
            combinedEvaluationResult.addResult(AlgorithmEvaluator.evaluate(projectToEvaluate, algorithmToEvaluate, topRefactoringsBound));
        }
        return combinedEvaluationResult;
    }
}
