package org.jetbrains.research.groups.ml_methods.extraction.refactoring.writers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringTextRepresentation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class JBWriter implements RefactoringsWriter {
    private static final Gson JSON_CONVERTER = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void write(List<RefactoringTextRepresentation> refactorings, Path refactoringsPath) throws IOException {
        Files.write(refactoringsPath, Collections.singleton(JSON_CONVERTER.toJson(refactorings)));
    }

    @Override
    public void write(List<RefactoringTextRepresentation> refactorings, OutputStream outputStream) throws IOException {
        outputStream.write(JSON_CONVERTER.toJson(refactorings).getBytes());
    }
}
