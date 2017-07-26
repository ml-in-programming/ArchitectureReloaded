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

import com.intellij.psi.*;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.stockmetrics.classMetrics.DepthOfInheritanceMetric;
import com.sixrr.stockmetrics.classMetrics.FanInClassMetric;
import com.sixrr.stockmetrics.classMetrics.FanOutClassMetric;
import com.sixrr.stockmetrics.classMetrics.NumChildrenMetric;
import com.sixrr.stockmetrics.methodMetrics.FanInMethodMetric;
import com.sixrr.stockmetrics.methodMetrics.FanOutMethodMetric;
import org.ml_methods_group.utils.PsiSearchUtil;

import java.util.*;

public abstract class Entity {
    private static final VectorCalculator CLASS_ENTITY_CALCULATOR = new VectorCalculator()
            .addMetricDependence(DepthOfInheritanceMetric.class)
            .addMetricDependence(NumChildrenMetric.class)
            .addMetricDependence(FanInClassMetric.class)
            .addMetricDependence(FanOutClassMetric.class);

    private static final VectorCalculator METHOD_ENTITY_CALCULATOR = new VectorCalculator()
            .addMetricDependence(DepthOfInheritanceMetric.class)
            .addMetricDependence(NumChildrenMetric.class)
            .addMetricDependence(FanInMethodMetric.class)
            .addMetricDependence(FanOutMethodMetric.class);

    private static final VectorCalculator FIELD_ENTITY_CALCULATOR = new VectorCalculator()
            .addMetricDependence(DepthOfInheritanceMetric.class)
            .addMetricDependence(NumChildrenMetric.class)
            .addPropertyDependence(RelevantProperties::numberOfMethods)
            .addConstValue(0);

    private static final int DIMENSION = CLASS_ENTITY_CALCULATOR.getDimension();

    static {
        assert CLASS_ENTITY_CALCULATOR.getDimension() == DIMENSION;
        assert METHOD_ENTITY_CALCULATOR.getDimension() == DIMENSION;
        assert FIELD_ENTITY_CALCULATOR.getDimension() == DIMENSION;
    }

    private final RelevantProperties relevantProperties;
    private final String name;
    private double[] vector;
    protected boolean isMovable = true;

    public Entity(PsiElement element) {
        this.name = PsiSearchUtil.getHumanReadableName(element);
        relevantProperties = new RelevantProperties();
    }

    void calculateVector(MetricsRun metricsRun) {
        vector = getCalculatorForEntity().calculateVector(metricsRun, this);
    }

    private double square(double value) {
        return value * value;
    }

    public double distance(Entity entity) {
        if (relevantProperties.hasCommonPrivateMember(entity.relevantProperties)) {
            return 0;
        }

        double ans = 0.0;
        for (int i = 0; i < DIMENSION; i++) {
            ans += square(vector[i] - entity.vector[i]);
        }

        final int rpIntersect = entity.relevantProperties.sizeOfIntersection(relevantProperties);
        if (rpIntersect == 0) {
            return Double.POSITIVE_INFINITY;
        }
        ans += 2.0 * (1 - rpIntersect /
                (1.0 * relevantProperties.size() + entity.relevantProperties.size() - rpIntersect));

        ans /= DIMENSION + 2;
        return Math.sqrt(ans);
    }

    static void normalize(Iterable<? extends Entity> entities) {
        for (int i = 0; i < DIMENSION; i++) {
            double mx = 0.0;
            for (Entity entity : entities) {
                mx = Math.max(mx, entity.vector[i]);
            }

            if (mx == 0.0) {
                continue;
            }

            for (Entity entity : entities) {
                entity.vector[i] /= mx;
            }
        }
    }

    public void moveToClass(String newClass) {
        relevantProperties.moveTo(newClass);
    }

    public void removeFromClass(String method) {
        relevantProperties.removeMethod(method);
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
}
