package org.jetbrains.research.groups.ml_methods.evaluation;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.Project;
import com.sixrr.metrics.utils.ProjectUtils;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringsLoader;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.readers.RefactoringsReaders;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveToClassRefactoring;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

class ProjectLoader {
    static ProjectToEvaluate loadForEvaluation(Path rootFolder) throws IOException {
        final Path projectPath = rootFolder.resolve("project");
        final Project project = ProjectUtils.loadProjectWithAllDependencies(projectPath);
        if (project == null) {
            final String errorMessage = "Cannot open project. Check that path is correct and SDK is set up. " +
                    "Passed path: " + projectPath.toAbsolutePath();
            throw new IllegalArgumentException(errorMessage);
        }
        final AnalysisScope scope = new AnalysisScope(project);
        List<MoveToClassRefactoring> goodRefactorings = RefactoringsLoader.load(rootFolder.resolve("good"),
                RefactoringsReaders.getJBReader(), scope)
                .stream()
                .map(moveMethodRefactoring -> (MoveToClassRefactoring)moveMethodRefactoring)
                .collect(Collectors.toList());
        List<MoveToClassRefactoring> badRefactorings = RefactoringsLoader.load(rootFolder.resolve("bad"),
                RefactoringsReaders.getJBReader(), scope)
                .stream()
                .map(moveMethodRefactoring -> (MoveToClassRefactoring)moveMethodRefactoring)
                .collect(Collectors.toList());
        return new ProjectToEvaluate(project, goodRefactorings, badRefactorings);
    }
}
