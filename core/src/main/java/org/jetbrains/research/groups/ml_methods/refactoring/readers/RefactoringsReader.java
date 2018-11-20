package org.jetbrains.research.groups.ml_methods.refactoring.readers;

import org.jetbrains.research.groups.ml_methods.refactoring.RefactoringTextRepresentation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface RefactoringsReader {
    List<RefactoringTextRepresentation> read(Path refactoringsPath) throws IOException;
    List<RefactoringTextRepresentation> read(InputStream inputStream) throws IOException;
    String getName();
}
