package org.jetbrains.research.groups.ml_methods.utils;

import org.apache.log4j.Logger;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.Refactoring;
import org.jetbrains.research.groups.ml_methods.config.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Function;

import static java.nio.file.Files.createFile;
import static java.nio.file.Files.write;

public class ExportResultsUtil {
    private static final Logger LOGGER = Logging.getLogger(ExportResultsUtil.class);
    private static final String fileName = "Results.txt";

    private ExportResultsUtil(){
    }

    public static void exportToFile(List<Refactoring> refactorings, String directory) {
        exportToFile(refactorings, ExportResultsUtil::defaultRefactoringView, directory);
    }

    public static void exportToFile(List<Refactoring> refactorings, Function<Refactoring, String> show, String directory) {
        try {
            StringBuilder results = new StringBuilder();
            for (Refactoring refactoring: refactorings) {
                results.append(show.apply(refactoring)).append(System.lineSeparator());
            }
            Path path = Paths.get(directory + File.separator + fileName);
            Files.deleteIfExists(path);
            createFile(path);
            write(path, results.toString().getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
        }
    }

    private static String defaultRefactoringView(Refactoring r) {
        return String.format("%s --> %s (%s)", r.getEntityName(), r.getTargetName(), r.getAccuracy());
    }
}
