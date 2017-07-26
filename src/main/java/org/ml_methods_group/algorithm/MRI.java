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
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// It changes entities
@Deprecated
public class MRI extends Algorithm {
    private final List<Entity> entities = new ArrayList<>();

    public MRI() {
        super("MRI", false);
    }

    @Override
    protected Map<String, String> calculateRefactorings(ExecutionContext context) {
        this.entities.clear();
        this.entities.addAll(context.entities.getClasses());
        this.entities.addAll(context.entities.getMethods());
        this.entities.addAll(context.entities.getFields());

        final Map<String, String> refactorings = new HashMap<>();

        for (Entity currentEntity : entities) {
            if (currentEntity.getCategory() == MetricCategory.Class) {
                continue;
            }

            final Entity nearestClass = getNearestClass(currentEntity);
            if (nearestClass == null) {
                System.out.println("WARNING: " + currentEntity.getName() + " has no nearest class");
                continue;
            }

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
    private Entity getNearestClass(Entity entity) {
        Entity candidateClass = null;
        double minDist = Double.POSITIVE_INFINITY;

        for (Entity currentClass : entities) {
            if (currentClass.getCategory() == MetricCategory.Class) {
                final double dist = entity.distance(currentClass);
                if (dist < minDist) {
                    minDist = dist;
                    candidateClass = currentClass;
                }
            }
        }
        return candidateClass;
    }

    private void processMethod(Map<String, String> refactorings, Entity method, Entity nearestClass) {
        if (method.isMovable()) {
            refactorings.put(method.getName(), nearestClass.getClassName());
            method.moveToClass(nearestClass.getName());
            nearestClass.removeFromClass(method.getName());
        }
    }
}
