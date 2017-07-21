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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.utils.MethodUtils;
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.Entity;

import java.util.*;

public class ARI {
    private final Map<String, Set<Entity>> communities = new HashMap<>();
    private final Map<Entity, String> communityIds = new HashMap<>();
    private final List<Entity> methodsAndFields;
    private final List<ClassEntity> classEntities;
    private final Set<PsiClass> psiClasses;

    public ARI(Iterable<Entity> entityList) {
        methodsAndFields = new ArrayList<>();
        classEntities = new ArrayList<>();
        psiClasses = new HashSet<>();
        for (Entity entity : entityList) {
            if (entity.getCategory() == MetricCategory.Class) {
                classEntities.add((ClassEntity) entity);
                psiClasses.add((PsiClass) entity.getPsiElement());
            } else {
                methodsAndFields.add(entity);
            }
        }
    }

    public Map<String, String> run() {
        final Map<String, String> refactorings = new HashMap<>();

        for (Entity unit : methodsAndFields) {
            double minD = Double.MAX_VALUE;
            ClassEntity targetClass = null;

            for (final ClassEntity classEntity : classEntities) {
                if (unit.getCategory() == MetricCategory.Method) {
                    final PsiMethod method = (PsiMethod) unit.getPsiElement();
                    final PsiClass classFrom = (method).getContainingClass();
                    final PsiClass classTo = (PsiClass) classEntity.getPsiElement();

                    final Set<PsiClass> supersTo = PSIUtil.getAllSupers(classTo, psiClasses);
                    final Set<PsiClass> supersFrom = PSIUtil.getAllSupers(classFrom, psiClasses);

                    if (supersTo.contains(classFrom) || supersFrom.contains(classTo)) {
                        continue;
                    }

                    supersFrom.retainAll(supersTo);
                    final boolean isOverride = supersFrom.stream()
                            .flatMap(c -> Arrays.stream(c.getMethods()))
                            .anyMatch(unit::equals);

                    if (isOverride || method.isConstructor() || MethodUtils.isAbstract(method)) {
                        continue;
                    }
                }

                final double distance = unit.distance(classEntity);

                if (distance < minD) {
                    minD = distance;
                    targetClass = classEntity;
                }
            }

//            assert targetClass != null;

            if (targetClass == null) {
                System.out.println("!!!!! targetClass is null for " + unit.getName());
                // TODO: find out why are they null
                continue;
            }

            if (communityIds.containsKey(targetClass)) {
                putMethodOrField(unit, targetClass);
            } else {
                createCommunity(unit, targetClass);
            }

            System.out.println("Add " + unit.getName() + " to " + targetClass.getName());
        }

        for (Entity entity : methodsAndFields) {
            if (!entity.getClassName().equals(communityIds.get(entity))) {
                refactorings.put(entity.getName(), communityIds.get(entity));
            }
        }

        return refactorings;
    }

    private void createCommunity(Entity method, Entity cl) {
        communityIds.put(cl, cl.getName());
        communityIds.put(method, cl.getName());
        communities.put(cl.getName(), new HashSet<>(Arrays.asList(method, cl)));
    }

    private void putMethodOrField(Entity method, Entity cl) {
        communityIds.put(method, cl.getName());
        communities.get(cl.getName()).add(method);
    }
}
