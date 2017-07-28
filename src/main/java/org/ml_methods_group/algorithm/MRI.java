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

import com.sixrr.metrics.MetricCategory;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;
import org.ml_methods_group.config.Logging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MRI extends Algorithm {
    private static final Logger LOGGER = Logging.getLogger(MRI.class);

    private final List<Entity> units = new ArrayList<>();
    private final Map<String, ClassEntity> classesByName = new HashMap<>();
    private final List<ClassEntity> classes = new ArrayList<>();

    public MRI() {
        super("MRI", true);
    }

    @Override
    protected Map<String, String> calculateRefactorings(ExecutionContext context) {
        final EntitySearchResult searchResult = context.entities;
        units.clear();
        classes.clear();
        Stream.of(searchResult.getFields(), searchResult.getMethods())
                .flatMap(List::stream)
                .filter(Entity::isMovable)
                .forEach(units::add);

        searchResult.getClasses()
                .stream()
                .map(ClassEntity::copy) // create local copies
                .peek(entity -> classesByName.put(entity.getName(), entity))
                .forEach(classes::add);

        final Map<String, String> refactorings = new HashMap<>();

        int progress = 0;
        for (Entity currentEntity : units) {
            final Holder minHolder = runParallel(classes, context, Holder::new,
                            (candidate, holder) -> getNearestClass(currentEntity, candidate, holder), this::min);
            progress++;
            reportProgress((double) progress / units.size(), context);
            if (minHolder.candidate == null) {
                LOGGER.warn(currentEntity.getName() + " has no nearest class");
                continue;
            }
            final ClassEntity nearestClass = minHolder.candidate;
            if (nearestClass.getName().equals(currentEntity.getClassName())) {
                continue;
            }

            if (currentEntity.getCategory() == MetricCategory.Method) {
                processMethod(refactorings, currentEntity, nearestClass);
            } else {
                refactorings.put(currentEntity.getName(), nearestClass.getName());
            }
        }

        return refactorings;
    }

    @Nullable
    private Holder getNearestClass(Entity entity, ClassEntity targetClass, Holder holder) {
        final double distance = entity.distance(targetClass);
        if (holder.distance > distance) {
            holder.distance = distance;
            holder.candidate = targetClass;
        }
        return holder;
    }

    private class Holder {
        private double distance = Double.POSITIVE_INFINITY;
        private ClassEntity candidate;
    }

    private Holder min(Holder first, Holder second) {
        return first.distance > second.distance ? second : first;
    }

    private void processMethod(Map<String, String> refactorings, Entity method, ClassEntity nearestClass) {
        if (method.isMovable()) {
            final ClassEntity containingClass = classesByName.get(method.getClassName());
            refactorings.put(method.getName(), nearestClass.getClassName());
            containingClass.removeFromClass(method.getName());
            nearestClass.addToClass(method.getName());
        }
    }
}
