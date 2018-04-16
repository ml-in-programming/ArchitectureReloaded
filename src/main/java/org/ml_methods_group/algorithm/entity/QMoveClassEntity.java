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

package org.ml_methods_group.algorithm.entity;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.PsiClass;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.stockmetrics.classMetrics.*;
import com.sixrr.stockmetrics.projectMetrics.*;

import java.util.HashSet;
import java.util.Set;

public class QMoveClassEntity extends ClassEntity {
    private double complexity;
    private double size;
    private double polymorphism;
    private double abstraction;
    private double hierarchies;
    private double encapsulation;
    private double coupling;
    private double messaging;
    private double cohesion;
    private double composition;
    private double inheritance;

    private static final VectorCalculator QMOOD_CALCULATOR = new VectorCalculator()
            .addMetricDependence(NumMethodsClassMetric.class) //Complexity 0
            .addMetricDependence(NumClassesProjectMetric.class)  //Size 1
            .addMetricDependence(NumPolymorphicMethodsProjectMetric.class)  //Polymorphism 2
            .addMetricDependence(AverageNumOfAncestorsProjectMetric.class) //Abstraction 3
            .addMetricDependence(NumHierarchiesProjectMetric.class) //Hierarchies 4
            .addMetricDependence(DataAccessClassMetric.class) //Encapsulation 5
            .addMetricDependence(DirectClassCouplingProjectMetric.class) //Coupling 6
            .addMetricDependence(NumPublicMethodsClassMetric.class) //Messaging 7
            .addMetricDependence(CohesionAmongMethodsOfClassMetric.class) //Cohesion 8
            .addMetricDependence(MeasureOfAggregationProjectMetric.class) //Composition 9
            .addMetricDependence(MeasureOfFunctionalAbstractionMetric.class); //Inheritance 10

    QMoveClassEntity(PsiClass psiClass) {
        super(psiClass);
    }


    @Override
    void calculateVector(MetricsRun metricsRun) {
        System.err.println("Calculating vector");
        double[] vector = QMOOD_CALCULATOR.calculateVector(metricsRun, this);
        for(int i = 0; i < 11; i++){
            System.err.printf("%f ", vector[i]);
        }
        System.err.println();
        complexity = vector[0];
        size = vector[1];
        polymorphism = vector[2];
        abstraction = vector[3];
        hierarchies = vector[4];
        encapsulation = vector[5];
        coupling = vector[6];
        messaging = vector[7];
        cohesion = vector[8];
        composition = vector[9];
        inheritance = vector[10];
    }

    @Override
    public MetricCategory getCategory() {
        return MetricCategory.Class;
    }

    @Override
    public String getClassName() {
        return getName();
    }


    @Override
    public boolean isField() {
        return false;
    }

    public void removeFromClass(String method) {
        getRelevantProperties().removeMethod(method);
    }

    public void addToClass(String method) {
        getRelevantProperties().addMethod(method);
    }

    private double reusability() {
        return -0.25 * coupling + 0.25 * cohesion + 0.5 * messaging
                + 0.5 * size;
    }

    private double flexibility() {
        return 0.25 * encapsulation - 0.25 * coupling + 0.5 * composition
                + 0.5 * polymorphism;
    }

    private double understandability() {
        return 0.33 * abstraction + 0.33 * encapsulation - 0.33 * coupling
                + 0.33 * cohesion - 0.33 * polymorphism - 0.33 * complexity
                - 0.33 * size;
    }

    private double functionality() {
        return +0.12 * cohesion + 0.22 * polymorphism + 0.22 * messaging
                + 0.22 * size + 0.22 * hierarchies;
    }

    private double extendibility() {
        return 0.5 * abstraction - 0.5 * coupling + 0.5 * inheritance
                + 0.5 * polymorphism;
    }

    private double effectiveness() {
        return 0.2 * abstraction + 0.2 * encapsulation + 0.2 * composition
                + 0.2 * inheritance + 0.2 * polymorphism;
    }

    public double fitness(){
        //calculateVector();
        return reusability() + flexibility() + understandability() +
                functionality() + extendibility() + effectiveness();
    }


    public static Set<Class<? extends Metric>> getRequestedMetrics() {
        return QMOOD_CALCULATOR.getRequestedMetrics();
    }



}
