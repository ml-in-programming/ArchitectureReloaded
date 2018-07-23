package org.jetbrains.research.groups.ml_methods.generation;

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
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.Refactoring;
import org.jetbrains.research.groups.ml_methods.generation.constraints.GenerationConstraintsFactory;
import org.jetbrains.research.groups.ml_methods.generation.constraints.GenerationConstraintsFactory.GenerationConstraintType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.sixrr.metrics.utils.MethodUtils.calculateSignature;

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
        if (args.length != 2) {
            printUsage();
            APPLICATION.exit(true, true);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: refactorings-generation <path to project>");
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
            final Project project = ProjectUtil.openOrImport(projectPath.toAbsolutePath().toString(),
                    null, false);
            if (project == null) {
                System.err.println("Cannot open project. Check that path is correct.");
                APPLICATION.exit(true, true);
            }
            final AnalysisScope scope = new AnalysisScope(Objects.requireNonNull(project));
            Set<Refactoring> generatedRefactoring = RefactoringsGenerator.generate(GenerationConstraintsFactory.get(
                    GenerationConstraintType.ACCEPT_METHOD_PARAMS), 100, scope);
            printGeneratedRefactorings(generatedRefactoring);
        } catch (Throwable throwable) {
            System.out.println("Error: "+ throwable.getMessage());
            throwable.printStackTrace();
        }
        APPLICATION.exit(true, true);
    }

    private void printGeneratedRefactorings(Set<Refactoring> generatedRefactoring) {
        System.out.println("Generated " + generatedRefactoring.size() + " refactorings");
        for (Refactoring refactoring : generatedRefactoring) {
            System.out.print("method ");
            System.out.print(calculateSignature(refactoring.getMethod()));
            System.out.print(" need move to ");
            System.out.print(refactoring.getTargetClass().getQualifiedName());
            System.out.print('\n');
        }
    }
}
