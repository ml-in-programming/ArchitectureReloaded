package org.jetbrains.research.groups.ml_methods.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import org.apache.log4j.Logger;
import org.jetbrains.research.groups.ml_methods.logging.Logging;
import org.jetbrains.research.groups.ml_methods.refactoring.CalculatedRefactoring;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.nio.file.Files.createFile;
import static java.nio.file.Files.write;
import static org.jetbrains.research.groups.ml_methods.utils.PSIUtil.getHumanReadableName;

public class ExportResultsUtil {
    private static final Logger LOGGER = Logging.getLogger(ExportResultsUtil.class);
    private static final String fileName = "Results.txt";

    private ExportResultsUtil(){
    }

    public static void exportToFile(List<CalculatedRefactoring> refactorings, String directory) {
        exportToFile(refactorings, ExportResultsUtil::defaultRefactoringView, directory);
    }

    public static void exportToFile(List<CalculatedRefactoring> refactorings, Function<CalculatedRefactoring, String> show, String directory) {
        try {
            StringBuilder results = new StringBuilder();
            for (CalculatedRefactoring refactoring: refactorings) {
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

    private static String defaultRefactoringView(CalculatedRefactoring r) {
        Optional<PsiMember> member = r.getRefactoring().getEntity();
        Optional<PsiClass> target = r.getRefactoring().getTargetClass();
        return String.format("%s --> %s (%s)",
                member.isPresent() ?
                        getHumanReadableName(member.get()) :
                        ArchitectureReloadedBundle.message("java.member.is.not.valid"),
                target.isPresent() ?
                        getHumanReadableName(target.get()) :
                        ArchitectureReloadedBundle.message("target.class.is.not.valid"),
                r.getAccuracy());
    }
}
