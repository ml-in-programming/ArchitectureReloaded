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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.stockmetrics.classMetrics.DepthOfInheritanceMetric;
import com.sixrr.stockmetrics.classMetrics.FanInClassMetric;
import com.sixrr.stockmetrics.classMetrics.FanOutClassMetric;
import com.sixrr.stockmetrics.classMetrics.NumChildrenMetric;
import com.sixrr.stockmetrics.methodMetrics.FanInMethodMetric;
import com.sixrr.stockmetrics.methodMetrics.FanOutMethodMetric;
import org.ml_methods_group.algorithm.PropertiesFinder;
import org.ml_methods_group.algorithm.RelevantProperties;

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

    private final PsiElement psiEntity;
    private double[] vector;
    private final RelevantProperties relevantProperties;
    private final String name;



    public Entity(String name, MetricsRun metricsRun, PropertiesFinder propertiesFinder) {
        this.name = name;
        vector = getCalculatorForEntity().calculateVector(metricsRun, propertiesFinder, this);
        relevantProperties = propertiesFinder.getProperties(this.name);
        psiEntity = propertiesFinder.getPsiElement(name);
    }

    private double square(double value) {
        return value * value;
    }

    public double distance(Entity entity) {
        double ans = 0.0;
//        for (int i = 0; i < DIMENSION; i++) {
//            ans += square(vector[i] - entity.vector[i]);
//        }

        final int rpIntersect = entity.relevantProperties.sizeOfIntersect(relevantProperties);
        if (rpIntersect == 0) {
            return Double.MAX_VALUE;
        }
        ans += 2.0 * (1 - rpIntersect /
                (1.0 * relevantProperties.size() + entity.relevantProperties.size() - rpIntersect));

//        ans /= DIMENSION + 2;
        return Math.sqrt(ans);
    }

    public static void normalize(Iterable<Entity> entities) {
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

    public void moveToClass(PsiClass newClass) {
        final Set<PsiClass> oldClasses = relevantProperties.getAllClasses();
        for (PsiClass oldClass : oldClasses) {
            relevantProperties.removeClass(oldClass);
        }
        relevantProperties.addClass(newClass);
    }

    public void removeFromClass(PsiMethod method) {
        relevantProperties.removeMethod(method);
    }

    public void removeFromClass(PsiField field) {
        relevantProperties.removeField(field);
    }

    public RelevantProperties getRelevantProperties() {
        return relevantProperties;
    }

    public double[] getVector() {
        return vector;
    }

    public String getName() {
        return name;
    }

    public void print() {
        System.out.println(name + ": " + getCategory().name());
        System.out.print("    ");
        for (double comp : vector) {
            System.out.print(comp);
            System.out.print(" ");
        }
        System.out.println();

        relevantProperties.printAll();
    }

    public PsiElement getPsiElement() {
        return psiEntity;
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

    abstract public MetricCategory getCategory();
    abstract public String getClassName();
}
