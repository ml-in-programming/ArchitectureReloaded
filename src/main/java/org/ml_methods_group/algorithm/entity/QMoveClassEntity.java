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

import com.intellij.psi.PsiClass;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.stockmetrics.classMetrics.*;
import com.sixrr.stockmetrics.projectMetrics.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QMoveClassEntity extends ClassEntity {
    private double complexity;
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
            .addMetricDependence(MeasureOfFunctionalAbstractionMetric.class)  //Inheritance 1
            .addMetricDependence(NumPolymorphicMethodsProjectMetric.class)  //Polymorphism 2
            .addMetricDependence(NumAncestorsClassMetric.class) //Abstraction 3
            .addMetricDependence(IsRootOfHierarchyClassMetric.class) //Hierarchies 4
            .addMetricDependence(DataAccessClassMetric.class) //Encapsulation 5
            .addMetricDependence(DirectClassCouplingMetric.class) //Coupling 6
            .addMetricDependence(NumPublicMethodsClassMetric.class) //Messaging 7
            .addMetricDependence(CohesionAmongMethodsOfClassMetric.class) //Cohesion 8
            .addMetricDependence(MeasureOfAggregationClassMetric.class); //Composition 9

    QMoveClassEntity(PsiClass psiClass) {
        super(psiClass);
    }


    @Override
    void calculateVector(MetricsRun metricsRun) {
        double[] vector = QMOOD_CALCULATOR.calculateVector(metricsRun, this);
        complexity = vector[0];
        polymorphism = vector[2];
        abstraction = vector[3];
        hierarchies = vector[4];
        encapsulation = vector[5];
        coupling = vector[6];
        messaging = vector[7];
        cohesion = vector[8];
        composition = vector[9];
        inheritance = vector[1];
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

    @Override
    public void removeFromClass(String method) {
        getRelevantProperties().removeMethod(method);
    }

    public void addToClass(String method) {
        getRelevantProperties().addMethod(method);
    }

    public double getComplexity(){
        return complexity;
    }

    public double getAbstraction() {
        return abstraction;
    }

    public double getCohesion() {
        return cohesion;
    }

    public double getCoupling() {
        return coupling;
    }

    public double getHierarchies() {
        return hierarchies;
    }

    public double getComposition() {
        return composition;
    }

    public double getEncapsulation() {
        return encapsulation;
    }

    public double getInheritance() {
        return inheritance;
    }

    public double getMessaging() {
        return messaging;
    }

    public double getPolymorphism() {
        return polymorphism;
    }

    public static Set<Class<? extends Metric>> getRequestedMetrics() {
        return QMOOD_CALCULATOR.getRequestedMetrics();
    }

    public void addMethod(String method){
        getRelevantProperties().addMethod(method);
        complexity++;
        recalculateCoupling();
        recalculateCohesion();
    }

    public void removeMethod(String method){
        getRelevantProperties().removeMethod(method);
        complexity--;
        recalculateCoupling();
        recalculateCohesion();
    }

    private void recalculateCoupling() {
        Set<String> relatedClasses = new HashSet<>();
        relatedClasses.addAll(
                getRelevantProperties().getFields().stream().map(
                        this::extractClassnameFromField)
                        .filter(x -> !Objects.equals(x, "")).
                        collect(Collectors.toSet()));
        relatedClasses.addAll(
                getRelevantProperties().getMethods().stream()
                        .flatMap(this::extractParametersFromMethod)
                        .filter(x -> !Objects.equals(x, ""))
                .collect(Collectors.toSet()));
        coupling = relatedClasses.size();
    }

    private void recalculateCohesion(){
        int totalSize =
                getRelevantProperties().getMethods().stream()
                        .flatMap(this::extractParametersFromMethod).collect(
                                Collectors.toSet()
                ).size();
        double sum = getRelevantProperties().getMethods().stream()
                .mapToDouble(x -> extractParametersFromMethod(x).collect(
                        Collectors.toSet()
                ).size()).sum();
        cohesion = totalSize == 0 ? 0 : sum / (totalSize *
                getRelevantProperties().getMethods().size());
    }

    private String extractClassnameFromField(String s){
        String[] strings = s.split("\\.");
        assert strings.length > 1;
        return strings[strings.length - 2];
    }

    @NotNull
    private Stream<String> extractParametersFromMethod(String s){
        String params = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
        return Stream.of(params.split(","));
    }
}
