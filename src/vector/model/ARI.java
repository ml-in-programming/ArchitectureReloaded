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
        classes = new ArrayList<>();
        allClasses = new HashSet<>();
        for (Entity entity : entityList) {
            final String name = entity.getName();
            entityByName.put(name, entity);
            if (entity.getCategory() == MetricCategory.Class) {
                classes.add((ClassEntity) entity);
                allClasses.add((PsiClass) entity.getPsiElement());
            } else {
                methodsAndFields.add(entity);
            }
        }
    }

    public Map<String, String> run() {
        final Map<String, String> refactorings = new HashMap<>();

        for (Entity method : methodsAndFields) {
            double minD = Double.MAX_VALUE;
            int classId = -1;
            for (int i = 0; i < classes.size(); i++) {
                final ClassEntity classEntity = classes.get(i);
                if (method.getCategory() == MetricCategory.Method) {
                    final PsiClass moveFromClass = ((PsiMember) method.getPsiElement()).getContainingClass();
                    final PsiClass moveToClass = (PsiClass) classEntity.getPsiElement();

                    final Set<PsiClass> supersTo = PSIUtil.getAllSupers(moveToClass, allClasses);//new HashSet<PsiClass>(Arrays.asList(moveToClass.getSupers()));
                    boolean isSuper = false;

                    for (PsiClass sup : supersTo) {
                        if (sup.equals(moveFromClass)) {
                            isSuper = true;
                            break;
                        }
                    }

                    final Set<PsiClass> supersFrom = PSIUtil.getAllSupers(moveFromClass, allClasses);//new HashSet<PsiClass>(Arrays.asList(moveFromClass.getSupers()));
                    for (PsiClass sup : supersFrom) {
                        if (sup.equals(moveToClass)) {
                            isSuper = true;
                            break;
                        }
                    }
                    supersFrom.retainAll(supersTo);
                    boolean isOverride = false;

                    if (isSuper) {
                        continue;
                    }

                    for (PsiClass sup : supersFrom) {
                        final PsiMethod[] methods = sup.getMethods();
                        for (PsiMethod m : methods) {
                            if (m.equals(method)) {
                                isOverride = true;
                                break;
                            }
                        }

                        if (isOverride) {
                            break;
                        }
                    }

                    if (isOverride) {
                        continue;
                    }
                }

                final double distance = method.dist(classEntity);

                if (distance < minD) {
                    minD = distance;
                    classId = i;
                }
            }

            if (classId == -1) {
                System.out.println("HOW??? " + method.getName());
            }

            final ClassEntity classEntity = classes.get(classId);
            if (communityIds.containsKey(classEntity)) {
                putMethodOrField(method, classEntity);
            } else {
                createCommunity(method, classEntity);
            }

            System.out.println("Add " + method.getName() + " to " + classEntity.getName());
        }

        for (Entity ent : methodsAndFields) {
            if (!ent.getClassName().equals(communityIds.get(ent))) {
                refactorings.put(ent.getName(), communityIds.get(ent));
            }
        }

        return refactorings;
    }

    private void createCommunity(Entity method, Entity cl) {
        communityIds.put(cl, cl.getName());
        communityIds.put(method, cl.getName());
        final Set<Entity> newCommunity = new HashSet<>();
        newCommunity.add(method);
        newCommunity.add(cl);
        communities.put(cl.getName(), newCommunity);
    }

    private void putMethodOrField(Entity method, Entity cl) {
        communityIds.put(method, cl.getName());
        communities.get(cl.getName()).add(method);
    }

    private final Map<String, Set<Entity>> communities = new HashMap<>();
    private final Map<Entity, String> communityIds = new HashMap<>();

    private final List<Entity> methodsAndFields;
    private final List<ClassEntity> classes;
    private final Set<PsiClass> allClasses;
    private final Map<String, Entity> entityByName = new HashMap<>();
}
