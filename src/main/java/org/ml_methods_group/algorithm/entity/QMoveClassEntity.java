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

import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.stockmetrics.classMetrics.*;
import org.ml_methods_group.utils.PsiSearchUtil;
import org.ml_methods_group.utils.QMoveUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QMoveClassEntity extends ClassEntity {
    static Set<PsiClass> userDefinedClasses = new HashSet<>();
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

    private int numOfAllMethods;
    private PsiClass psiClass;
    private Map<String, PsiMethod> methods;
    private Map<PsiClass, Integer> relatedClasses = new HashMap<>();

    private static final VectorCalculator QMOOD_CALCULATOR = new VectorCalculator()
            .addMetricDependence(NumMethodsClassMetric.class) //Complexity 0
            .addMetricDependence(NumOperationsInheritedMetric.class)  //Inheritance 1
            .addMetricDependence(NumOperationsOverriddenMetric.class)  //Polymorphism 2
            .addMetricDependence(NumAncestorsClassMetric.class) //Abstraction 3
            .addMetricDependence(IsRootOfHierarchyClassMetric.class) //Hierarchies 4
            .addMetricDependence(DataAccessClassMetric.class) //Encapsulation 5
            //.addMetricDependence(DirectClassCouplingMetric.class) //Coupling 6
            .addMetricDependence(NumPublicMethodsClassMetric.class) //Messaging 7
            .addMetricDependence(CohesionAmongMethodsOfClassMetric.class) //Cohesion 8
            .addMetricDependence(MeasureOfAggregationClassMetric.class); //Composition 9

    QMoveClassEntity(PsiClass psiClass) {
        super(psiClass);
        this.psiClass = psiClass;
        methods = Stream.of(psiClass.getMethods())
                .collect(Collectors.toMap(PsiSearchUtil::getHumanReadableName, Function.identity()));
    }

    @Override
    void calculateVector(MetricsRun metricsRun) {
        double[] vector = QMOOD_CALCULATOR.calculateVector(metricsRun, this);
        complexity = vector[0];
        polymorphism = vector[2];
        abstraction = vector[3];
        hierarchies = vector[4];
        encapsulation = vector[5];
        messaging = vector[6];
        cohesion = vector[7];
        composition = vector[8];
        inheritance = vector[1];
        numOfAllMethods = psiClass.getAllMethods().length;
        coupling = calculateRelatedClasses();
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
        if(numOfAllMethods == 0){
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

    public void addMethod(QMoveMethodEntity entity) {
        PsiMethod method = entity.getPsiMethod();
        methods.put(entity.getName(), method);
        complexity++;
        if(method.hasModifierProperty(PsiModifier.PUBLIC)){
            messaging++;
        }
        recalculateCoupling(entity, true);
        recalculateCohesion();
        numOfAllMethods++;
    }

    public void removeMethod(QMoveMethodEntity methodEntity){
        methods.remove(methodEntity.getName());
        complexity--;
        PsiMethod method = methodEntity.getPsiMethod();
        if(method.hasModifierProperty(PsiModifier.PUBLIC)){
            messaging--;
        }
        recalculateCoupling(methodEntity, false);
        recalculateCohesion();
        numOfAllMethods--;
    }

    private void recalculateCoupling(QMoveMethodEntity entity, boolean add) {
        if(add){
            QMoveUtil.addToUnion(relatedClasses, entity.getRelatedClasses());
        }
        else {
            QMoveUtil.removeFromUnion(relatedClasses, entity.getRelatedClasses());
        }
        coupling = relatedClasses.size();
    }

    private int calculateRelatedClasses() {
            PsiField[] fields = psiClass.getFields();
            for (PsiField field : fields) {
                if (!field.isPhysical()) {
                    continue;
                }
                PsiType type = field.getType().getDeepComponentType();
                PsiClass classInType = PsiUtil.resolveClassInType(type);
                if (classInType == null) {
                    continue;
                }
                relatedClasses.put(classInType,
                        relatedClasses.getOrDefault(classInType, 0));
            }
            for (Map.Entry<String, PsiMethod> method : methods.entrySet()) {
                QMoveUtil.calculateRelatedClasses(method.getValue(), relatedClasses);
            }
            return relatedClasses.size();
    }


    private void recalculateCohesion(){
        double sumIntersection = 0;
        int numMethods = 0;
        Set<PsiType> parameters = new HashSet<>();
        for(Map.Entry<String, PsiMethod> methodEntry : methods.entrySet()){
            PsiMethod method = methodEntry.getValue();
            if(method.isConstructor() || method.hasModifierProperty(PsiModifier.STATIC)){
                continue;
            }
            numMethods++;
            Set<PsiType> parametersInMethod = Stream.of(method.getParameterList()).
                    flatMap(x -> Stream.of(x.getParameters())).
                    map(PsiVariable::getType).collect(Collectors.toSet());
            sumIntersection += parametersInMethod.size();
            parameters.addAll(parametersInMethod);
        }
        if(parameters.size() == 0){
            return;
        }
        cohesion = sumIntersection / numMethods * parameters.size();
    }

    public void recalculateInheritance(boolean increment){
        if(increment){
            inheritance++;
            numOfAllMethods++;
        }
        else{
            inheritance--;
            numOfAllMethods--;
        }
    }

    public PsiClass getPsiClass() {
        return psiClass;
    }
}
