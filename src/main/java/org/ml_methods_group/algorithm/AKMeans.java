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
import org.ml_methods_group.algorithm.entity.Entity;

import java.util.*;

public class AKMeans {
    public AKMeans(Iterable<Entity> entityList, int steps) {
        this.steps = steps;
        for (Entity e : entityList) {
            if (e.getCategory() != MetricCategory.Class) {
                points.add(e);
                communityIds.put(e, "");
            } else {
                numberOfClasses++;
                allClasses.add((PsiClass) e.getPsiElement());
            }
        }
    }

    private void initializeCenters() {
        final List<Entity> entities = new ArrayList<>(points);
        Collections.shuffle(entities);

        for (int i = 0; i < numberOfClasses; ++i) {
            final Entity center = entities.get(i);
            communities.put(center.getName(), new HashSet<>(Collections.singletonList(center)));
        }
    }

    public Map<String, String> run() {
        final Map<String, String> refactorings = new HashMap<>();
        initializeCenters();

        for (int step = 0; step < steps; ++step) {
            boolean isMoved = false;
            final Map<Entity, String> newCommunities = new HashMap<>();
            for (Entity entity : points) {
                final String newCenter = findNearestCommunity(entity);
                if (newCenter.isEmpty()) {
                    continue;
                }

                if (!newCenter.equals(communityIds.get(entity))) {
                    isMoved = true;
                }

                newCommunities.put(entity, newCenter);
                if (!communityIds.get(entity).equals(newCenter)) {
                    System.out.println("Move " + entity.getName() + " to " + newCenter + ", " + distToCommunity(entity, newCenter));
                }
            }

            for (Entity entity : newCommunities.keySet()) {
                moveToCommunity(entity, newCommunities.get(entity));
            }
            System.out.println();

            if (!isMoved) {
                break;
            }
        }

        for (String center : communities.keySet()) {
            final String newName = receiveClassName(center);
            communities.get(center).stream()
                    .filter(e -> !e.getClassName().equals(newName))
                    .forEach(e -> refactorings.put(e.getName(), newName));
        }

        return refactorings;
    }

    private String receiveClassName(String center) {
        String name = "";
        Integer maxClassCount = Integer.valueOf(0);
        final Map<String, Integer> classCounts = new HashMap<>();
        for (Entity entity : communities.get(center)) {
            final String className = entity.getClassName();
            if (!classCounts.containsKey(className)) {
                classCounts.put(className, 0);
            }

            classCounts.put(className, Integer.valueOf(classCounts.get(className) + 1));
        }

        for (String className : classCounts.keySet()) {
            if (maxClassCount < classCounts.get(className)) {
                maxClassCount = classCounts.get(className);
                name = className;
            }
        }

        if (name.isEmpty()) {
            newClassCount++;
            name = "NewClass" + newClassCount;
        }

        return name;
    }

    private String findNearestCommunity(Entity entity) {
        double minD = Double.MAX_VALUE;
        String id = "";
        for (String center : communities.keySet()) {
            double d = distToCommunity(entity, center);
            if (!canMove(entity, center)) {
                d = Double.MAX_VALUE;
            }
            if (d < minD) {
                minD = d;
                id = center;
            }
        }

        return id;
    }

    private boolean canMove(Entity entity, String center) {
        if (entity.getCategory() != MetricCategory.Method) {
            return true;
        }

        final PsiMethod method = (PsiMethod) entity.getPsiElement();
        final Set<Entity> cluster = communities.get(center);
        for (Entity e : cluster) {
            if (e.getCategory() != MetricCategory.Method) {
                continue;
            }

            final PsiMethod component = (PsiMethod) e.getPsiElement();

            final Set<PsiMethod> supers = PSIUtil.getAllSupers(component, allClasses);
            supers.retainAll(PSIUtil.getAllSupers(method));
            if (!supers.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private double distToCommunity(Entity entity, String center) {
        double minD = 0.0;
        for (Entity point : communities.get(center)) {
            final double d = entity.dist(point);
            minD = Math.max(d, minD);
        }

        return minD;
    }

    private void moveToCommunity(Entity entity, String id) {
        communities.get(id).add(entity);
        communityIds.put(entity, id);
    }

    private final List<Entity> points = new ArrayList<>();
    private final Map<String, Set<Entity>> communities = new HashMap<>();
    private final Map<Entity, String> communityIds = new HashMap<>();
    private final Set<PsiClass> allClasses = new HashSet<>();
    private int numberOfClasses = 0;
    private int steps = 0;
    private int newClassCount = 0;
}
