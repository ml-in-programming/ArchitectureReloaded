package org.jetbrains.research.groups.ml_methods.refactoring.logging;

import com.sixrr.metrics.Metric;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.stockmetrics.methodMetrics.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.MoveFieldRefactoring;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.MoveMethodRefactoring;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.Refactoring;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.RefactoringVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class contains features extracted from some {@link Refactoring}. This features should
 * characterize {@link Refactoring} in a way that helps to extract user refactorings preferences
 * from features of accepted and rejected refactorings.
 */
public abstract class RefactoringFeatures {
    private static final @NotNull List<Metric> requestedMetrics =
        Arrays.asList(
            new FormalParametersCountMethodMetric(),
            new LinesOfCodeMethodMetric(),
            new NumAssertsMetric(),
            new NumLoopsMetric(),
            new NumLocalVarsMetric(),
            new IsStaticMethodMetric(),
            new IsPrivateMethodMetric(),
            new NameLenMethodMetric(),
            new NumExceptionsThrownMetric()
        );

    private static final @NotNull Set<String> requestedMetricsIds =
        requestedMetrics.stream().map(Metric::getID).collect(Collectors.toSet());

    @NotNull
    public abstract <R> R accept(final @NotNull RefactoringFeaturesVisitor<R> visitor);

    /**
     * Extracts features from a given {@link Refactoring}.
     *
     * @param refactoring a {@link Refactoring} to extract features from.
     * @param metricsRun a result of metrics calculations. Some of metrics values calculated for
     *                   objects given refactoring operates on can be used to extract refactoring
     *                   features.
     */
    public static @NotNull RefactoringFeatures extractFeatures(
        final @NotNull Refactoring refactoring,
        final @NotNull MetricsRun metricsRun
    ) {
        return refactoring.accept(new RefactoringVisitor<RefactoringFeatures>() {
            @Override
            public @NotNull RefactoringFeatures visit(@NotNull MoveMethodRefactoring refactoring) {
                return new MoveMethodRefactoringFeatures(refactoring, metricsRun);
            }

            @Override
            public @NotNull RefactoringFeatures visit(@NotNull MoveFieldRefactoring refactoring) {
                return new MoveFieldRefactoringFeatures(refactoring, metricsRun);
            }
        });
    }

    /**
     * Returns {@link List} of {@link Metric}s that are required to construct
     * {@link RefactoringFeatures}. This returned metrics are supposed to be calculated during
     * common metrics calculation process and passed to {@link #extractFeatures} method when
     * features extraction is required.
     */
    public static @NotNull List<Metric> getRequestedMetrics() {
        return requestedMetrics;
    }

    protected static List<MetricCalculationResult> extractMetricsResultsFor(
        final @NotNull String measuredObject,
        final @NotNull MetricsResult results
    ) {
        List<MetricCalculationResult> extractedResults = new ArrayList<>();

        Metric[] metrics = results.getMetrics();

        for (Metric metric : metrics) {
            if (!requestedMetricsIds.contains(metric.getID())) {
                continue;
            }

            double metricValue = results.getValueForMetric(metric, measuredObject);

            extractedResults.add(
                    new MetricCalculationResult(metric.getID(), metricValue)
            );
        }

        return extractedResults;
    }

    /**
     * Result of calculation of one single {@link Metric} for a particular object. Contains actual
     * real value and metric's id.
     */
    public static class MetricCalculationResult {
        private final @NotNull String metricId;

        private final double metricValue;

        public MetricCalculationResult(
            final @NotNull String metricId,
            final double metricValue
        ) {
            this.metricId = metricId;
            this.metricValue = metricValue;
        }

        public @NotNull String getMetricId() {
            return metricId;
        }

        public double getMetricValue() {
            return metricValue;
        }
    }
}
