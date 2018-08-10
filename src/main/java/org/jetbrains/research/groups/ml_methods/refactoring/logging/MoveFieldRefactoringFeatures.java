package org.jetbrains.research.groups.ml_methods.refactoring.logging;

import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRun;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.MoveFieldRefactoring;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.MoveMethodRefactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Subclass of {@link RefactoringFeatures} that contains features of a
 * {@link MoveFieldRefactoring}.
 */
public class MoveFieldRefactoringFeatures extends RefactoringFeatures {
    private final @NotNull List<MetricCalculationResult> targetClassMetricsValues;

    private final @NotNull List<MetricCalculationResult> sourceClassMetricsValues;

    /**
     * Extracts features from a given {@link MoveFieldRefactoring}.
     *
     * @param refactoring a {@link MoveFieldRefactoring} to extract features from.
     * @param metricsRun a result of metrics calculations. Some of metrics values calculated for
     *                   objects given refactoring operates on can be used to extract refactoring
     *                   features.
     */
    public MoveFieldRefactoringFeatures(
        final @NotNull MoveFieldRefactoring refactoring,
        final @NotNull MetricsRun metricsRun
    ) {
        MetricsResult resultsForClasses = metricsRun.getResultsForCategory(MetricCategory.Class);

        targetClassMetricsValues = extractMetricsResultsFor(
                Objects.requireNonNull(refactoring.getTargetClassOrThrow().getQualifiedName()),
                resultsForClasses
        );

        sourceClassMetricsValues = extractMetricsResultsFor(
                Objects.requireNonNull(refactoring.getContainingClassOrThrow().getQualifiedName()),
                resultsForClasses
        );
    }

    /**
     * Returns {@link List} of {@link MetricCalculationResult} with all extracted metrics values for
     * a target class of initial {@link MoveMethodRefactoring}.
     */
    public @NotNull List<MetricCalculationResult> getTargetClassMetricsValues() {
        return new ArrayList<>(targetClassMetricsValues);
    }

    /**
     * Returns {@link List} of {@link MetricCalculationResult} with all extracted metrics values for
     * a source class of initial {@link MoveMethodRefactoring}.
     */
    public @NotNull List<MetricCalculationResult> getSourceClassMetricsValues() {
        return new ArrayList<>(sourceClassMetricsValues);
    }

    @NotNull
    @Override
    public <R> R accept(@NotNull RefactoringFeaturesVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
