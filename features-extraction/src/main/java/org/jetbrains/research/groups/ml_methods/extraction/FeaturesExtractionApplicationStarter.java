package org.jetbrains.research.groups.ml_methods.extraction;

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
import org.jetbrains.research.groups.ml_methods.extraction.features.extractors.*;
import org.jetbrains.research.groups.ml_methods.extraction.features.vector.FeatureVector;
import org.jetbrains.research.groups.ml_methods.extraction.features.vector.VectorSerializer;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.Refactoring;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringTextRepresentation;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringsLoader;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.readers.RefactoringsReaders;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.writers.RefactoringsWriters;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.jetbrains.research.groups.ml_methods.utils.MethodUtils.extractMethodDeclaration;


public class FeaturesExtractionApplicationStarter implements ApplicationStarter {
    private static final ApplicationEx APPLICATION = (ApplicationEx) ApplicationManager.getApplication();

    private static final @NotNull Logger LOGGER =
        Logger.getLogger(FeaturesExtractionApplicationStarter.class);

    static {
        LOGGER.setLevel(Level.INFO);
        LOGGER.addAppender(new ConsoleAppender(new PatternLayout("%p %m%n")));
    }

    private static void checkCommandLineArguments(@NotNull String[] args) {
        if (args.length != 4) {
            printUsage();
            APPLICATION.exit(true, true);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: features-extraction <path to project> <path to correct refactorings> <path to output folder>");
    }

    @Override
    public String getCommandName() {
        return "features-extraction";
    }

    @Override
    public void premain(String[] args) {
    }

    public void main(String[] args) {
        try {
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
            List<Refactoring> refactorings;
            try {
                refactorings = RefactoringsLoader.load(refactoringsPath, RefactoringsReaders.getJBReader(), scope);
            } catch (Exception e) {
                System.err.println("Error during refactorings search. Reason: " + e.getMessage());
                e.printStackTrace();
                APPLICATION.exit(true, true);
                return;
            }

            System.out.println("Found " + refactorings.size() + " refactorings: ");
            refactorings.forEach(refactoring ->
                    LOGGER.info(
                            refactoring.getMethod() + "->" +
                                    refactoring.getTargetClass() + System.lineSeparator() +
                                    extractMethodDeclaration(refactoring.getMethod()))
            );
            RefactoringsWriters.getJBWriter().write(refactorings.stream().
                            map(RefactoringTextRepresentation::new).collect(Collectors.toList()),
                    Paths.get(args[3]).toAbsolutePath());

            List<FeatureVector> vectors;
            try {
                vectors = MoveMethodFeaturesExtractor.getInstance().extract(
                    scope,
                    new LinkedList<>(refactorings),
                    Arrays.asList(
                        AnotherInstanceCallersExtractor.class,
                        AnotherInstanceNotPublicCallTargetsExtractor.class,
                        AnotherInstancePublicCallTargetsExtractor.class,
                        SameClassFieldsAccessedExtractor.class,
                        SameClassStaticNotPublicCallTargetsExtractor.class,
                        SameClassStaticPublicCallTargetsExtractor.class,
                        SameInstanceCallersExtractor.class,
                        SameInstanceNotPublicCallTargetsExtractor.class,
                        SameInstancePublicCallTargetsExtractor.class,
                        TargetClassCallersExtractor.class,
                        TargetClassFieldsAccessedExtractor.class,
                        TargetClassInstanceCallTargetsExtractor.class,
                        TargetClassStaticCallTargetsExtractor.class
                    )
                );
            } catch (IllegalAccessException | InstantiationException e) {
                System.err.println("Error during features extraction. Reason: " + e.getMessage());
                e.printStackTrace();
                APPLICATION.exit(true, true);
                return;
            }

            try {
                Path path = Paths.get(args[3]).toAbsolutePath();
                path.toFile().mkdirs();

                VectorSerializer.getInstance().serialize(vectors, path);
            } catch (IOException e) {
                System.err.println(
                    "Error during features serialization. Reason: " +
                    e.getClass().getSimpleName() + ". " + e.getMessage()
                );
                e.printStackTrace();
                APPLICATION.exit(true, true);
                return;
            }
            APPLICATION.exit(true, true);
        } catch (Throwable throwable) {
            System.out.println("Error: "+ throwable.getMessage());
            throwable.printStackTrace();
        }
    }
}
