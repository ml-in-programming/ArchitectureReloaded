package org.jetbrains.research.groups.ml_methods.evaluation;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.application.ex.ApplicationEx;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;
import org.jetbrains.research.groups.ml_methods.algorithm.AlgorithmsRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class AlgorithmsEvaluationApplicationStarter implements ApplicationStarter {
    private static final ApplicationEx APPLICATION = (ApplicationEx) ApplicationManager.getApplication();
    private static final int NUMBER_OF_ARGUMENTS = 4;
    private static final @NotNull Logger LOGGER =
            Logger.getLogger(AlgorithmsEvaluationApplicationStarter.class);

    static {
        LOGGER.setLevel(Level.INFO);
        LOGGER.addAppender(new ConsoleAppender(new PatternLayout("%p %m%n")));
    }

    private static void checkCommandLineArguments(@NotNull String[] args) {
        if (args.length != NUMBER_OF_ARGUMENTS) {
            printUsage();
            APPLICATION.exit(true, true);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: algorithms-evaluation <path to dataset folder> <algorithms names> <pathToSaveResults>");
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
            List<String> algorithmsNames = Arrays.asList(args[2].split(","));
            Path pathToSaveResults = Paths.get(args[3]);
            List<Algorithm> algorithmsToEvaluate;
            if (algorithmsNames.get(0).equals("")) {
                algorithmsToEvaluate = AlgorithmsRepository.getAvailableAlgorithms();
            } else {
                algorithmsToEvaluate = algorithmsNames.stream()
                        .map(algorithmName -> AlgorithmsRepository.getAlgorithmByName(algorithmName)
                                .orElseThrow(() -> new IllegalArgumentException("No such algorithm")))
                        .collect(Collectors.toList());
            }
            List<EvaluationResult> evaluationResults = algorithmsToEvaluate.parallelStream().map(algorithm -> {
                try {
                    return JBAlgorithmEvaluator.evaluateDataset(datasetPath, algorithm);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
            evaluationResults.forEach(this::printEvaluationResult);
            if (!pathToSaveResults.toString().equals("")) {
                EvaluationResultsWriter.writeTable(evaluationResults, pathToSaveResults);
            }
        } catch (Throwable throwable) {
            System.out.println(throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
            throwable.printStackTrace();
        } finally {
            APPLICATION.exit(true, true);
        }
    }

    private void printEvaluationResult(EvaluationResult evaluationResult) {
        System.out.println("==================");
        System.out.println("EVALUATION RESULT FOR " + evaluationResult.getAlgorithm().getDescriptionString());
        System.out.println("Number of good: " + evaluationResult.getNumberOfGood());
        System.out.println("Number of found good: " + evaluationResult.getNumberOfFoundGood());
        System.out.println("Number of bad: " + evaluationResult.getNumberOfBad());
        System.out.println("Number of found bad: " + evaluationResult.getNumberOfFoundBad());
        System.out.println("Number of found others: " + evaluationResult.getNumberOfFoundOthers());
        System.out.println("Good precision: " + evaluationResult.getGoodPrecision());
        System.out.println("Bad precision: " + evaluationResult.getBadPrecision());
        System.out.println("Good recall: " + evaluationResult.getGoodRecall());
        System.out.println("Bad recall: " + evaluationResult.getBadRecall());
        System.out.println("Mean squared error (MSE): " + evaluationResult.getMSE());
        System.out.println("Mean error (ME): " + evaluationResult.getME());
    }
}
