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
import org.ml_methods_group.utils.QMoveUtil;

import java.util.Set;

public class QMoveClassEntity extends ClassEntity {
    protected QMoveRelevantProperties properties = new QMoveRelevantProperties();
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

    private double numOfAllMethods;
    private PsiClass psiClass;


    public Set<QMoveClassEntity> getInheritors(){
        return properties.getInheritors();
    }

    QMoveRelevantProperties getProperties(){
        return properties;
    }

    private static final VectorCalculator QMOOD_CALCULATOR = new VectorCalculator()
            .addMetricDependence(NumMethodsClassMetric.class) //Complexity 0
            .addMetricDependence(NumOperationsInheritedMetric.class)  //Inheritance 1
            .addMetricDependence(NumOperationsOverriddenMetric.class)  //Polymorphism 2
            .addMetricDependence(NumAncestorsClassMetric.class) //Abstraction 3
            .addMetricDependence(IsRootOfHierarchyClassMetric.class) //Hierarchies 4
            .addMetricDependence(DataAccessClassMetric.class) //Encapsulation 5
            .addMetricDependence(NumPublicMethodsClassMetric.class); //Messaging 6
           // .addMetricDependence(CohesionAmongMethodsOfClassMetric.class) //Cohesion 8
    //.addMetricDependence(DirectClassCouplingMetric.class) //Coupling 9
            //.addMetricDependence(MeasureOfAggregationClassMetric.class); //Composition 10


    QMoveClassEntity(PsiClass psiClass) {
        super(psiClass);
        this.psiClass = psiClass;
    }

    @Override
    void calculateVector(MetricsRun metricsRun) {
        double[] vector = QMOOD_CALCULATOR.calculateVector(metricsRun, this);
        complexity = vector[0];
        inheritance = vector[1];
        polymorphism = vector[2];
        abstraction = vector[3];
        hierarchies = vector[4];
        encapsulation = vector[5];
        messaging = vector[6];

        numOfAllMethods = inheritance + complexity;

        coupling = properties.getRelatedClasses().size();

        cohesion = properties.getSumIntersection() != 0 ? properties.getSumIntersection() /
                (properties.getParametersOfMethods().size() * properties.getNumOfMethods()) : 0;

        composition = properties.getNumUserDefinedClasses();
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

    public double getComplexity() {
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
        if (numOfAllMethods == 0) {
            return 0;
        }
        return inheritance / numOfAllMethods;
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

    public double recalculateCohesion(QMoveMethodEntity entity, boolean add) {
        int numMethods = properties.getNumOfMethods();
        double sumIntersection = properties.getSumIntersection();
        int union;
        if (add) {
            numMethods++;
            sumIntersection += entity.getProperties().getSumIntersection();
            union = QMoveUtil.addToUnion(properties.getParametersOfMethods(),
                    entity.getProperties().getParametersOfMethods());
        } else {
            numMethods--;
            sumIntersection -= entity.getProperties().getSumIntersection();
            union = QMoveUtil.removeFromUnion(properties.getParametersOfMethods(),
                    entity.getProperties().getParametersOfMethods());
        }
        if (union == 0) {
            return 0;
        }
        return sumIntersection / (numMethods * union);
    }

    public double recalculateCoupling(QMoveMethodEntity entity, boolean add) {
        if (add) {
            return QMoveUtil.addToUnion(properties.getRelatedClasses(),
                    entity.getProperties().getRelatedClasses());
        } else {
            return QMoveUtil.removeFromUnion(properties.getRelatedClasses(),
                    entity.getProperties().getRelatedClasses());
        }
    }

    public double recalculateInheritance(boolean increment) {
        if (increment) {
            return (inheritance + 1) / (numOfAllMethods + 1);
        } else {
            return (inheritance - 1) / (numOfAllMethods - 1);
        }
    }


}
