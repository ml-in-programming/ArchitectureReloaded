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

import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

public class AKMeans extends Algorithm {
    private final List<Entity> points = new ArrayList<>();
    private final Map<String, Set<Entity>> communities = new HashMap<>();
    private final Map<Entity, String> communityIds = new HashMap<>();
    private final int steps;
    private int numberOfClasses = 0;
    private int newClassCount = 0;

    public AKMeans(int steps) {
        super("AKMeans", true);
        this.steps = steps;
    }

    public AKMeans() {
        this(50);
    }

    private void init(EntitySearchResult entities) {
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
        init(context.entities);
        final Map<String, String> refactorings = new HashMap<>();
        initializeCenters();

        for (int step = 0; step < steps; step++) {
            reportProgress((double) step / steps, context);
            final Map<Entity, String> movements =
                    runParallel(points, context, HashMap::new, this::findNearestCommunity, Algorithm::combineMaps);
            for (Entry<Entity, String> movement : movements.entrySet()) {
                moveToCommunity(movement.getKey(), movement.getValue());
            }
            if (movements.size() == 0) {
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
            classCounts.put(className, classCounts.getOrDefault(className, 0) + 1);
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

    private Map<Entity, String> findNearestCommunity(Entity entity, Map<Entity, String> accumulator) {
        double minDistance = Double.POSITIVE_INFINITY;
        String target = null;
        for (String center : communities.keySet()) {
            double distance = distToCommunity(entity, center);
            if (canMove(entity, center) && distance < minDistance) {
                minDistance = distance;
                target = center;
            }
        }
        if (target != null && !target.equals(communityIds.get(entity))) {
            accumulator.put(entity, target);
        }
        return accumulator;
    }

    private boolean canMove(Entity entity, String center) {
        return entity.isMovable() || entity.getClassName().equals(center);
    }

    private double distToCommunity(Entity entity, String center) {
        double maxDistance = 0.0;
        for (Entity point : communities.get(center)) {
            final double distance = entity.distance(point);
            maxDistance = Math.max(distance, maxDistance);
            if (maxDistance == Double.POSITIVE_INFINITY) {
                break;
            }
        }

        return maxDistance;
    }

    private void moveToCommunity(Entity entity, String id) {
        final String currentCommunity = communityIds.get(entity);
        if (!currentCommunity.isEmpty()) {
            communities.get(currentCommunity).remove(entity);
        }
        communities.get(id).add(entity);
        communityIds.put(entity, id);
    }
}
