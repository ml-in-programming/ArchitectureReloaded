package org.jetbrains.research.groups.ml_methods.extraction.refactoring.writers;

import org.jetbrains.research.groups.ml_methods.refactoring.MoveMethodRefactoring;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringTextRepresentation;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

public interface RefactoringsWriter {
    void write(List<MoveMethodRefactoring> refactorings, Path refactoringsPath) throws IOException;
    void write(List<MoveMethodRefactoring> refactorings, OutputStream outputStream) throws IOException;
    void writeRefactoringsInTextForm(List<RefactoringTextRepresentation> refactorings, Path refactoringsPath) throws IOException;
    void writeRefactoringsInTextForm(List<RefactoringTextRepresentation> refactorings, OutputStream outputStream) throws IOException;
    String getName();
}
