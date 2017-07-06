/*
 * Copyright 2005-2017 Sixth and Red River Software, Bas Leijdekkers
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package vector.model;

import java.util.*;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;
import org.jetbrains.annotations.Nullable;
import vector.model.entity.Entity;

public class MRI {
    private final List<Entity> entities;
    private final Set<PsiClass> allClasses;

    public MRI(List<Entity> entityList, Set<PsiClass> existingClasses) {
        entities = entityList;
        allClasses = existingClasses;
    }

    public Map<String, String> run() {
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
        double minDist = Double.MAX_VALUE;

        for (Entity currentClass : entities) {
            if (currentClass.getCategory() == MetricCategory.Class) {
                final double dist = entity.dist(currentClass);
                if (dist < minDist) {
                    minDist = dist;
                    candidateClass = currentClass;
                }
            }
        }
        return candidateClass;
    }

    private void processMethod(Map<String, String> refactorings, Entity currentEntity, Entity nearestClass) {
        final PsiMethod method = (PsiMethod) currentEntity.getPsiElement();
        final PsiClass classToMoveFrom = method.getContainingClass();
        final PsiClass classToMoveTo = (PsiClass) nearestClass.getPsiElement();

        final Set<PsiClass> supersTo = PSIUtil.getAllSupers(classToMoveTo, allClasses);
        final Set<PsiClass> supersFrom = PSIUtil.getAllSupers(classToMoveFrom, allClasses);

        if (supersTo.contains(classToMoveFrom) || supersFrom.contains(classToMoveTo)) {
            return;
        }

        supersFrom.retainAll(supersTo);

        final boolean isOverride = supersFrom.stream()
                .flatMap(c -> Arrays.stream(c.getMethods()))
                .anyMatch(method::equals);

        if (!isOverride) {
            refactorings.put(currentEntity.getName(), nearestClass.getClassName());
            currentEntity.moveToClass((PsiClass) nearestClass.getPsiElement());
            nearestClass.removeFromClass((PsiMethod) currentEntity.getPsiElement());
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
                final double dist = ent.dist(entity);
                String d = "";
                d = dist == Double.MAX_VALUE
                        ? String.format("%1$" + maxLength + "s", "inf")
                        : String.format("  %." + (maxLength - 4) + "f", Double.valueOf(dist));
                System.out.print(d);
            }
            System.out.println();
        }
    }
}
