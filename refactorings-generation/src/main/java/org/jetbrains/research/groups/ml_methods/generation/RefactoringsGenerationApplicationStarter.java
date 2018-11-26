package org.jetbrains.research.groups.ml_methods.generation;

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
import org.jetbrains.research.groups.ml_methods.generation.constraints.GenerationConstraintsFactory;
import org.jetbrains.research.groups.ml_methods.generation.constraints.GenerationConstraintsFactory.GenerationConstraintType;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveMethodRefactoring;
import org.jetbrains.research.groups.ml_methods.refactoring.writers.RefactoringsWriters;
import org.jetbrains.research.groups.ml_methods.utils.PSIUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class RefactoringsGenerationApplicationStarter implements ApplicationStarter {
    private static final ApplicationEx APPLICATION = (ApplicationEx) ApplicationManager.getApplication();

    private static final @NotNull
    Logger LOGGER =
            Logger.getLogger(RefactoringsGenerationApplicationStarter.class);

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
        System.out.println("Usage: refactorings-generation <path to project> <path to output file>");
    }

    @Override
    public String getCommandName() {
        return "refactorings-generation";
    }

    @Override
    public void premain(String[] args) {
    }

    public void main(String[] args) {
        try {
            checkCommandLineArguments(args);
            Path projectPath = Paths.get(args[1]);
            System.out.println("Opening project...");
            System.out.println(projectPath.toAbsolutePath().toString());
            final Project project = ProjectUtils.loadProjectWithAllDependencies(projectPath);
            if (project == null) {
                System.err.println("Cannot open project. Check that path is correct.");
                APPLICATION.exit(true, true);
            }
            final AnalysisScope scope = new AnalysisScope(Objects.requireNonNull(project));
            scope.setIncludeTestSource(false);
            if (scope.getFileCount() == 0) {
                System.err.println("Empty scope. Probably project cannot be open. Reload it with IDEA.");
                APPLICATION.exit(true, true);
            }
            int numberOfJavaFiles = PSIUtil.getNumberOfJavaFiles(project, false);
            int numberOfRefactoringsToGenerate = (int) (numberOfJavaFiles * 0.03);
            List<MoveMethodRefactoring> generatedRefactoring = RefactoringsGenerator.generate(GenerationConstraintsFactory.get(
                    GenerationConstraintType.ACCEPT_RELEVANT_PROPERTIES), numberOfRefactoringsToGenerate, scope);
            System.out.println("Number of java files without test sources: " + numberOfJavaFiles);
            System.out.println("Asked to generate: " + numberOfRefactoringsToGenerate);
            printGeneratedRefactorings(generatedRefactoring, Paths.get(args[2]));
        } catch (Throwable throwable) {
            System.out.println("Error: "+ throwable.getMessage());
            throwable.printStackTrace();
        }
        APPLICATION.exit(true, true);
    }

    private void printGeneratedRefactorings(List<MoveMethodRefactoring> generatedRefactoring, Path outputPath) throws IOException {
        System.out.println("Generated " + generatedRefactoring.size() + " refactorings");
        RefactoringsWriters.getJBWriter().write(generatedRefactoring, outputPath);
    }
}
