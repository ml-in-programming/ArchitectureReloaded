package org.jetbrains.research.groups.ml_methods.extraction.refactoring.writers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.MoveMethodRefactoring;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.JBRefactoringTextRepresentation;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringTextRepresentation;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JBWriter implements RefactoringsWriter {
    private static final Gson JSON_CONVERTER = new GsonBuilder().setPrettyPrinting().create();
    private static final String NAME = "JB";

    @Override
    public void write(List<MoveMethodRefactoring> refactorings, Path refactoringsPath) throws IOException {
        writeRefactoringsInTextForm(refactorings.stream()
                        .map(JBRefactoringTextRepresentation::new)
                        .collect(Collectors.toList()),
                refactoringsPath);
    }

    @Override
    public void write(List<MoveMethodRefactoring> refactorings, OutputStream outputStream) throws IOException {
        writeRefactoringsInTextForm(refactorings.stream()
                        .map(JBRefactoringTextRepresentation::new)
                        .collect(Collectors.toList()),
                outputStream);
    }

    @Override
    public void writeRefactoringsInTextForm(List<RefactoringTextRepresentation> refactorings, Path refactoringsPath) throws IOException {
        Files.write(refactoringsPath, Collections.singleton(JSON_CONVERTER.toJson(refactorings)));
    }

    @Override
    public void writeRefactoringsInTextForm(List<RefactoringTextRepresentation> refactorings, OutputStream outputStream) throws IOException {
        outputStream.write(JSON_CONVERTER.toJson(refactorings).getBytes());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
