package org.jetbrains.research.groups.ml_methods.refactoring.readers;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.research.groups.ml_methods.refactoring.JBRefactoringTextRepresentation;
import org.jetbrains.research.groups.ml_methods.refactoring.RefactoringTextRepresentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class JBReader implements RefactoringsReader {
    private static final Gson JSON_CONVERTER = new GsonBuilder().setPrettyPrinting().create();
    private static final String NAME = "JB";

    @Override
    public List<RefactoringTextRepresentation> read(Path refactoringsPath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(refactoringsPath)) {
            Type listType = new TypeToken<List<JBRefactoringTextRepresentation>>(){}.getType();
            return JSON_CONVERTER.fromJson(reader, listType);
        }
    }

    @Override
    public List<RefactoringTextRepresentation> read(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            Type listType = new TypeToken<List<JBRefactoringTextRepresentation>>(){}.getType();
            return JSON_CONVERTER.fromJson(reader, listType);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
