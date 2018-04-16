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


import org.ml_methods_group.algorithm.entity.*;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.utils.AlgorithmsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class QMove extends Algorithm {
    private static final Logger LOGGER = LoggerFactory.getLogger(QMove.class);
    private List<MethodEntity> methodEntities = new ArrayList<>();
    private final List<QMoveClassEntity> classes = new ArrayList<>();
    private final Map<String, QMoveClassEntity> qMoveClassesByName = new HashMap<>();
    private final Map<String, QMoveClassEntity> classesByName = new HashMap<>();
    public QMove() {
        super("Quality-orientated Move Method Refactoring", true);
    }

    @Override
    protected List<Refactoring> calculateRefactorings(
            ExecutionContext context, boolean enableFieldRefactorings) throws Exception {
        System.err.println("algorithm started");
        final QMoveEntitySearchResult searchResult = (QMoveEntitySearchResult)context.getEntities();
        methodEntities.clear();
        classes.clear();
        Stream.of(searchResult.getMethods())
                .flatMap(List::stream)
                .filter(Entity::isMovable)
                .forEach(methodEntities::add);

       searchResult.getqMoveClasses()
                .stream()
                .peek(entity -> qMoveClassesByName.put(entity.getName(), entity))
                .forEach(classes::add);

        runParallel(methodEntities, context, ArrayList::new,
                this::findBestMoveForMethod,
                AlgorithmsUtil::combineLists);
        return null;
    }



    private List<Refactoring> findBestMoveForMethod(MethodEntity method,
                                                         List<Refactoring> refactorings){
        System.err.printf("Find best move for %s\n", method.getName());
        double bestFitness = Double.NEGATIVE_INFINITY;
        QMoveClassEntity targetForThisMethod = null;
        for(QMoveClassEntity targetClass : classes){
            QMoveClassEntity containingClass = qMoveClassesByName.get(
                    method.getClassName());
            containingClass.removeFromClass(method.getName());
            targetClass.addToClass(method.getName());
            double newFitness = targetClass.fitness();
            //System.err.println(newFitness);
            if(newFitness > bestFitness){
                bestFitness = newFitness;
                targetForThisMethod =  targetClass;
            }
            targetClass.removeFromClass(method.getName());
            containingClass.addToClass(method.getName());
        }
        return refactorings;
    }

}
