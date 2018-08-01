package org.jetbrains.research.groups.ml_methods.evaluation;

import com.intellij.analysis.AnalysisScope;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.project.Project;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.AlgorithmsRepository;
import org.jetbrains.research.groups.ml_methods.extraction.MoveMethodFeaturesExtractor;
import org.jetbrains.research.groups.ml_methods.extraction.features.extractors.*;
import org.jetbrains.research.groups.ml_methods.extraction.features.vector.FeatureVector;
import org.jetbrains.research.groups.ml_methods.extraction.features.vector.VectorSerializer;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.Refactoring;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringTextRepresentation;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringsLoader;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.readers.RefactoringsReaders;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.writers.RefactoringsWriters;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.jetbrains.research.groups.ml_methods.utils.MethodUtils.extractMethodDeclaration;


public class AlgorithmsEvaluationApplicationStarter implements ApplicationStarter {
    private static final ApplicationEx APPLICATION = (ApplicationEx) ApplicationManager.getApplication();

    private static final @NotNull Logger LOGGER =
            Logger.getLogger(AlgorithmsEvaluationApplicationStarter.class);

    static {
        LOGGER.setLevel(Level.INFO);
        LOGGER.addAppender(new ConsoleAppender(new PatternLayout("%p %m%n")));
    }

    private static void checkCommandLineArguments(@NotNull String[] args) {
        if (args.length != 3) {
            printUsage();
            APPLICATION.exit(true, true);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: algorithms-evaluation <path to dataset folder> <algorithm name>");
    }

    @Override
    public String getCommandName() {
        return "algorithms-evaluation";
    }

    @Override
    public void premain(String[] args) {
    }

    public void main(String[] args) {
        try {
            checkCommandLineArguments(args);
            Path datasetPath = Paths.get(args[1]);
            String algorithmName = args[2];
            ProjectToEvaluate projectToEvaluate = ProjectLoader.loadForEvaluation(datasetPath);
            EvaluationResult evaluationResult = AlgorithmEvaluator.evaluate(projectToEvaluate.getProject(),
                    AlgorithmsRepository.getAlgorithmByName(algorithmName).orElseThrow(() ->
                            new IllegalArgumentException("No such algorithm")),
                    projectToEvaluate.getGoodRefactorings(),
                    projectToEvaluate.getBadRefactorings());
            System.out.println("Number of good: " + evaluationResult.getNumberOfGood());
            System.out.println("Number of found good: " + evaluationResult.getNumberOfFoundGood());
            System.out.println("Number of bad: " + evaluationResult.getNumberOfBad());
            System.out.println("Number of found bad: " + evaluationResult.getNumberOfFoundBad());
        } catch (Throwable throwable) {
            System.out.println("Error: "+ throwable.getMessage());
            throwable.printStackTrace();
        } finally {
            APPLICATION.exit(true, true);
        }
    }
}
