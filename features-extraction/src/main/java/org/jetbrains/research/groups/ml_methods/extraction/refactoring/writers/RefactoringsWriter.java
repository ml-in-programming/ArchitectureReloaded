package org.jetbrains.research.groups.ml_methods.extraction.refactoring.writers;

import org.jetbrains.research.groups.ml_methods.extraction.refactoring.Refactoring;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

public interface RefactoringsWriter {
    void write(List<Refactoring> refactorings, Path refactoringsPath) throws IOException;
    void write(List<Refactoring> refactorings, OutputStream outputStream) throws IOException;
    String getName();
}
