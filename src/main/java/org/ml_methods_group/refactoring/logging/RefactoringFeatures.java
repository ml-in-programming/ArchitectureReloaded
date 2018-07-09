/*
 * Copyright 2018 Machine Learning Methods in Software Engineering Group of JetBrains Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ml_methods_group.refactoring.logging;

import com.sixrr.metrics.Metric;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.stockmetrics.methodMetrics.*;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.refactoring.MoveFieldRefactoring;
import org.ml_methods_group.algorithm.refactoring.MoveMethodRefactoring;
import org.ml_methods_group.algorithm.refactoring.Refactoring;
import org.ml_methods_group.algorithm.refactoring.RefactoringVisitor;

import java.util.*;

/**
 * This class contains features extracted from some {@link Refactoring}. This features should
 * characterize {@link Refactoring} in a way that helps to extract user refactorings preferences
 * from features of accepted and rejected refactorings.
 */
public abstract class RefactoringFeatures {
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
     * Returns {@link Set} of {@link Metric}s that are required to construct
     * {@link RefactoringFeatures}. This returned metrics are supposed to be calculated during
     * common metrics calculation process and passed to {@link #extractFeatures} method when
     * features extraction is required.
     */
    public static @NotNull Set<Class<? extends Metric>> getRequestedMetrics() {
        return new HashSet<>(Arrays.asList(
            FormalParametersCountMethodMetric.class,
            LinesOfCodeMethodMetric.class,
            NumAssertsMetric.class,
            NumLoopsMetric.class,
            NumLocalVarsMetric.class,
            IsStaticMethodMetric.class,
            IsPrivateMethodMetric.class,
            NameLenMethodMetric.class,
            NumExceptionsThrownMetric.class
        ));
    }

    protected static List<MetricCalculationResult> extractMetricsResultsFor(
        final @NotNull String measuredObject,
        final @NotNull MetricsResult results
    ) {
        List<MetricCalculationResult> extractedResults = new ArrayList<>();

        Metric[] metrics = results.getMetrics();

        for (Metric metric : metrics) {
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
