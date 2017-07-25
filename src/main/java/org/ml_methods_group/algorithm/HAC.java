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

import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HAC extends Algorithm {
    private final SortedSet<Triple> heap = new TreeSet<>();
    private final Set<Community> communities = new HashSet<>();
    private long idGenerator = 0;
    private int newClassCount = 0;

    public HAC() {
        super("HAC", false);
    }

    @Override
    protected void setData(EntitySearchResult entities) {
        heap.clear();
        communities.clear();
        idGenerator = 0;
        newClassCount = 0;
        Stream.of(entities.getClasses(), entities.getMethods(), entities.getFields())
                .flatMap(List::stream)
                .map(this::singletonCommunity)
                .forEach(communities::add);

        for (Community first : communities) {
            final Entity representative = first.entities.get(0);
            for (Community second : communities) {
                if (first == second) {
                    break;
                }
                final double distance = representative.distance(second.entities.get(0));
                insertTriple(distance, first, second);
            }
        }
    }

    protected Map<String, String> calculateRefactorings(ExecutionContext context) {
        final Map<String, String> refactorings = new HashMap<>();

        while (!heap.isEmpty()) {
            final Triple minTriple = heap.first();
            invalidateTriple(minTriple);
            final Community first = minTriple.first;
            final Community second = minTriple.second;
            mergeCommunities(first, second);
        }

        for (Community community : communities) {
            final String newName = receiveClassName(community);
            for (Entity entity : community.entities) {
                if (!entity.getClassName().equals(newName)) {
                    refactorings.put(entity.getName(), newName);
                }
            }
        }
        return refactorings;
    }

    // todo doubtful code starts

    private String calculateClassName(Community community) {
        return community.entities.stream()
                .collect(Collectors.groupingBy(Entity::getClassName, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
    }

    private String receiveClassName(Community community) {
        final String name = calculateClassName(community);
        return name.isEmpty() ? "NewClass" + newClassCount++ : name;
    }

    // doubtful code ends

    private Community mergeCommunities(Community first, Community second) {
        final List<Entity> merged;
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
            final Triple fromFirst = first.usages.get(community);
            final Triple fromSecond = second.usages.get(community);
            final double newDistance = Math.max(getDistance(fromFirst), getDistance(fromSecond));
            insertTriple(newDistance, newCommunity, community);
            invalidateTriple(fromFirst);
            invalidateTriple(fromSecond);
        }
        communities.add(newCommunity);
        return newCommunity;
    }

    private double getDistance(Triple triple) {
        return triple == null? Double.POSITIVE_INFINITY : triple.distance;
    }

    private void insertTriple(double distance, Community first, Community second) {
        if (distance > 1.0) {
            return;
        }
        final Triple triple = new Triple(distance, first, second);
        second.usages.put(first, triple);
        first.usages.put(second, triple);
        heap.add(triple);
    }

    private void invalidateTriple(Triple triple) {
        if (triple == null) {
            return;
        }
        triple.first.usages.remove(triple.second, triple);
        triple.second.usages.remove(triple.first, triple);
        heap.remove(triple);
    }

    private Community singletonCommunity(Entity entity) {
        final List<Entity> singletonList = new ArrayList<>(1);
        singletonList.add(entity);
        return new Community(singletonList);
    }

    private class Community implements Comparable<Community> {

        private final List<Entity> entities;
        private final Map<Community, Triple> usages = new HashMap<>();
        private final long id;

        Community(List<Entity> entities) {
            this.entities = entities;
            id = idGenerator++;
        }

        @Override
        public int compareTo(@NotNull Community o) {
            return Long.compare(id, o.id);
        }

        @Override
        public int hashCode() {
            return Long.hashCode(id);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Community && ((Community) obj).id == id;
        }
    }

    private static class Triple implements Comparable<Triple> {
        private final double distance;
        private final Community first;
        private final Community second;

        Triple(double distance, Community first, Community second) {
            this.distance = distance;
            this.first = first;
            this.second = second;
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
