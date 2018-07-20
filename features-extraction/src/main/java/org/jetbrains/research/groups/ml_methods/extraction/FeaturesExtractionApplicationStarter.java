package org.jetbrains.research.groups.ml_methods.extraction;

import com.intellij.analysis.AnalysisScope;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.Refactoring;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringsLoader;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.parsers.RefactoringsFileParsers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FeaturesExtractionApplicationStarter implements ApplicationStarter {
    private static final ApplicationEx APPLICATION = (ApplicationEx) ApplicationManager.getApplication();

    private static void checkCommandLineArguments(@NotNull String[] args) {
        if (args.length != 3) {
            printUsage();
            APPLICATION.exit(true, true);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: features-extraction <path to project> <path to correct refactorings>");
    }

    @Override
    public String getCommandName() {
        return "features-extraction";
    }

    @Override
    public void premain(String[] args) {
    }

    public void main(String[] args) {
        checkCommandLineArguments(args);
        Path projectPath = Paths.get(args[1]);
        Path refactoringsPath = Paths.get(args[2]);
        System.out.println("Opening project...");
        System.out.println(projectPath.toAbsolutePath().toString());

        final Project project = ProjectUtil.openOrImport(projectPath.toAbsolutePath().toString(), null, false);
        if (project == null) {
            System.err.println("Cannot open project. Check that path is correct.");
            APPLICATION.exit(true, true);
        }
        final AnalysisScope scope = new AnalysisScope(Objects.requireNonNull(project));
        Set<Refactoring> refactorings;
        try {
            System.out.println("Start finding refactorings...");
            refactorings = RefactoringsLoader.load(refactoringsPath, RefactoringsFileParsers.getParserForJMoveDataSet(), scope);
        } catch (Exception e) {
            System.err.println("Error during refactorings search. Reason: " + e.getMessage());

            APPLICATION.exit(true, true);
            return;
        }

        System.out.println("Found " + refactorings.size() + " refactorings: ");
        refactorings.forEach(refactoring -> System.out.println(refactoring.getMethod() + "->" + refactoring.getTargetClass()));
        APPLICATION.exit(true, true);
    }
}
