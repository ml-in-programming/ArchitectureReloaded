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

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.classMetrics.NumAttributesAddedMetric;
import com.sixrr.stockmetrics.classMetrics.NumMethodsClassMetric;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.entity.OldEntity;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.utils.AlgorithmsUtil;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.ml_methods_group.utils.AlgorithmsUtil.getDensityBasedAccuracyRating;

public class HAC extends OldAlgorithm {
    private static final Logger LOGGER = Logging.getLogger(HAC.class);
    private static final double ACCURACY = 1;

    private final SortedSet<Triple> heap = new TreeSet<>();
    private final Map<Long, Triple> triples = new HashMap<>();
    private final Set<Community> communities = new HashSet<>();
    private final AtomicInteger progressCounter = new AtomicInteger();
    private OldExecutionContext context;
    private int idGenerator = 0;

    public HAC() {
        super("HAC", true);
    }

    @Override
    public @NotNull List<Class<? extends Metric>> requiredMetrics() {
        return Arrays.asList(NumMethodsClassMetric.class, NumAttributesAddedMetric.class);
    }

    private void init(OldExecutionContext context) {
        LOGGER.info("Init HAC");
        this.context = context;
        heap.clear();
        communities.clear();
        idGenerator = 0;
        progressCounter.set(0);
        final EntitySearchResult entities = context.getEntities();
        Stream.of(entities.getClasses(), entities.getMethods(), entities.getFields())
                .flatMap(List::stream)
                .map(this::singletonCommunity)
                .forEach(communities::add);
        final List<Community> communitiesAsList = new ArrayList<>(communities);
        Collections.shuffle(communitiesAsList);
        final List<Triple> toInsert =
                context.runParallel(communitiesAsList, ArrayList::new, this::findTriples, AlgorithmsUtil::combineLists);
        toInsert.forEach(this::insertTriple);
        LOGGER.info("Built heap (" + heap.size() + " triples)");
    }

    private List<Triple> findTriples(Community community, List<Triple> accumulator) {
        final OldEntity representative = community.entities.get(0);
        for (Community another : communities) {
            if (another == community) {
                break;
            }
            final double distance = representative.distance(another.entities.get(0));
            if (distance < 1) {
                accumulator.add(new Triple(distance, community, another));
            }
        }
        context.reportProgress(0.9 * (double) progressCounter.incrementAndGet() / communities.size());
        context.checkCanceled();
        return accumulator;
    }

    @Override
    protected List<Refactoring> calculateRefactorings(OldExecutionContext context, boolean enableFieldRefactorings) {
        init(context);
        final int initialCommunitiesCount = communities.size();
        while (!heap.isEmpty()) {
            final Triple minTriple = heap.first();
            invalidateTriple(minTriple);
            final Community first = minTriple.first;
            final Community second = minTriple.second;
            mergeCommunities(first, second);
            context.reportProgress(1 - 0.1 * communities.size() / initialCommunitiesCount);
            context.checkCanceled();
        }

        final List<Refactoring> refactorings = new ArrayList<>();
        for (Community community : communities) {
            final int entitiesCount = community.entities.size();
            if (entitiesCount == 0) {
                continue;
            }
            final Entry<String, Long> dominantClass = AlgorithmsUtil.getDominantClass(community.entities);
            final String className = dominantClass.getKey();
            LOGGER.info("Generate class name for community (id = " + community.id +"): " + className);
            for (OldEntity entity : community.entities) {
                if (!entity.getClassName().equals(className)) {
                    if (enableFieldRefactorings || !entity.isField()) {
                        refactorings.add(new Refactoring(entity.getName(), className,
                                getDensityBasedAccuracyRating(dominantClass.getValue(), entitiesCount) * ACCURACY,
                                entity.isField()));
                    }
                }
            }
        }
        Triple.clearPool();
        return refactorings;
    }

    private Community mergeCommunities(Community first, Community second) {
        final List<OldEntity> merged;
        if (first.entities.size() < second.entities.size()) {
            merged = second.entities;
            merged.addAll(first.entities);
        } else {
            merged = first.entities;
            merged.addAll(second.entities);
        }

        final Community newCommunity = new Community(merged);
        communities.remove(first);
        communities.remove(second);

        for (Community community : communities) {
            final long fromFirstID = getTripleID(first, community);
            final long fromSecondID = getTripleID(second, community);
            final Triple fromFirst = triples.get(fromFirstID);
            final Triple fromSecond = triples.get(fromSecondID);
            final double newDistance = Math.max(getDistance(fromFirst), getDistance(fromSecond));
            invalidateTriple(fromFirst);
            invalidateTriple(fromSecond);
            insertTripleIfNecessary(newDistance, newCommunity, community);
        }
        communities.add(newCommunity);
        return newCommunity;
    }

    private double getDistance(@Nullable Triple triple) {
        return triple == null? Double.POSITIVE_INFINITY : triple.distance;
    }

    private long getTripleID(Community first, Community second) {
        if (second.id > first.id) {
            return getTripleID(second, first);
        }
        return first.id * 1_000_000_009L + second.id;
    }

    private void insertTriple(@NotNull Triple triple) {
        triples.put(getTripleID(triple.first, triple.second), triple);
        heap.add(triple);
    }

    private void insertTripleIfNecessary(double distance, Community first, Community second) {
        if (distance > 1.0) {
            return;
        }
        final Triple triple = Triple.createTriple(distance, first, second);
        insertTriple(triple);
    }

    private void invalidateTriple(@Nullable Triple triple) {
        if (triple == null) {
            return;
        }
        final long tripleID = getTripleID(triple.first, triple.second);
        triples.remove(tripleID);
        heap.remove(triple);
        triple.release();
    }

    private Community singletonCommunity(OldEntity entity) {
        final List<OldEntity> singletonList = new ArrayList<>(1);
        singletonList.add(entity);
        return new Community(singletonList);
    }

    private class Community implements Comparable<Community> {

        private final List<OldEntity> entities;
        private final int id;

        Community(List<OldEntity> entities) {
            this.entities = entities;
            id = idGenerator++;
        }

        @Override
        public int compareTo(@NotNull Community o) {
            return id - o.id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Community && ((Community) obj).id == id;
        }
    }

    private static class Triple implements Comparable<Triple> {
        private static final Queue<Triple> triplesPoll = new ArrayDeque<>();

        private double distance;
        private Community first;
        private Community second;

        Triple(double distance, Community first, Community second) {
            this.distance = distance;
            this.first = first;
            this.second = second;
        }

        static Triple createTriple(double distance, Community first, Community second) {
            if (triplesPoll.isEmpty()) {
                return new Triple(distance, first, second);
            }
            final Triple triple = triplesPoll.poll();
            triple.distance = distance;
            triple.first = first;
            triple.second = second;
            return triple;
        }

        static void clearPool() {
            triplesPoll.clear();
        }

        void release() {
            triplesPoll.add(this);
        }

        @Override
        public int compareTo(@NotNull Triple other) {
            if (other == this) {
                return 0;
            }
            if (distance != other.distance) {
                return Double.compare(distance, other.distance);
            }
            if (first != other.first) {
                return first.compareTo(other.first);
            }
            return second.compareTo(other.second);
        }
    }
}
