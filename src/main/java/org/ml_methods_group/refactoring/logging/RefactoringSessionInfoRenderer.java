package org.ml_methods_group.refactoring.logging;

import org.apache.log4j.or.ObjectRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Log4J renderer for {@link RefactoringSessionInfo}. This renderer converts
 * {@link RefactoringSessionInfo} to a {@link String} that will be written to a log.
 */
public class RefactoringSessionInfoRenderer implements ObjectRenderer {
    /**
     * Converts given object which is expected to be a {@link RefactoringSessionInfo} to a
     * {@link String} representation.
     *
     * @param obj an arbitrary object which is expected to be a {@link RefactoringSessionInfo}.
     * @return string that will be written to a log.
     * @throws IllegalArgumentException if given object is not a {@link RefactoringSessionInfo}.
     */
    @Override
    public @NotNull String doRender(final @NotNull Object obj) {
        if (!(obj instanceof RefactoringSessionInfo)) {
            throw new IllegalArgumentException(
                "RefactoringSessionInfoRenderer received not a RefactoringSessionInfo as an argument!"
            );
        }

        RefactoringSessionInfo info = (RefactoringSessionInfo) obj;

        final String lineSeparator = System.getProperty("line.separator");

        StringBuilder builder = new StringBuilder();
        builder.append(lineSeparator);

        builder.append("Accepted refactorings").append(lineSeparator);
        for (RefactoringFeatures features : info.getAcceptedRefactoringsFeatures()) {
            builder.append('[')
                   .append(serializeFeatures(features, lineSeparator))
                   .append(']')
                   .append(lineSeparator);
        }

        builder.append(lineSeparator);

        builder.append("Rejected refactorings").append(lineSeparator);
        for (RefactoringFeatures features : info.getRejectedRefactoringsFeatures()) {
            builder.append('[')
                   .append(serializeFeatures(features, lineSeparator))
                   .append(']')
                   .append(lineSeparator);
        }

        return builder.toString();
    }

    private @NotNull String serializeFeatures(
        final @NotNull RefactoringFeatures features,
        final @NotNull String lineSeparator
    ) {
        return features.accept(new RefactoringFeaturesVisitor<String>() {
            @Override
            public @NotNull String visit(final @NotNull MoveMethodRefactoringFeatures features) {
                StringBuilder builder = new StringBuilder();
                builder.append("Move method refactoring").append(lineSeparator);

                builder.append("Target class metrics: ")
                       .append(serializeMetricCalculationResults(
                           features.getTargetClassMetricsValues()
                       )).append(lineSeparator);

                builder.append("Method metrics: ")
                        .append(serializeMetricCalculationResults(
                            features.getMethodMetricsValues()
                        ));

                return builder.toString();
            }

            @Override
            public @NotNull String visit(final @NotNull MoveFieldRefactoringFeatures features) {
                StringBuilder builder = new StringBuilder();
                builder.append("Move field refactoring").append(lineSeparator);

                builder.append("Target class metrics: ")
                        .append(serializeMetricCalculationResults(
                            features.getTargetClassMetricsValues()
                        ));

                return builder.toString();
            }
        });
    }

    private @NotNull String serializeMetricCalculationResults(
        final @NotNull List<RefactoringFeatures.MetricCalculationResult> results
    ) {
        return results.stream()
                      .map((it) -> it.getMetricId() + ": " + it.getMetricValue())
                      .collect(Collectors.joining(", "));
    }
}
