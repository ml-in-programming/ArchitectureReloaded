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

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class AlgorithmsEvaluationApplicationStarter implements ApplicationStarter {
    private static final ApplicationEx APPLICATION = (ApplicationEx) ApplicationManager.getApplication();
    private static final int NUMBER_OF_ARGUMENTS = 5;
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
            Integer topRefactoringsBound = args[4].equals("") ? null : Integer.parseInt(args[4]);
            List<Algorithm> algorithmsToEvaluate;
            if (algorithmsNames.get(0).equals("")) {
                algorithmsToEvaluate = AlgorithmsRepository.getAvailableAlgorithms();
            } else {
                algorithmsToEvaluate = algorithmsNames.stream()
                        .map(algorithmName -> AlgorithmsRepository.getAlgorithmByName(algorithmName)
                                .orElseThrow(() -> new IllegalArgumentException("No such algorithm")))
                        .collect(Collectors.toList());
            }
            List<EvaluationResult> evaluationResults = new ArrayList<>();
            for (Algorithm algorithm : algorithmsToEvaluate) {
                evaluationResults.add(JBAlgorithmEvaluator.evaluateDataset(datasetPath, algorithm, topRefactoringsBound));
            }
            try (PrintStream writeResultsStream = new PrintStream(Files.newOutputStream(pathToSaveResults))) {
                evaluationResults.forEach(evaluationResult -> {
                    printEvaluationResult(evaluationResult);
                    printEvaluationResult(evaluationResult, writeResultsStream, true);
                });
            }
            if (!pathToSaveResults.toString().equals("")) {
                EvaluationResultsWriter.writeTable(evaluationResults, pathToSaveResults, StandardOpenOption.APPEND);
            }
        } catch (Throwable throwable) {
            System.out.println(throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
            throwable.printStackTrace();
        } finally {
            APPLICATION.exit(true, true);
        }
    }

    private void printEvaluationResult(@NotNull EvaluationResult evaluationResult) {
        printEvaluationResult(evaluationResult, System.out, false);
    }

    private void printEvaluationResult(@NotNull EvaluationResult evaluationResult,
                                       @NotNull PrintStream printStream,
                                       boolean isHtmlFile) {
        String lineBreak = isHtmlFile ? "<br>" : System.lineSeparator();
        printStream.print("==================" + lineBreak);
        printStream.print("EVALUATION RESULT FOR " + evaluationResult.getAlgorithm().getDescriptionString() + lineBreak);
        printStream.print("Number of good: " + evaluationResult.getNumberOfGood() + lineBreak);
        printStream.print("Number of found good: " + evaluationResult.getNumberOfFoundGood() + lineBreak);
        printStream.print("Number of bad: " + evaluationResult.getNumberOfBad() + lineBreak);
        printStream.print("Number of found bad: " + evaluationResult.getNumberOfFoundBad() + lineBreak);
        printStream.print("Number of found others: " + evaluationResult.getNumberOfFoundOthers() + lineBreak);
        printStream.print("Good precision: " + evaluationResult.getGoodPrecision() + lineBreak);
        printStream.print("Bad precision: " + evaluationResult.getBadPrecision() + lineBreak);
        printStream.print("Good recall: " + evaluationResult.getGoodRecall() + lineBreak);
        printStream.print("Bad recall: " + evaluationResult.getBadRecall() + lineBreak);
        printStream.print("Mean squared error (MSE): " + evaluationResult.getMSE() + lineBreak);
        printStream.print("Mean error (ME): " + evaluationResult.getME() + lineBreak);
    }
}
