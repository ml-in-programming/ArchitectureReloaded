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

import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;

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
    protected void setData(EntitySearchResult entities) {
        this.entities.clear();
        this.entities.addAll(entities.getClasses());
        this.entities.addAll(entities.getMethods());
        this.entities.addAll(entities.getFields());
    }

    @Override
    protected Map<String, String> calculateRefactorings(ExecutionContext context) {
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

    public void printTableDistances() {
        int maxLength = 0;
        for (Entity ent : entities) {
            maxLength = Math.max(maxLength, ent.getName().length() + 4);
        }

        System.out.print(String.format("%1$" + maxLength + "s", ""));
        for (Entity ent : entities) {
            final String name = String.format("%1$" + maxLength + "s", ent.getName());
            System.out.print(name);
        }
        System.out.println();

        for (Entity ent : entities) {
            final String name = String.format("%1$" + maxLength + "s", ent.getName());
            System.out.print(name);
            for (Entity entity : entities) {
                final double dist = ent.distance(entity);
                String d = "";
                d = dist == Double.POSITIVE_INFINITY
                        ? String.format("%1$" + maxLength + "s", "inf")
                        : String.format("  %." + (maxLength - 4) + "f", dist);
                System.out.print(d);
            }
            System.out.println();
        }
    }
}
