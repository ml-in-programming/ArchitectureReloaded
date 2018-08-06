package org.jetbrains.research.groups.ml_methods.extraction.refactoring;

import com.intellij.analysis.AnalysisScope;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.readers.RefactoringsReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class RefactoringsLoader {
    public static List<Refactoring> load(Path refactoringsPath, RefactoringsReader reader, AnalysisScope scope) throws IOException {
        return RefactoringsFinder.find(scope, reader.read(refactoringsPath));
    }
}
