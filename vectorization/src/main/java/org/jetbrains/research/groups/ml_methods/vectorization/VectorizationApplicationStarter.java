package org.jetbrains.research.groups.ml_methods.vectorization;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.application.ex.ApplicationEx;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;


public class VectorizationApplicationStarter implements ApplicationStarter {
    private static final ApplicationEx APPLICATION = (ApplicationEx) ApplicationManager.getApplication();
    private static final int NUMBER_OF_ARGUMENTS = 3;
    private static final @NotNull
    Logger LOGGER = Logger.getLogger(VectorizationApplicationStarter.class);

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
        System.out.println("Usage: vectorization <path to dataset folder> <vectorization type> <path to save results>");
    }

    public static void logError(Throwable throwable, Logger logger, String message) {
        logger.error(throwable.getClass().getSimpleName() + ": " + throwable.getMessage()
                + "\n Message: " + message);
    }

    public static void showError(Throwable throwable, String message) {
        System.out.println(throwable.getClass().getSimpleName() + ": " + throwable.getMessage()
                + "\n Message: " + message);
        throwable.printStackTrace();
    }

    @Override
    public String getCommandName() {
        return "vectorization";
    }

    @Override
    public void premain(String[] args) {
    }

    public void main(String[] args) {
        try {
            checkCommandLineArguments(args);
            Path datasetPath = Paths.get(args[1]);
            Vectorization vectorization =
                    VectorizationFactory.parseType(args[2])
                            .orElseThrow(() -> new IllegalArgumentException("Unknown vectorization type"));
            vectorization.vectorizeAndSave(datasetPath);
        } catch (Throwable throwable) {
            String message = "Unhandled error occurred, application exiting";
            logError(throwable, LOGGER, message);
            showError(throwable, message);
        } finally {
            APPLICATION.exit(true, true);
        }
    }
}
