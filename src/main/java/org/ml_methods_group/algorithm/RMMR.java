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

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;
import org.ml_methods_group.algorithm.entity.MethodEntity;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.utils.AlgorithmsUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static java.lang.Math.sqrt;

/**
 * Implementation of RMMR (Recommendation of Move Method Refactoring) algorithm.
 * Based on @see <a href="https://drive.google.com/file/d/17yAlVXRaLuhIcXB4PEzNiZj5p1oi4HlL/view">this article</a>.
 */
public class RMMR extends Algorithm {
    private static final boolean IS_PARALLELED = true;
    /**
     * Describes minimal accuracy that algorithm accepts.
     */
    private final static double MIN_ACCURACY = 0.01;
    /**
     * Internal name of the algorithm in the program.
     */
    public static final String NAME = "RMMR";
    private static final Logger LOGGER = Logging.getLogger(RMMR.class);

    /**
     * Map: class -> set of method in this class.
     */
    private final Map<ClassEntity, Set<MethodEntity>> methodsByClass = new HashMap<>();
    /**
     * Methods to check for refactoring.
     */
    private final List<MethodEntity> units = new ArrayList<>();
    /**
     * Classes to which method will be considered for moving.
     */
    private final List<ClassEntity> classEntities = new ArrayList<>();
    private final AtomicInteger progressCount = new AtomicInteger();
    /**
     * Context which stores all found classes, methods and its metrics (by storing Entity).
     */
    private ExecutionContext context;

    public RMMR() {
        super(NAME, true);
    }

    @Override
    @NotNull
    protected List<Refactoring> calculateRefactorings(@NotNull ExecutionContext context, boolean enableFieldRefactorings) {
        if (enableFieldRefactorings) {
            // TODO: write to LOGGER or throw Exception? Change UI: disable field checkbox if only RMMR is chosen.
            LOGGER.error("Field refactorings are not supported",
                    new UnsupportedOperationException("Field refactorings are not supported"));
        }
        this.context = context;
        init();

        if (IS_PARALLELED) {
            return runParallel(units, context, ArrayList::new, this::findRefactoring, AlgorithmsUtil::combineLists);
        } else {
            List<Refactoring> accum = new LinkedList<>();
            units.forEach(methodEntity -> findRefactoring(methodEntity, accum));
            return accum;
        }
    }

    /**
     * Initializes units, methodsByClass, classEntities. Data is gathered from context.getEntities().
     */
    private void init() {
        final EntitySearchResult entities = context.getEntities();
        LOGGER.info("Init RMMR");
        units.clear();
        classEntities.clear();
        methodsByClass.clear();

        classEntities.addAll(entities.getClasses());
        units.addAll(entities.getMethods());
        progressCount.set(0);

        entities.getMethods().forEach(methodEntity -> {
            List<ClassEntity> methodClassEntity = entities.getClasses().stream()
                    .filter(classEntity -> methodEntity.getClassName().equals(classEntity.getName()))
                    .collect(Collectors.toList());
            if (methodClassEntity.size() != 1) {
                LOGGER.error("Found more than 1 class that has this method");
            }
            methodsByClass.computeIfAbsent(methodClassEntity.get(0), anyKey -> new HashSet<>()).add(methodEntity);
        });
    }

