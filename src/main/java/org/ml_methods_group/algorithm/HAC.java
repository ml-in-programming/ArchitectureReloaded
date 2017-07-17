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

public class HAC {
    private final SortedSet<Triple> heap = new TreeSet<>();
    private final Set<Community> communities = new HashSet<>();
    private long ID_GENERATOR = 0;

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

    public HAC(Collection<Entity> entityList) {
        entityList.stream()
                .map(entry -> new Community(Collections.singleton(entry), entry.getName()))
                .forEach(communities::add);

        for (Community first : communities) {
            final Entity representative = getRepresentative(first);
            for (Community second : communities) {
                if (first == second) {
                    break;
                }
                final double distance = representative.distance(getRepresentative(second));
                if (distance != getRepresentative(second).distance(representative))
                    System.out.println("Distances: " + distance + " vs " + getRepresentative(second).distance(representative));
                final Triple triple = new Triple(distance, first, second);
                first.usages.put(second, triple);
                second.usages.put(first, triple);
                heap.add(triple);
            }
        }
    }

    private Entity getRepresentative(Community community) {
        if (community.entities.size() != 1) {
            throw new IllegalArgumentException("Something went wrong! Singleton set expected");
        }
        return community.entities.iterator().next();
    }

    public Map<String, String> run() {
        final Map<String, String> refactorings = new HashMap<String, String>();

        while (!heap.isEmpty()) {
            final Triple min = heap.first();
            if (min.distance > 1.0) {
                break;
            }

            final Community first = min.first;
            final Community second = min.second;
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

    private String calculateClassName(Community community) {
        return community.entities.stream()
                .collect(Collectors.groupingBy(Entity::getClassName, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
    }

    private int newClassCount = 0;

    private String receiveClassName(Community community) {
        final String name = calculateClassName(community);
        return name.isEmpty() ? "NewClass" + newClassCount++ : name;
    }

    private Community mergeCommunities(Community first, Community second) {
        System.out.println("merge " + first.name + ", " + second.name);
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
        heap.remove(first.usages.get(second));

        for (Community community : communities) {

            final Triple fromFirst = first.usages.get(community);
            final Triple fromSecond = second.usages.get(community);
            final double newDistance = Math.max(fromFirst.distance, fromSecond.distance);

            final Triple fromNew = new Triple(newDistance, newCommunity, community);
            community.usages.remove(first);
            community.usages.remove(second);
            community.usages.put(newCommunity, fromNew);
            newCommunity.usages.put(community, fromNew);

            heap.remove(fromFirst);
            heap.remove(fromSecond);
            heap.add(fromNew);
        }
        communities.add(newCommunity);
        return newCommunity;
    }

    private class Community implements Comparable<Community> {

        private final Set<Entity> entities;
        private final Map<Community, Triple> usages = new HashMap<>();
        private final String name;
        private final long id;

        Community(Set<Entity> entities, String name) {
            this.entities = entities;
            this.name = name;
            id = ID_GENERATOR++;
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
}
