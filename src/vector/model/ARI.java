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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;

import java.util.*;

public class ARI {
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

        for (Entity method : methodsAndFields) {
            double minD = Double.MAX_VALUE;
            ClassEntity targetClass = null;

            for (final ClassEntity classEntity : classEntities) {
                if (method.getCategory() == MetricCategory.Method) {
                    final PsiClass classFrom = ((PsiMember) method.getPsiElement()).getContainingClass();
                    final PsiClass classTo = (PsiClass) classEntity.getPsiElement();

                    final Set<PsiClass> supersTo = PSIUtil.getAllSupers(classTo, psiClasses);
                    final Set<PsiClass> supersFrom = PSIUtil.getAllSupers(classFrom, psiClasses);

                    if (supersTo.contains(classFrom) || supersFrom.contains(classTo)) {
                        continue;
                    }

                    supersFrom.retainAll(supersTo);
                    final boolean isOverride = supersFrom.stream()
                            .flatMap(c -> Arrays.stream(c.getMethods()))
                            .anyMatch(method::equals);

                    if (isOverride) {
                        continue;
                    }
                }

                final double distance = method.dist(classEntity);

                if (distance < minD) {
                    minD = distance;
                    targetClass = classEntity;
                }
            }

            assert targetClass != null;

            if (communityIds.containsKey(targetClass)) {
                putMethodOrField(method, targetClass);
            } else {
                createCommunity(method, targetClass);
            }

            System.out.println("Add " + method.getName() + " to " + targetClass.getName());
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

    private final Map<String, Set<Entity>> communities = new HashMap<>();
    private final Map<Entity, String> communityIds = new HashMap<>();

    private final List<Entity> methodsAndFields;
    private final List<ClassEntity> classEntities;
    private final Set<PsiClass> psiClasses;
}
