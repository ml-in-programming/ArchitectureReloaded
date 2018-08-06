package org.jetbrains.research.groups.ml_methods.evaluation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.research.groups.ml_methods.evaluation.EvaluationResult.Evaluation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

class EvaluationResultsWriter {
    private static final Gson JSON_CONVERTER = new GsonBuilder().setPrettyPrinting().create();
    private static final EnumMap<Evaluation, String> evaluationNames = new EnumMap<>(Evaluation.class);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    static {
        evaluationNames.put(Evaluation.GOOD_PRECISION, "Good Precision");
        evaluationNames.put(Evaluation.GOOD_RECALL, "Good Recall");
        evaluationNames.put(Evaluation.BAD_PRECISION, "Bad Precision");
        evaluationNames.put(Evaluation.BAD_RECALL, "Bad Recall");
        evaluationNames.put(Evaluation.MSE, "Mean Square Error");
        evaluationNames.put(Evaluation.ME, "Mean Error");
    }

    static void writeTable(List<EvaluationResult> evaluationResults, Path outPath) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("<table border=\"1\">");

        lines.add("<tr>");
        lines.addAll(Arrays.asList("<th>", "", "</th>"));
        for (EvaluationResult evaluationResult : evaluationResults) {
            lines.addAll(Arrays.asList("<th>", evaluationResult.getAlgorithm().getDescriptionString(), "</th>"));
        }
        lines.add("</tr>");

        for (Evaluation evaluation : Evaluation.values()) {
            List<Double> results = evaluationResults.stream()
                    .map(evaluationResult -> evaluationResult.getEvaluation(evaluation).get())
                    .collect(Collectors.toList());
            Double valueToHighlight = evaluation.equals(Evaluation.MSE) || evaluation.equals(Evaluation.ME) ?
                    Collections.min(results) : Collections.max(results);

            lines.add("<tr>");
            lines.addAll(Arrays.asList("<td>", evaluationNames.get(evaluation), "</td>"));
            for (Double result : results) {
                if (result.equals(valueToHighlight)) {
                    lines.addAll(Arrays.asList("<td>", "<b>", DECIMAL_FORMAT.format(result), "</b>", "</td>"));
                } else {
                    lines.addAll(Arrays.asList("<td>", DECIMAL_FORMAT.format(result), "</td>"));
                }
            }
            lines.add("</tr>");
        }

        lines.add("</table>");
        Files.write(outPath, lines);
    }

    static void writeJSON(List<EvaluationResult> evaluationResults, Path outPath) throws IOException {
        Files.write(outPath, Collections.singleton(JSON_CONVERTER.toJson(evaluationResults)));
    }
}
