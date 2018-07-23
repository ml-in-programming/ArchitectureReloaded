package org.jetbrains.research.groups.ml_methods.extraction.refactoring;

import com.intellij.analysis.AnalysisScope;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.parsers.RefactoringsFileParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class RefactoringsLoader {
    public static Set<Refactoring> load(Path refactoringsPath, RefactoringsFileParser parser, AnalysisScope scope) throws IOException {
        return RefactoringsFinder.find(scope, parser.parse(refactoringsPath));
    }
}
