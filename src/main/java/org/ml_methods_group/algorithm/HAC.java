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
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.entity.Entity;

import java.util.*;
import java.util.stream.Collectors;

public class HAC extends Algorithm {
    private final SortedSet<Triple> heap = new TreeSet<>();
    private final Set<Community> communities = new HashSet<>();
    private long idGenerator = 0;
    private int newClassCount = 0;

    public HAC(Collection<Entity> entityList) {
        super("HAC", false);
        entityList.stream()
                .map(Community::new)
                .forEach(communities::add);

        for (Community first : communities) {
            final Entity representative = getRepresentative(first);
            for (Community second : communities) {
                if (first == second) {
                    break;
                }
                final double distance = representative.distance(getRepresentative(second));
                createAndInsertTriple(distance, first, second);
            }
        }
    }

    protected Map<String, String> calculateRefactorings(ExecutionContext context) {
        final Map<String, String> refactorings = new HashMap<>();

        while (!heap.isEmpty()) {
            final Triple minTriple = heap.first();
            if (minTriple.distance > 1.0) {
                break;
            }
            invalidateTriple(minTriple);
            final Community first = minTriple.first;
            final Community second = minTriple.second;
            final Community mergedCommunity = mergeCommunities(first, second);
            System.out.println("Merge " + first + " and " + second + " to " + mergedCommunity);
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

    private Entity getRepresentative(Community community) {
        if (community.entities.size() != 1) {
            throw new IllegalArgumentException("Something went wrong! Singleton set expected");
        }
        return community.entities.iterator().next();
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
        final Set<Entity> merged = new HashSet<>();
        merged.addAll(first.entities);
        merged.addAll(second.entities);
        final Set<String> availableClassNames = merged.stream()
                .filter(e -> e.getCategory() == MetricCategory.Class)
                .map(Entity::getName)
                .collect(Collectors.toSet());
        final String newName = merged.stream()
                .collect(Collectors.groupingBy(Entity::getClassName, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> availableClassNames.contains(entry.getKey()))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(first.name);

        final Community newCommunity = new Community(merged, newName);
        communities.remove(first);
        communities.remove(second);

        for (Community community : communities) {
            final Triple fromFirst = first.usages.get(community);
            final Triple fromSecond = second.usages.get(community);
            final double newDistance = Math.max(fromFirst.distance, fromSecond.distance);
            createAndInsertTriple(newDistance, newCommunity, community);
            invalidateTriple(fromFirst);
            invalidateTriple(fromSecond);
        }
        communities.add(newCommunity);
        return newCommunity;
    }

    private void createAndInsertTriple(double distance, Community first, Community second) {
        final Triple triple = new Triple(distance, first, second);
        second.usages.put(first, triple);
        first.usages.put(second, triple);
        heap.add(triple);
    }

    private void invalidateTriple(Triple triple) {
        triple.first.usages.remove(triple.second, triple);
        triple.second.usages.remove(triple.first, triple);
        heap.remove(triple);
    }

    private class Community implements Comparable<Community> {

        private final Set<Entity> entities;
        private final Map<Community, Triple> usages = new HashMap<>();
        private final String name;
        private final long id;

        Community(Set<Entity> entities, String name) {
            this.entities = entities;
            this.name = name;
            id = idGenerator++;
        }

        Community(Entity entity) {
            this(Collections.singleton(entity), entity.getName());
        }

        @Override
        public String toString() {
            return name + "(id = " + id + ")";
        }

        @Override
        public int compareTo(@NotNull Community o) {
            if (o == this) {
                return 0;
            }
            if (!name.equals(o.name)) {
                return name.compareTo(o.name);
            }
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
