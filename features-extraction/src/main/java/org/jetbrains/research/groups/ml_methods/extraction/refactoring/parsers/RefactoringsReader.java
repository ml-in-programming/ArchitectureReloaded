package org.jetbrains.research.groups.ml_methods.extraction.refactoring.parsers;

import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringTextRepresentation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface RefactoringsReader {
    List<RefactoringTextRepresentation> read(Path refactoringsPath) throws IOException;
    List<RefactoringTextRepresentation> read(InputStream inputStream) throws IOException;
}
