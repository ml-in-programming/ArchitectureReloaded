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
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.algorithm.entity.MethodEntity;

import java.util.*;

public class ARI extends Algorithm {
    private final List<Entity> units = new ArrayList<>();
    private final List<ClassEntity> classEntities = new ArrayList<>();

    public ARI() {
        super("ARI", true);
    }

    @Override
    protected void setData(Collection<Entity> entities) {
        units.clear();
        classEntities.clear();
        for (Entity entity : entities) {
            if (entity.getCategory() == MetricCategory.Class) {
                classEntities.add((ClassEntity) entity);
            } else {
                units.add(entity);
            }
        }
    }

    @Override
    protected Map<String, String> calculateRefactorings(ExecutionContext context) {
        return runParallel(units, context, HashMap<String, String>::new, this::findRefactoring, this::combineMaps);
    }

    private <K, V> Map<K, V> combineMaps(Map<K, V> first, Map<K, V> second) {
        first.putAll(second);
        return first;
    }

    // todo check, that method isn't abstract or constructor
    private Map<String, String> findRefactoring(Entity entity, Map<String, String> accumulator) {
        if (entity.getCategory() == MetricCategory.Method && ((MethodEntity) entity).isOverriding()) {
            return accumulator;
        }
        double minDistance = Double.POSITIVE_INFINITY;
        ClassEntity targetClass = null;
        for (final ClassEntity classEntity : classEntities) {

            final double distance = entity.distance(classEntity);
            if (distance < minDistance) {
                minDistance = distance;
                targetClass = classEntity;
            }
        }

        if (targetClass == null) {
            System.out.println("Warning (ARI): targetClass is null for " + entity.getName());
            return accumulator;
        }

        final String targetClassName = targetClass.getName();
        if (!targetClassName.equals(entity.getClassName())) {
            accumulator.put(entity.getName(), targetClassName);
        }
        return accumulator;
    }
}
