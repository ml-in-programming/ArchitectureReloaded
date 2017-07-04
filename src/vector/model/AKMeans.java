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
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;

import java.util.*;

/**
 * Created by Kivi on 17.05.2017.
 */
public class AKMeans {
    public AKMeans(List<Entity> entityList, int steps) {
        this.steps = steps;
        for (Entity e : entityList) {
            if (e.getCategory() != MetricCategory.Class) {
                points.add(e);
                communityIds.put(e, "");
                entityByName.put(e.getName(), e);
            } else {
                N++;
                allClasses.add((PsiClass) e.getPsiElement());
            }
        }
    }

    private void initializeCenters() {
        List<Entity> entities = new ArrayList<Entity>(points);
        Collections.shuffle(entities);

        for (int i = 0; i < N; ++i) {
            Entity center = entities.get(i);
            Set<Entity> simpleCommunity = new HashSet<Entity>();
            simpleCommunity.add(center);
            communities.put(center.getName(), simpleCommunity);
        }
    }

    public Map<String, String> run() {
        Map<String, String> refactorings = new HashMap<String, String>();

        initializeCenters();
        for (int step = 0; step < steps; ++step) {
            boolean moving = false;
            Map<Entity, String> newCommunities = new HashMap<Entity, String>();
            for (Entity entity : points) {
                String newCenter = findNearestCommunity(entity);
                if (newCenter.equals("")) {
                    continue;
                }
                if (!newCenter.equals(communityIds.get(entity))) {
                    moving = true;
                }

                newCommunities.put(entity, newCenter);
                if (!communityIds.get(entity).equals(newCenter)) {
                    System.out.println("Move " + entity.getName() + " to " + newCenter + ", " + distToCommunity(entity, newCenter));
                }
                //moveToCommunity(entity, newCenter);
            }

            for (Entity entity : newCommunities.keySet()) {
                moveToCommunity(entity, newCommunities.get(entity));
            }
            System.out.println();

            if (!moving) {
                break;
            }
        }

        for (String center : communities.keySet()) {
            String newName = receiveClassName(center);
            for (Entity entity : communities.get(center)) {
                if (!entity.getClassName().equals(newName)) {
                    refactorings.put(entity.getName(), newName);
                }
            }
        }

        return refactorings;
    }

    private String receiveClassName(String center) {
        String name = "";
        Integer maxClassCount = 0;
        Map<String, Integer> classCounts = new HashMap<String, Integer>();
        for (Entity entity : communities.get(center)) {
            String className = entity.getClassName();
            if (!classCounts.containsKey(className)) {
                classCounts.put(className, 0);
            }

            Integer count = classCounts.get(className);
            count++;
            classCounts.put(className, count);
        }

        for (String className : classCounts.keySet()) {
            if (maxClassCount < classCounts.get(className)) {
                maxClassCount = classCounts.get(className);
                name = className;
            }
        }

        if (name.equals("")) {
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

        PsiMethod method = (PsiMethod) entity.getPsiElement();
        Set<Entity> cluster = communities.get(center);
        for (Entity e : cluster) {
            if (e.getCategory() != MetricCategory.Method) {
                continue;
            }

            PsiMethod component = (PsiMethod) e.getPsiElement();

            Set<PsiMethod> supers = PSIUtil.getAllSupers(component, allClasses);
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
            double d = entity.dist(point);
            minD = Math.max(d, minD);
        }

        return minD;
    }

    private void moveToCommunity(Entity entity, String id) {
        communities.get(id).add(entity);
        communityIds.put(entity, id);
    }

    private List<Entity> points = new ArrayList<Entity>();
    //private Set<>
    private Map<String, Set<Entity>> communities = new HashMap<String, Set<Entity>>();
    private Map<Entity, String> communityIds = new HashMap<Entity, String>();
    private Map<String, Entity> entityByName = new HashMap<String, Entity>();
    private Set<PsiClass> allClasses = new HashSet<PsiClass>();
    private int N = 0;
    private int steps = 0;
    private int newClassCount = 0;
}
