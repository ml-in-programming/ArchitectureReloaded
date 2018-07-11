/*
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
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

package org.ml_methods_group.algorithm.entity;

import com.sixrr.metrics.Metric;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRun;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import static com.sixrr.metrics.MetricCategory.Class;

class VectorCalculator {
    private final Map<Class<? extends Metric>, Integer> metricsDependencies = new HashMap<>();
    private final Map<Function<RelevantProperties, ? extends Number>, Integer> propertiesDependencies = new HashMap<>();
    private final Map<Integer, Double> constValues = new HashMap<>();
    private int dimension;

    VectorCalculator addMetricDependence(Class<? extends Metric> metric) {
        metricsDependencies.put(metric, dimension++);
        return this;
    }

    VectorCalculator addPropertyDependence(Function<RelevantProperties, ? extends Number> extractor) {
        propertiesDependencies.put(extractor, dimension++);
        return this;
    }

    VectorCalculator addConstValue(double value) {
        constValues.put(dimension++, value);
        return this;
    }

    double[] calculateVector(MetricsRun metricsRun, OldEntity entity) {
        final double[] vector = new double[dimension];
        applyMetricsDependencies(metricsRun.getResultsForCategory(entity.getCategory()), entity.getName(), vector);
        if (entity.getCategory() != Class) {
            applyMetricsDependencies(metricsRun.getResultsForCategory(Class), entity.getClassName(), vector);
        }
        if (!propertiesDependencies.isEmpty()) {
            applyPropertiesDependencies(entity.getRelevantProperties(), vector);
        }
        for (Entry<Integer, Double> entry : constValues.entrySet()) {
            vector[entry.getKey()] = entry.getValue();
        }
        return vector;
    }

    int getDimension() {
        return dimension;
    }

    Set<Class<? extends Metric>> getRequestedMetrics() {
        return metricsDependencies.keySet();
    }

    private void applyMetricsDependencies(MetricsResult result, String unit, double[] vector) {
        for (Metric metric : result.getMetrics()) {
            if (metricsDependencies.containsKey(metric.getClass())) {
                vector[metricsDependencies.get(metric.getClass())] = safeCast(result.getValueForMetric(metric, unit));
            }
        }
    }

    private void applyPropertiesDependencies(RelevantProperties properties, double[] vector) {
        for (Entry<Function<RelevantProperties, ? extends Number>, Integer> entry : propertiesDependencies.entrySet()) {
            vector[entry.getValue()] = entry.getKey().apply(properties).doubleValue();
        }
    }

    private double safeCast(Double d) {
        return d == null ? 0 : d;
    }
}
