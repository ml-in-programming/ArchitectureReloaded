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

package org.ml_methods_group.algorithm.attributes;

import com.sixrr.metrics.Metric;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRun;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.Algorithm;
import org.ml_methods_group.algorithm.entity.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One object of this class must be created per {@link Algorithm} execution. This class stores all
 * relevant code entities (classes, methods, ...) along with their
 * {@link org.ml_methods_group.algorithm.entity.RelevantProperties} and features extracted
 * specially for one particular {@link Algorithm} from metrics this algorithm requested through
 * {@link Algorithm#requiredMetrics}.
 */
public class AttributesStorage {
    private final @NotNull List<ClassAttributes> classesAttributes;

    private final @NotNull List<MethodAttributes> methodsAttributes;

    private final @NotNull List<FieldAttributes> fieldsAttributes;

    /**
     * Creates storage for attributes.
     *
     * @param entities entities attributes will be constructed from.
     * @param metricClasses metrics that must be used to create features vectors.
     * @param metricsRun result of all metrics calculations.
     * @throws NoRequestedMetricException if there are some missing metric calculation results.
     */
    public AttributesStorage(
        final @NotNull EntitiesStorage entities,
        final @NotNull List<Class<? extends Metric>> metricClasses,
        final @NotNull MetricsRun metricsRun
    ) throws NoRequestedMetricException {
        List<Metric> metrics = new ArrayList<>();
        for (Class<? extends Metric> metricClass : metricClasses) {
            boolean isMetricPresent = false;
            for (Metric metric : metricsRun.getMetrics()) {
                if (metric.getClass().equals(metricClass)) {
                    metrics.add(metric);
                    isMetricPresent = true;
                    break;
                }
            }

            if (!isMetricPresent) {
                throw new NoRequestedMetricException(
                    "Requested metric '" +
                    metricClass.getCanonicalName() +
                    "' cannot be found in given MetricsRun"
                );
            }
        }

        classesAttributes = new ArrayList<>();
        for (ClassEntity classEntity : entities.getClasses()) {
            classesAttributes.add(
                new ClassAttributes(
                    classEntity,
                    extractFeatures(classEntity, metrics, metricsRun)
                )
            );
        }

        methodsAttributes = new ArrayList<>();
        for (MethodEntity methodEntity : entities.getMethods()) {
            methodsAttributes.add(
                new MethodAttributes(
                    methodEntity,
                    extractFeatures(methodEntity, metrics, metricsRun)
                )
            );
        }

        fieldsAttributes = new ArrayList<>();
        for (FieldEntity fieldEntity : entities.getFields()) {
            fieldsAttributes.add(new FieldAttributes(fieldEntity, new double[0]));
        }
    }

    public @NotNull List<ClassAttributes> getClassesAttributes() {
        return Collections.unmodifiableList(classesAttributes);
    }

    public @NotNull List<MethodAttributes> getMethodsAttributes() {
        return Collections.unmodifiableList(methodsAttributes);
    }
    public @NotNull List<FieldAttributes> getFieldsAttributes() {
        return Collections.unmodifiableList(fieldsAttributes);
    }

    private @NotNull double[] extractFeatures(
        final @NotNull CodeEntity entity,
        final @NotNull List<Metric> metrics,
        final @NotNull MetricsRun metricsRun
    ) throws NoRequestedMetricException {
        MetricsResult result = metricsRun.getResultsForCategory(entity.getMetricCategory());

        final int dimension =
            (int) metrics.stream()
                .filter(it -> it.getCategory().equals(entity.getMetricCategory()))
                .count();

        double[] features = new double[dimension];

        int pointer = 0;
        for (Metric metric : metrics) {
            if (metric.getCategory().equals(entity.getMetricCategory())) {
                Double metricValue = result.getValueForMetric(metric, entity.getIdentifier());
                if (metricValue != null) {
                    features[pointer++] = metricValue;
                } else {
                    throw new NoRequestedMetricException(
                        "Requested metric '" +
                        metric.getID() +
                        "' is not computed for object '" +
                        entity.getIdentifier() +
                        "'"
                    );
                }
            }
        }

        return features;
    }
}
