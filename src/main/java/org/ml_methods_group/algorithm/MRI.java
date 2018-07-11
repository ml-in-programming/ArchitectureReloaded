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

package org.ml_methods_group.algorithm;

import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.stockmetrics.classMetrics.NumAttributesAddedMetric;
import com.sixrr.stockmetrics.classMetrics.NumMethodsClassMetric;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.entity.ClassOldEntity;
import org.ml_methods_group.algorithm.entity.OldEntity;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;
import org.ml_methods_group.algorithm.entity.FieldOldEntity;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.utils.AlgorithmsUtil;

import java.util.*;
import java.util.stream.Stream;

public class MRI extends OldAlgorithm {
    private static final Logger LOGGER = Logging.getLogger(MRI.class);
    private static final double ACCURACY = 1;

    private final List<OldEntity> units = new ArrayList<>();
    private final Map<String, ClassOldEntity> classesByName = new HashMap<>();
    private final List<ClassOldEntity> classes = new ArrayList<>();

    public MRI() {
        super("MRI", true);
    }

    @Override
    public @NotNull List<Class<? extends Metric>> requiredMetrics() {
        return Arrays.asList(NumMethodsClassMetric.class, NumAttributesAddedMetric.class);
    }

    @Override
    protected List<Refactoring> calculateRefactorings(OldExecutionContext context, boolean enableFieldRefactorings) {
        final EntitySearchResult searchResult = context.getEntities();
        units.clear();
        classes.clear();
        List<FieldOldEntity> fields = enableFieldRefactorings ? searchResult.getFields() : Collections.emptyList();
        Stream.of(searchResult.getMethods(), fields)
                .flatMap(List::stream)
                .filter(OldEntity::isMovable)
                .forEach(units::add);

        searchResult.getClasses()
                .stream()
                .map(ClassOldEntity::copy) // create local copies
                .peek(entity -> classesByName.put(entity.getName(), entity))
                .forEach(classes::add);

        final List<Refactoring> refactorings = new ArrayList<>();

        int progress = 0;
        for (OldEntity currentEntity : units) {
            context.checkCanceled();
            final Holder minHolder = context.runParallel(classes, Holder::new,
                    (candidate, holder) -> getNearestClass(currentEntity, candidate, holder), this::min);
            progress++;
            context.reportProgress((double) progress / units.size());
            if (minHolder.candidate == null) {
                LOGGER.warn(currentEntity.getName() + " has no nearest class");
                continue;
            }
            final ClassOldEntity nearestClass = minHolder.candidate;
            if (nearestClass.getName().equals(currentEntity.getClassName())) {
                continue;
            }

            final double accuracyRating =
                    AlgorithmsUtil.getGapBasedAccuracyRating(minHolder.distance, minHolder.difference) * ACCURACY;
            if (currentEntity.getCategory() == MetricCategory.Method) {
                processMethod(refactorings, currentEntity, nearestClass, accuracyRating);
            } else {
                refactorings.add(new Refactoring(currentEntity.getName(), nearestClass.getName(), accuracyRating,
                        currentEntity.isField()));
            }
        }

        return refactorings;
    }

    @Nullable
    private Holder getNearestClass(OldEntity entity, ClassOldEntity targetClass, Holder holder) {
        final double distance = entity.distance(targetClass);
        if (holder.distance > distance) {
            holder.difference = holder.distance - distance;
            holder.distance = distance;
            holder.candidate = targetClass;
        } else if (distance - holder.distance < holder.difference) {
            holder.difference = distance - holder.distance;
        }
        return holder;
    }

    private class Holder {
        private double distance = Double.POSITIVE_INFINITY;
        private double difference = Double.POSITIVE_INFINITY;
        private ClassOldEntity candidate;
    }

    private Holder min(Holder first, Holder second) {
        if (first.distance > second.distance) {
            return min(second, first);
        }
        first.difference = Math.min(second.distance - first.distance, first.difference);
        return first;
    }

    private void processMethod(List<Refactoring> refactorings, OldEntity method, ClassOldEntity target, double accuracy) {
        if (method.isMovable()) {
            final ClassOldEntity containingClass = classesByName.get(method.getClassName());
            refactorings.add(new Refactoring(method.getName(), target.getName(), accuracy, false));
            containingClass.removeFromClass(method.getName());
            target.addToClass(method.getName());
        }
    }
}
