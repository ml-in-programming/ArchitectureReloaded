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

import com.intellij.psi.PsiElement;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.stockmetrics.classMetrics.NumAttributesAddedMetric;
import com.sixrr.stockmetrics.classMetrics.NumMethodsClassMetric;
import org.ml_methods_group.utils.PsiSearchUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class Entity {
    private static final VectorCalculator CLASS_ENTITY_CALCULATOR = new VectorCalculator()
            .addMetricDependence(NumMethodsClassMetric.class)
            .addMetricDependence(NumAttributesAddedMetric.class);

    private static final VectorCalculator METHOD_ENTITY_CALCULATOR = new VectorCalculator()
            .addConstValue(0)
            .addConstValue(0);

    private static final VectorCalculator FIELD_ENTITY_CALCULATOR = new VectorCalculator()
            .addConstValue(0)
            .addConstValue(0);

    private static final int DIMENSION = CLASS_ENTITY_CALCULATOR.getDimension();

    static {
        assert CLASS_ENTITY_CALCULATOR.getDimension() == DIMENSION;
        assert METHOD_ENTITY_CALCULATOR.getDimension() == DIMENSION;
        assert FIELD_ENTITY_CALCULATOR.getDimension() == DIMENSION;
    }

    private final RelevantProperties relevantProperties;
    private final String name;
    private final PropertiesStrategy strategy;
    private double[] vector;
    protected boolean isMovable = true;

    public Entity(PsiElement element, PropertiesStrategy strategy) {
        this.name = PsiSearchUtil.getHumanReadableName(element);
        this.strategy = strategy;
        relevantProperties = new RelevantProperties();
    }

    protected Entity(Entity original) {
        relevantProperties = original.relevantProperties.copy();
        name = original.name;
        vector = Arrays.copyOf(original.vector, original.vector.length);
        isMovable = original.isMovable;
        strategy = original.strategy;
    }

    void calculateVector(MetricsRun metricsRun) {
        vector = getCalculatorForEntity().calculateVector(metricsRun, this);
    }

    private double square(double value) {
        return value * value;
    }

    public double distance(Entity entity) {
        return strategy.distanceCalculator.apply(this, entity);
    }

    public RelevantProperties getRelevantProperties() {
        return relevantProperties;
    }

    public String getName() {
        return name;
    }

    private VectorCalculator getCalculatorForEntity() {
        if (getClass() == ClassEntity.class) {
            return CLASS_ENTITY_CALCULATOR;
        } else if (getClass() == MethodEntity.class) {
            return METHOD_ENTITY_CALCULATOR;
        } else if (getClass() == FieldEntity.class) {
            return FIELD_ENTITY_CALCULATOR;
        }
        throw new UnsupportedOperationException("Such type of entity isn't supported: " + getClass());
    }

    public static Set<Class<? extends Metric>> getRequestedMetrics() {
        final Set<Class<? extends Metric>> result = new HashSet<>();
        result.addAll(CLASS_ENTITY_CALCULATOR.getRequestedMetrics());
        result.addAll(METHOD_ENTITY_CALCULATOR.getRequestedMetrics());
        result.addAll(FIELD_ENTITY_CALCULATOR.getRequestedMetrics());
        return result;
    }

    public boolean isMovable() {
        return isMovable;
    }

    abstract public MetricCategory getCategory();

    abstract public String getClassName();

    abstract public Entity copy();
}
