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

package org.jetbrains.research.groups.ml_methods.algorithm;

import org.apache.log4j.Logger;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.Entity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.EntitySearchResult;
import org.jetbrains.research.groups.ml_methods.config.Logging;
import org.jetbrains.research.groups.ml_methods.utils.AlgorithmsUtil;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static org.jetbrains.research.groups.ml_methods.utils.AlgorithmsUtil.getDensityBasedAccuracyRating;

public class AKMeans extends Algorithm {
    private static final Logger LOGGER = Logging.getLogger(AKMeans.class);
    private static final double ACCURACY = 1;

    private final List<Entity> points = new ArrayList<>();
    private final List<Integer> indexes = new ArrayList<>();
    private final List<Integer> communityID = new ArrayList<>();
    private final List<Set<Entity>> communities = new ArrayList<>();
    private final int steps;
    private int numberOfClasses = 0;

    public AKMeans(int steps) {
        super("AKMeans", true);
        this.steps = steps;
    }

    public AKMeans() {
        this(25);
    }

    private void init(EntitySearchResult entities) {
        LOGGER.info("Init AKMeans");
        points.clear();
        communities.clear();
        communityID.clear();
        numberOfClasses = entities.getClasses().size();
        Stream.of(entities.getMethods(), entities.getFields())
                .flatMap(List::stream)
                .peek(x -> communityID.add(-1))
                .forEach(points::add);
        communityID.addAll(Collections.nCopies(points.size(), -1));
        Stream.iterate(0, x -> x + 1)
                .sequential()
                .limit(points.size())
                .forEach(indexes::add);
    }

    private void initializeCenters() {
        LOGGER.info("Initialize centers");
        final List<Entity> entities = new ArrayList<>(points);
        Collections.shuffle(entities);

        for (int i = 0; i < numberOfClasses; i++) {
            final Set<Entity> community = new HashSet<>();
            community.add(entities.get(i));
            communityID.set(i, i);
            communities.add(community);
        }
    }

    @Override
    protected List<Refactoring> calculateRefactorings(ExecutionContext context, boolean enableFieldRefactorings) {
        init(context.getEntities());
        context.checkCanceled();
        initializeCenters();
        context.checkCanceled();

        for (int step = 0; step < steps; step++) {
            LOGGER.info("Start step " + step);
            reportProgress((double) step / steps, context);
            context.checkCanceled();
            final Map<Integer, Integer> movements =
                    runParallel(indexes, context, HashMap::new, this::findNearestCommunity, AlgorithmsUtil::combineMaps);
            for (Entry<Integer, Integer> movement : movements.entrySet()) {
                moveToCommunity(movement.getKey(), movement.getValue());
            }
            LOGGER.info(movements.size() + " movements found");
            if (movements.size() == 0) {
                break;
            }
        }

        final List<Refactoring> refactorings = new ArrayList<>();
        for (Set<Entity> community : communities) {
            final Entry<String, Long> dominant = AlgorithmsUtil.getDominantClass(community);
            community.stream()
                    .filter(e -> !e.getClassName().equals(dominant.getKey()))
                    .filter(Entity::isMovable)
                    .filter(e -> enableFieldRefactorings || !e.isField())
                    .map(e -> new Refactoring(e.getName(), dominant.getKey(),
                            getDensityBasedAccuracyRating(dominant.getValue(), community.size()) * ACCURACY,
                            e.isField()))
                    .forEach(refactorings::add);
        }
        return refactorings;
    }

    private Map<Integer, Integer> findNearestCommunity(int entityID, Map<Integer, Integer> accumulator) {
        double minDistance = Double.POSITIVE_INFINITY;
        int targetID = -1;
        final Entity entity = points.get(entityID);
        for (int centerID = 0; centerID < communities.size(); centerID++) {
            double distance = distToCommunity(entity, centerID);
            if (distance < minDistance) {
                minDistance = distance;
                targetID = centerID;
            }
        }
        if (targetID != -1 && targetID != communityID.get(entityID)) {
            accumulator.put(entityID, targetID);
        }
        return accumulator;
    }

    private double distToCommunity(Entity entity, int centerID) {
        final Set<Entity> community = communities.get(centerID);
        if (community.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }
        double maxDistance = 0.0;
        for (Entity point : community) {
            final double distance = entity.distance(point);
            maxDistance = Math.max(distance, maxDistance);
            if (maxDistance == Double.POSITIVE_INFINITY) {
                break;
            }
        }

        return maxDistance;
    }

    private void moveToCommunity(int entityID, int centerID) {
        final Entity entity = points.get(entityID);
        final int currentCommunity = communityID.get(entityID);
        if (currentCommunity != -1) {
            communities.get(currentCommunity).remove(points.get(entityID));
        }
        communities.get(centerID).add(entity);
        communityID.set(entityID, centerID);
    }
}
