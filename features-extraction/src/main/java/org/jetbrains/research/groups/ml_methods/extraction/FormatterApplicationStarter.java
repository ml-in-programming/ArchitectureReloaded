package org.jetbrains.research.groups.ml_methods.extraction;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.project.Project;
import com.sixrr.metrics.utils.ProjectUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringsLoader;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.readers.RefactoringsReader;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.readers.RefactoringsReaders;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.writers.RefactoringsWriter;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.writers.RefactoringsWriters;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveMethodRefactoring;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static com.sixrr.metrics.utils.MethodUtils.extractMethodDeclaration;


public class FormatterApplicationStarter implements ApplicationStarter {
    private static final ApplicationEx APPLICATION = (ApplicationEx) ApplicationManager.getApplication();

    private static final @NotNull Logger LOGGER =
        Logger.getLogger(FormatterApplicationStarter.class);

    static {
        LOGGER.setLevel(Level.INFO);
        LOGGER.addAppender(new ConsoleAppender(new PatternLayout("%p %m%n")));
    }

    private static void checkCommandLineArguments(@NotNull String[] args) {
        if (args.length != 6) {
            printUsage();
            APPLICATION.exit(true, true);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: reformat-refactorings <pathToProject> <in> <inFormatName> <out> <outFormatName>");
    }

    @Override
    public String getCommandName() {
        return "reformat-refactorings";
    }

    @Override
    public void premain(String[] args) {
    }

    public void main(String[] args) {
        try {
            checkCommandLineArguments(args);
            Path projectPath = Paths.get(args[1]);
            Path in = Paths.get(args[2]);
            RefactoringsReader reader = RefactoringsReaders
                    .getReaderByName(args[3])
                    .orElseThrow(() -> new IllegalArgumentException("No reader in format " + args[3] + " is available"));
            Path out = Paths.get(args[4]);
            RefactoringsWriter writer = RefactoringsWriters
                    .getWriterByName(args[5])
                    .orElseThrow(() -> new IllegalArgumentException("No writer in format " + args[5] + " is available"));

            LOGGER.info("Opening project...");
            LOGGER.info(projectPath.toAbsolutePath().toString());
            final Project project = ProjectUtils.loadProjectWithAllDependencies(projectPath);
            if (project == null) {
                System.err.println("Cannot open project. Check that path is correct.");
                APPLICATION.exit(true, true);
            }
            final AnalysisScope scope = new AnalysisScope(Objects.requireNonNull(project));
            List<MoveMethodRefactoring> refactorings;
            try {
                refactorings = RefactoringsLoader.load(in, reader, scope);
            } catch (Exception e) {
                System.err.println("Error during refactorings search. Reason: " + e.getMessage());
                e.printStackTrace();
                APPLICATION.exit(true, true);
                return;
            }

            LOGGER.info("Found " + refactorings.size() + " refactorings: ");
            refactorings.forEach(refactoring ->
                    LOGGER.info(
                            refactoring.getMethod() + "->" +
                                    refactoring.getTargetClass() + System.lineSeparator() +
                                    extractMethodDeclaration(refactoring.getMethodOrThrow()))
            );
            writer.write(refactorings, out);
        } catch (Throwable throwable) {
            LOGGER.error("Error: "+ throwable.getMessage());
            throwable.printStackTrace();
        } finally {
            APPLICATION.exit(true, true);
        }
    }
}