    /**
     * Methods decides whether to move method or not, based on calculating distances between given method and classes.
     *
     * @param entity      method to check for move method refactoring.
     * @param accumulator list of refactorings, if method must be moved, refactoring for it will be added to this accumulator.
     * @return changed or unchanged accumulator.
     */
    @NotNull
    private List<Refactoring> findRefactoring(@NotNull MethodEntity entity, @NotNull List<Refactoring> accumulator) {
        reportProgress((double) progressCount.incrementAndGet() / units.size(), context);
        context.checkCanceled();
        if (!entity.isMovable() || classEntities.size() < 2) {
            return accumulator;
        }
        double minDistance = Double.POSITIVE_INFINITY;
        double difference = Double.POSITIVE_INFINITY;
        double distanceWithSourceClass = 1;
        ClassEntity targetClass = null;
        ClassEntity sourceClass = null;
        for (final ClassEntity classEntity : classEntities) {
            final double contextualDistance = classEntity.getContextualVector().size() == 0 ? 1 : getContextualDistance(entity, classEntity);
            final double distance = 0.5 * getDistance(entity, classEntity) + 0.5 * contextualDistance;
            if (classEntity.getName().equals(entity.getClassName())) {
                sourceClass = classEntity;
                distanceWithSourceClass = distance;
            }
            if (distance < minDistance) {
                difference = minDistance - distance;
                minDistance = distance;
                targetClass = classEntity;
            } else if (distance - minDistance < difference) {
                difference = distance - minDistance;
            }
        }

        if (targetClass == null) {
            LOGGER.warn("targetClass is null for " + entity.getName());
            return accumulator;
        }
        final String targetClassName = targetClass.getName();
        double differenceWithSourceClass = distanceWithSourceClass - minDistance;
        int numberOfMethodsInSourceClass = methodsByClass.get(sourceClass).size();
        int numberOfMethodsInTargetClass = methodsByClass.getOrDefault(targetClass, Collections.emptySet()).size();
        // considers amount of entities.
        double sourceClassCoefficient = max(1 - 1.0 / (2 * numberOfMethodsInSourceClass * numberOfMethodsInSourceClass), 0);
        double targetClassCoefficient = max(1 - 1.0 / (4 * numberOfMethodsInTargetClass * numberOfMethodsInTargetClass), 0);
        double differenceWithSourceClassCoefficient = (1 - minDistance) * differenceWithSourceClass;
        double powerCoefficient = max(1 - 1.0 / (2 * entity.getRelevantProperties().getClasses().size()), 0);
        double accuracy = (0.5 * distanceWithSourceClass + 0.3 * (1 - minDistance) + 0.2 * differenceWithSourceClass) * powerCoefficient;
        if (entity.getClassName().contains("Util") || entity.getClassName().contains("Factory") ||
                entity.getClassName().contains("Builder")) {
            if (accuracy > 0.7) {
                accuracy /= 2;
            }
        }
        if (entity.getName().contains("main")) {
            accuracy /= 2;
        }
        if (differenceWithSourceClass != 0 && accuracy >= MIN_ACCURACY && !targetClassName.equals(entity.getClassName())) {
            accumulator.add(new Refactoring(entity.getName(), targetClassName, accuracy, entity.isField()));
        }
        return accumulator;
    }

    private double getContextualDistance(@NotNull MethodEntity entity, @NotNull ClassEntity classEntity) {
        Map<String, Double> methodVector = entity.getContextualVector();
        Map<String, Double> classVector = classEntity.getContextualVector();
        double methodVectorNorm = norm(methodVector);
        double classVectorNorm = norm(classVector);
        return methodVectorNorm == 0 || classVectorNorm == 0 ?
                1 : 1 - dotProduct(methodVector, classVector) / (methodVectorNorm * classVectorNorm);
    }

    private double dotProduct(@NotNull Map<String, Double> vector1, @NotNull Map<String, Double> vector2) {
        final double[] productValue = {0};
        vector1.forEach((s, aDouble) -> productValue[0] += aDouble * vector2.getOrDefault(s, 0.0));
        return productValue[0];
    }

    private double norm(@NotNull Map<String, Double> vector) {
        return sqrt(dotProduct(vector, vector));
    }

    /**
     * Measures distance (a number in [0; 1]) between method and a class.
     * It is an average of distances between method and class methods.
     * If there is no methods in a given class then distance is 1.
     * @param methodEntity method to calculate distance.
     * @param classEntity class to calculate distance.
     * @return distance between the method and the class.
     */
    private double getDistance(@NotNull MethodEntity methodEntity, @NotNull ClassEntity classEntity) {
        int number = 0;
        double sumOfDistances = 0;

        if (methodsByClass.containsKey(classEntity)) {
            for (MethodEntity methodEntityInClass : methodsByClass.get(classEntity)) {
                if (!methodEntity.equals(methodEntityInClass)) {
                    sumOfDistances += getDistance(methodEntity, methodEntityInClass);
                    number++;
                }
            }
        }

        return number == 0 ? 1 : sumOfDistances / number;
    }

    /**
     * Measures distance (a number in [0; 1]) between two methods.
     * It is sizeOfIntersection(A1, A2) / sizeOfUnion(A1, A2), where Ai is a conceptual set of method.
     * If A1 and A2 are empty then distance is 1.
     * @param methodEntity1 method to calculate distance.
     * @param methodEntity2 method to calculate distance.
     * @return distance between two given methods.
     */
    private double getDistance(@NotNull MethodEntity methodEntity1, @NotNull MethodEntity methodEntity2) {
        // TODO: Maybe add to methodEntity2 source class where it is located?
        Set<String> method1Classes = methodEntity1.getRelevantProperties().getClasses();
        Set<String> method2Classes = methodEntity2.getRelevantProperties().getClasses();
        int sizeOfIntersection = intersection(method1Classes, method2Classes).size();
        int sizeOfUnion = union(method1Classes, method2Classes).size();
        return (sizeOfUnion == 0) ? 1 : 1 - (double) sizeOfIntersection / sizeOfUnion;
    }

    @NotNull
    private <T> Set<T> intersection(@NotNull Set<T> set1, @NotNull Set<T> set2) {
        Set<T> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        return intersection;
    }

    @NotNull
    private <T> Set<T> union(@NotNull Set<T> set1, @NotNull Set<T> set2) {
        Set<T> union = new HashSet<>(set1);
        union.addAll(set2);
        return union;
    }
}
