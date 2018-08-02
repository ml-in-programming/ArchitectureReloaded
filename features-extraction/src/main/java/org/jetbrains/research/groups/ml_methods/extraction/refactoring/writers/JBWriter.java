package org.jetbrains.research.groups.ml_methods.extraction.refactoring.writers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.JBRefactoringTextRepresentation;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.Refactoring;

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
    public void write(List<Refactoring> refactorings, Path refactoringsPath) throws IOException {
        List<JBRefactoringTextRepresentation> textualRefactorings =
                refactorings.stream().map(JBRefactoringTextRepresentation::new).collect(Collectors.toList());
        Files.write(refactoringsPath, Collections.singleton(JSON_CONVERTER.toJson(textualRefactorings)));
    }

    @Override
    public void write(List<Refactoring> refactorings, OutputStream outputStream) throws IOException {
        List<JBRefactoringTextRepresentation> textualRefactorings =
                refactorings.stream().map(JBRefactoringTextRepresentation::new).collect(Collectors.toList());
        outputStream.write(JSON_CONVERTER.toJson(textualRefactorings).getBytes());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
