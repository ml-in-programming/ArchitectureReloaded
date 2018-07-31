package org.jetbrains.research.groups.ml_methods.extraction.refactoring.writers;

import org.jetbrains.research.groups.ml_methods.extraction.refactoring.Refactoring;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringTextRepresentation;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

public interface RefactoringsWriter {
    void write(List<RefactoringTextRepresentation> refactorings, Path refactoringsPath) throws IOException;
    void write(List<RefactoringTextRepresentation> refactorings, OutputStream outputStream) throws IOException;
}
