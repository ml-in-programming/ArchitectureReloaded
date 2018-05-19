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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class QMove extends Algorithm {
    private List<QMoveMethodEntity> methodEntities = new ArrayList<>();
    private final List<QMoveClassEntity> classes = new ArrayList<>();
    private final Map<String, QMoveClassEntity> qMoveClassesByName = new HashMap<>();
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
    private QMoveEntitySearchResult searchResult;
    public QMove() {
        super("QMove", true);
    }

    @Override
    protected List<Refactoring> calculateRefactorings(
            ExecutionContext context, boolean enableFieldRefactorings) throws Exception {
        searchResult = (QMoveEntitySearchResult)context.getEntities();
        methodEntities.clear();
        classes.clear();
        Stream.of(searchResult.getMethods())
                .flatMap(List::stream)
                .filter(Entity::isMovable)
                .map(x -> (QMoveMethodEntity)x)
                .forEach(methodEntities::add);

        searchResult.getClasses()
                .stream()
                .map(x -> (QMoveClassEntity)x)
                .peek(entity -> qMoveClassesByName.put(entity.getName(), entity))
                .forEach(classes::add);
        calculateMetrics();
        ArrayList<Refactoring> refactorings = new ArrayList<>();

        for(QMoveMethodEntity methodEntity : methodEntities){
            findBestMoveForMethod(methodEntity, refactorings);
        }
        for(Refactoring refactoring : refactorings){
            System.err.println(refactoring.getTarget() +
                    " " + refactoring.getUnit() + " " + refactoring.getAccuracy());
        }
        return refactorings;
    }



    private List<Refactoring> findBestMoveForMethod(QMoveMethodEntity method,
                                                    List<Refactoring> refactorings){
        double initFitness = fitness();
        double bestFitness = initFitness;
        QMoveClassEntity targetForThisMethod = null;
        for(QMoveClassEntity targetClass : classes){
            QMoveClassEntity containingClass = qMoveClassesByName.get(
                    method.getClassName());
            if(containingClass.equals(targetClass)){
                continue;
            }
            moveMethod(method, targetClass, containingClass);
            double newFitness = fitness();
            if(newFitness > bestFitness){
                bestFitness = newFitness;
                targetForThisMethod =  targetClass;
            }
            moveMethod(method, containingClass, targetClass);
            double fitness = fitness();
            if(fitness != initFitness){
                System.err.println("ERROR");
            }
        }
        if(targetForThisMethod != null){
            refactorings.add(new Refactoring(method.getName(),
                    targetForThisMethod.getName(), bestFitness / fitness() - 1, false));
        }
        return refactorings;
    }


    private void calculateMetrics(){
        size = classes.size();
        complexity = methodEntities.size();
        polymorphism = classes.stream().mapToDouble(QMoveClassEntity::getPolymorphism).sum();
        abstraction = classes.stream().mapToDouble(QMoveClassEntity::getAbstraction).sum() / size;
        hierarchies = classes.stream().mapToDouble(QMoveClassEntity::getHierarchies).sum();
        encapsulation = classes.stream().mapToDouble(QMoveClassEntity::getEncapsulation).sum();
        coupling = classes.stream().mapToDouble(QMoveClassEntity::getCoupling).sum();
        messaging = classes.stream().mapToDouble(QMoveClassEntity::getMessaging).sum();
        cohesion = classes.stream().mapToDouble(QMoveClassEntity::getCohesion).sum();
        composition = classes.stream().mapToDouble(QMoveClassEntity::getComposition).sum();
        inheritance = classes.stream().mapToDouble(QMoveClassEntity::getInheritance).sum();
    }

    private void recalculateMetrics(){
        double coup = coupling;
        coupling = classes.stream().mapToDouble(QMoveClassEntity::getCoupling).sum();
        double mess = messaging;
        messaging = classes.stream().mapToDouble(QMoveClassEntity::getMessaging).sum();
        double coh = cohesion;
        cohesion = classes.stream().mapToDouble(QMoveClassEntity::getCohesion).sum();
        double inh = inheritance;
        inheritance = classes.stream().mapToDouble(QMoveClassEntity::getInheritance).sum();
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

    private double fitness(){
        recalculateMetrics();
        return reusability() + flexibility() + understandability() +
                functionality() + extendibility() + effectiveness();
    }

    private void moveMethod(QMoveMethodEntity method, QMoveClassEntity target, QMoveClassEntity containingClass){
        containingClass.removeMethod(method);
        target.addMethod(method);
        for(QMoveClassEntity entity : classes){
            if(entity.getName().equals(target.getName()) ||
                    entity.getName().equals(containingClass.getName())){
                continue;
            }
            if(entity.getPsiClass().isInheritor(target.getPsiClass(), true)){
                entity.recalculateInheritance(true);
            }
            if(entity.getPsiClass().isInheritor(containingClass.getPsiClass(), true)){
                entity.recalculateInheritance(false);
            }
        }
    }
}
