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
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;
import org.ml_methods_group.algorithm.entity.MethodEntity;

import java.util.*;
import java.util.stream.Stream;

public class AKMeans extends Algorithm {
    private final List<Entity> points = new ArrayList<>();
    private final Map<String, Set<Entity>> communities = new HashMap<>();
    private final Map<Entity, String> communityIds = new HashMap<>();
    private final int steps;
    private int numberOfClasses = 0;
    private int newClassCount = 0;

    public AKMeans(int steps) {
        super("AKMeans", false);
        this.steps = steps;
    }

    public AKMeans() {
        this(50);
    }

    @Override
    protected void setData(EntitySearchResult entities) {
        points.clear();
        communities.clear();
        communityIds.clear();
        newClassCount = 0;
        numberOfClasses = entities.getClasses().size();
        Stream.of(entities.getMethods(), entities.getFields())
                .flatMap(List::stream)
                .peek(points::add)
                .forEach(e -> communityIds.put(e, ""));
    }

    private void initializeCenters() {
        final List<Entity> entities = new ArrayList<>(points);
        Collections.shuffle(entities);

        for (int i = 0; i < numberOfClasses; ++i) {
            final Entity center = entities.get(i);
            communities.put(center.getName(), new HashSet<>(Collections.singletonList(center)));
        }
    }

    @Override
    protected Map<String, String> calculateRefactorings(ExecutionContext context) {
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
        Integer maxClassCount = 0;
        final Map<String, Integer> classCounts = new HashMap<>();
        for (Entity entity : communities.get(center)) {
            final String className = entity.getClassName();
            if (!classCounts.containsKey(className)) {
                classCounts.put(className, 0);
            }

            classCounts.put(className, classCounts.get(className) + 1);
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
        double minD = Double.POSITIVE_INFINITY;
        String id = "";
        for (String center : communities.keySet()) {
            double d = distToCommunity(entity, center);
            if (!canMove(entity, center)) {
                d = Double.POSITIVE_INFINITY;
            }
            if (d < minD) {
                minD = d;
                id = center;
            }
        }

        return id;
    }

    private boolean canMove(Entity entity, String center) {
        return entity.isMovable() || entity.getClassName().equals(center);
    }

    private double distToCommunity(Entity entity, String center) {
        double minD = 0.0;
        for (Entity point : communities.get(center)) {
            final double d = entity.distance(point);
            minD = Math.max(d, minD);
        }

        return minD;
    }

    private void moveToCommunity(Entity entity, String id) {
        communities.get(id).add(entity);
        communityIds.put(entity, id);
    }
}
