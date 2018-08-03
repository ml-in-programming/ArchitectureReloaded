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

import java.nio.file.Path;
import java.nio.file.Paths;


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
            Algorithm algorithm = AlgorithmsRepository.getAlgorithmByName(algorithmName).orElseThrow(() ->
                    new IllegalArgumentException("No such algorithm"));
            EvaluationResult evaluationResult = JBAlgorithmEvaluator.evaluateDataset(datasetPath, algorithm);
            printEvaluationResult(evaluationResult);
        } catch (Throwable throwable) {
            System.out.println(throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
            throwable.printStackTrace();
        } finally {
            APPLICATION.exit(true, true);
        }
    }

    private void printEvaluationResult(EvaluationResult evaluationResult) {
        System.out.println("==================");
        System.out.println("EVALUATION RESULT");
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
    }
}
