package org.jetbrains.research.groups.ml_methods.evaluation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class EvaluationResultsWriter {
    private static final Gson JSON_CONVERTER = new GsonBuilder().setPrettyPrinting().create();

    public static void writeTable(List<EvaluationResult> evaluationResults, Path outPath) {
    }

    public static void writeJSON(List<EvaluationResult> evaluationResults, Path outPath) throws IOException {
        Files.write(outPath, Collections.singleton(JSON_CONVERTER.toJson(evaluationResults)));
    }
}
