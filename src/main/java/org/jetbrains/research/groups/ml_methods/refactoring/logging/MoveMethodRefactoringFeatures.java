package org.jetbrains.research.groups.ml_methods.refactoring.logging;

import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveMethodRefactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Subclass of {@link RefactoringFeatures} that contains features of a
 * {@link MoveMethodRefactoring}.
 */
public class MoveMethodRefactoringFeatures extends RefactoringFeatures {
    private final @NotNull List<MetricCalculationResult> targetClassMetricsValues;

    private final @NotNull List<MetricCalculationResult> sourceClassMetricsValues;

    private final @NotNull List<MetricCalculationResult> methodMetricsValues;

    /**
     * Extracts features from a given {@link MoveMethodRefactoring}.
     *
     * @param refactoring a {@link MoveMethodRefactoring} to extract features from.
     * @param metricsRun a result of metrics calculations. Some of metrics values calculated for
     *                   objects given refactoring operates on can be used to extract refactoring
     *                   features.
     */
    public MoveMethodRefactoringFeatures(
        final @NotNull MoveMethodRefactoring refactoring,
        final @NotNull MetricsRun metricsRun
    ) {
        MetricsResult resultsForClasses = metricsRun.getResultsForCategory(MetricCategory.Class);
        MetricsResult resultsForMethods = metricsRun.getResultsForCategory(MetricCategory.Method);

        targetClassMetricsValues = extractMetricsResultsFor(
                Objects.requireNonNull(refactoring.getTargetClass().getQualifiedName()),
                resultsForClasses
        );

        sourceClassMetricsValues = extractMetricsResultsFor(
                Objects.requireNonNull(refactoring.getContainingClass().getQualifiedName()),
                resultsForClasses
        );

        methodMetricsValues = extractMetricsResultsFor(
            MethodUtils.calculateUniqueSignature(refactoring.getMethod()),
            resultsForMethods
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

    /**
     * Returns {@link List} of {@link MetricCalculationResult} with all extracted metrics values for
     * a method that is moved in initial {@link MoveMethodRefactoring}.
     */
    public @NotNull List<MetricCalculationResult> getMethodMetricsValues() {
        return new ArrayList<>(methodMetricsValues);
    }

    @NotNull
    @Override
    public <R> R accept(@NotNull RefactoringFeaturesVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
