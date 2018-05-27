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

package org.ml_methods_group.algorithm;


import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.algorithm.entity.QMoveClassEntity;
import org.ml_methods_group.algorithm.entity.QMoveEntitySearchResult;
import org.ml_methods_group.algorithm.entity.QMoveMethodEntity;
import org.ml_methods_group.utils.AlgorithmsUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class QMove extends Algorithm {
    private List<QMoveMethodEntity> methodEntities = new ArrayList<>();
    private final List<QMoveClassEntity> classes = new ArrayList<>();
    private final Map<String, QMoveClassEntity> qMoveClassesByName = new HashMap<>();
    private QMoveMetric metric = new QMoveMetric();
    private ExecutionContext context;
    private AtomicInteger progress = new AtomicInteger();
    private long start;

    public QMove() {
        super("QMove", true);
    }

    @Override
    protected List<Refactoring> calculateRefactorings(
            ExecutionContext context, boolean enableFieldRefactorings) throws Exception {
        this.context = context;
        QMoveEntitySearchResult searchResult = (QMoveEntitySearchResult) context.getEntities();
        methodEntities.clear();
        classes.clear();
        progress.set(0);
        Stream.of(searchResult.getMethods())
                .flatMap(List::stream)
                .filter(Entity::isMovable)
                .map(x -> (QMoveMethodEntity) x)
                .forEach(methodEntities::add);
        searchResult.getClasses()
                .stream()
                .map(x -> (QMoveClassEntity) x)
                .peek(entity -> qMoveClassesByName.put(entity.getName(), entity))
                .forEach(classes::add);
        calculateMetrics();
        System.err.println("started");
        start = System.currentTimeMillis();
       /* List<Refactoring> refactorings = new ArrayList<>();
        for (QMoveMethodEntity methodEntity : methodEntities) {
            findBestMoveForMethod(methodEntity, refactorings);
        }*/
        return runParallel(methodEntities, context, ArrayList::new,
               this::findBestMoveForMethod, AlgorithmsUtil::combineLists);
       // return refactorings;
    }


    private List<Refactoring> findBestMoveForMethod(QMoveMethodEntity method,
                                                    List<Refactoring> refactorings) {
        reportProgress((double) progress.incrementAndGet() / methodEntities.size(), context);
        QMoveMetric copy = new QMoveMetric(metric);
        context.checkCanceled();
        ResultHolder resultHolder = new ResultHolder(null, 0);
        start = System.currentTimeMillis();
        if (method.getMoveAbility() == QMoveMethodEntity.MoveAbility.ANY_CLASS) {
            System.err.println(method.getName());
            resultHolder = checkMoveForClasses(method, classes, copy);

        } else {
            for (Set<QMoveClassEntity> targets : method.getTargets()) {
                ResultHolder holder = checkMoveForClasses(method, targets, copy);
                if (resultHolder.fitness < holder.fitness) {
                    resultHolder = holder;
                }
            }
        }
        System.err.println(System.currentTimeMillis() - start);
        if (resultHolder.target != null) {
            refactorings.add(new Refactoring(method.getName(),
                    resultHolder.target.getName(), resultHolder.fitness,
                    false));
        }
        return refactorings;
    }

    private ResultHolder checkMoveForClasses(QMoveMethodEntity method,
                                             Collection<QMoveClassEntity> classes, QMoveMetric metricCopy) {
        double bestFitness = 0;
        QMoveClassEntity targetForThisMethod = null;
        for (QMoveClassEntity targetClass : classes) {
            QMoveClassEntity containingClass = qMoveClassesByName.get(
                    method.getClassName());
            if (containingClass.equals(targetClass) ||
                    !method.isValidMoveToClass(targetClass)) {
                continue;
            }
            double newFitness = moveMethod(method, targetClass, containingClass, metricCopy);
            if (newFitness > bestFitness) {
                bestFitness = newFitness;
                targetForThisMethod = targetClass;
            }
        }
        return new ResultHolder(targetForThisMethod, bestFitness);
    }


    private void calculateMetrics() {
        metric.size = classes.size();
        metric.complexity = methodEntities.size();
        metric.polymorphism = classes.stream().mapToDouble(QMoveClassEntity::getPolymorphism).sum();
        metric.abstraction = classes.stream().mapToDouble(QMoveClassEntity::getAbstraction).sum() / metric.size;
        metric.hierarchies = classes.stream().mapToDouble(QMoveClassEntity::getHierarchies).sum();
        metric.encapsulation = classes.stream().mapToDouble(QMoveClassEntity::getEncapsulation).sum();
        metric.coupling = classes.stream().mapToDouble(QMoveClassEntity::getCoupling).sum();
        metric.messaging = classes.stream().mapToDouble(QMoveClassEntity::getMessaging).sum();
        metric.cohesion = classes.stream().mapToDouble(QMoveClassEntity::getCohesion).sum();
        metric.composition = classes.stream().mapToDouble(QMoveClassEntity::getComposition).sum();
        metric.inheritance = classes.stream().mapToDouble(QMoveClassEntity::getInheritance).sum();
    }

    private double moveMethod(QMoveMethodEntity method,
                              QMoveClassEntity target,
                              QMoveClassEntity containingClass, QMoveMetric copyMetric) {
        //recalculating cohesion
        double removeFromCohesion = target.getCohesion();
        removeFromCohesion += containingClass.getCohesion();
        double addToCohesion = target.recalculateCohesion(method, true);
        addToCohesion += containingClass.recalculateCohesion(method, false);
        double oldCohesion = copyMetric.cohesion;
        copyMetric.cohesion -= removeFromCohesion;
        copyMetric.cohesion += addToCohesion;

        //recalculating coupling
        double removeFromCoupling = target.getCoupling();
        removeFromCoupling += containingClass.getCoupling();
        double addToCoupling = target.recalculateCoupling(method, true);
        addToCoupling += containingClass.recalculateCoupling(method, false);
        double oldCoupling = copyMetric.coupling;
        copyMetric.coupling -= removeFromCoupling;
        copyMetric.coupling += addToCoupling;
        if(metric.coupling != oldCoupling){
            System.err.println("wtf");
        }
        //recalculating inheritance
        double oldInheritance = copyMetric.inheritance;
        for (QMoveClassEntity entity : target.getInheritors()) {
            copyMetric.inheritance -= entity.getInheritance();
            copyMetric.inheritance += entity.recalculateInheritance(true);
        }
        for (QMoveClassEntity entity : containingClass.getInheritors()) {
            copyMetric.inheritance -= entity.getInheritance();
            copyMetric.inheritance += entity.recalculateInheritance(false);
        }
        double fitness = copyMetric.fitness(this.metric);
        copyMetric.cohesion = oldCohesion;
        copyMetric.coupling = oldCoupling;
        copyMetric.inheritance = oldInheritance;
        return fitness;
    }

    private static class QMoveMetric {
        private QMoveMetric() {
        }

        private QMoveMetric(QMoveMetric other) {
            cohesion = other.cohesion;
            complexity = other.complexity;
            coupling = other.coupling;
            encapsulation = other.encapsulation;
            polymorphism = other.polymorphism;
            abstraction = other.abstraction;
            hierarchies = other.hierarchies;
            messaging = other.messaging;
            composition = other.composition;
            inheritance = other.inheritance;
            size = other.size;
        }

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
        private double size;

        private double effectiveness() {
            return 0.2 * abstraction + 0.2 * encapsulation + 0.2 * composition
                    + 0.2 * inheritance + 0.2 * polymorphism;
        }

        private double extendibility() {
            return 0.5 * abstraction - 0.5 * coupling + 0.5 * inheritance
                    + 0.5 * polymorphism;
        }

        private double functionality() {
            return +0.12 * cohesion + 0.22 * polymorphism + 0.22 * messaging
                    + 0.22 * size + 0.22 * hierarchies;
        }

        private double understandability() {
            return 0.33 * abstraction + 0.33 * encapsulation - 0.33 * coupling
                    + 0.33 * cohesion - 0.33 * polymorphism - 0.33 * complexity
                    - 0.33 * size;
        }

        private double reusability() {
            return -0.25 * coupling + 0.25 * cohesion + 0.5 * messaging
                    + 0.5 * size;
        }

        private double flexibility() {
            return 0.25 * encapsulation - 0.25 * coupling + 0.5 * composition
                    + 0.5 * polymorphism;
        }

        private double fitness(QMoveMetric other) {
            if (flexibility() < other.flexibility() ||
                    understandability() < other.understandability()
                    || extendibility() < other.extendibility()) {
                return Double.NEGATIVE_INFINITY;
            }
            return flexibility() + understandability() + extendibility()
                    - other.extendibility() - other.flexibility() - other.understandability();
        }

    }

    private static class ResultHolder {
        private ResultHolder(QMoveClassEntity target, double fitness) {
            this.fitness = fitness;
            this.target = target;
        }

        private QMoveClassEntity target;
        private double fitness;
    }
}
