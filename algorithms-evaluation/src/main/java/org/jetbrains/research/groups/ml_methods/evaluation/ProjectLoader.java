package org.jetbrains.research.groups.ml_methods.evaluation;

import com.intellij.analysis.AnalysisScope;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.Refactoring;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringsLoader;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.readers.RefactoringsReaders;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ProjectLoader {
    public static ProjectToEvaluate loadForEvaluation(Path rootFolder) throws IOException {
        final Project project = ProjectUtil.openOrImport(rootFolder.resolve("project").toAbsolutePath().toString(),
                null, false);
        if (project == null) {
            throw new IllegalArgumentException("Cannot open project. Check that path is correct.");
        }
        final AnalysisScope scope = new AnalysisScope(project);
        List<Refactoring> goodRefactorings = RefactoringsLoader.load(rootFolder.resolve("good"),
                RefactoringsReaders.getJBReader(), scope);
        List<Refactoring> badRefactorings = RefactoringsLoader.load(rootFolder.resolve("bad"),
                RefactoringsReaders.getJBReader(), scope);
        return new ProjectToEvaluate(project, goodRefactorings, badRefactorings);
    }
}
