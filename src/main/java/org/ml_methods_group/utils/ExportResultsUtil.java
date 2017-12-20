package org.ml_methods_group.utils;

import org.apache.log4j.Logger;
import org.ml_methods_group.algorithm.Refactoring;
import org.ml_methods_group.config.Logging;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static java.nio.file.Files.*;

public class ExportResultsUtil {
    private static final Logger LOGGER = Logging.getLogger(ExportResultsUtil.class);
    private static final String fileName = "Results.txt";

    private ExportResultsUtil(){
    }

    public static void export(List<Refactoring> refactorings) {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fileChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.CANCEL_OPTION)
            return;
        try {
            StringBuilder results = new StringBuilder();
            String pathString = fileChooser.getSelectedFile().getCanonicalPath() + File.separator;
            for (Refactoring refactoring: refactorings) {
                results.append(refactoring.toString()).append('\n');
            }
            Path path = Paths.get(pathString + fileName);
            if (exists(path)) {
                delete(path);
            }
            createFile(path);
            write(path, results.toString().getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.info("Failed to create file");
        }
    }
}
