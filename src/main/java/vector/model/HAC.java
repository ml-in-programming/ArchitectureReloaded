/*
 *  Copyright 2017 Machine Learning Methods in Software Engineering Research Group
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

import com.sixrr.metrics.MetricCategory;
import org.jetbrains.annotations.NotNull;
import vector.model.entity.Entity;

import java.util.*;

public class HAC {
    private final Map<String, Set<Entity>> communities = new HashMap<>();
    private final Map<Entity, String> communityIds = new HashMap<>();
    private final Map<String, HashMap<String, Double>> dists = new HashMap<>();
    private final Map<String, Entity> entityByName = new HashMap<>();
    private final SortedSet<Triple> distances = new TreeSet<>();
    private int newClassCount = 0;

    private static class Triple implements Comparable<Triple> {
        private final double d;
        private final String c1;
        private final String c2;

        Triple(double d, String c1, String c2) {
            this.d = d;
            this.c1 = c1;
            this.c2 = c2;
        }

        @Override
        public int compareTo(@NotNull Triple other) {
            final int comparedD = Double.compare(d, other.d);
            if (comparedD != 0) {
                return comparedD;
            }

            final int comparedC1 = c1.compareTo(other.c1);
            if (comparedC1 != 0) {
                return comparedC1;
            }

            final int comparedC2 = c2.compareTo(other.c2);
            if (comparedC2 != 0) {
                return comparedC2;
            }

            return 0;
        }

        public double getD() {
            return d;
        }

        public String getC1() {
            return c1;
        }

        public String getC2() {
            return c2;
        }
    }

    public HAC(Iterable<Entity> entityList) {
        for (Entity entity : entityList) {
            final String name = entity.getName();
            final Set<Entity> simpleCommunity = new HashSet<>();
            simpleCommunity.add(entity);
            communities.put(name, simpleCommunity);
            communityIds.put(entity, name);
            entityByName.put(name, entity);
        }

        for (String com1 : entityByName.keySet()) {
            dists.put(com1, new HashMap<>());
            for (String com2 : entityByName.keySet()) {
                final double d = entityByName.get(com1).dist(entityByName.get(com2));
                dists.get(com1).put(com2, Double.valueOf(d));
                if (com1.compareTo(com2) < 0) {
                    distances.add(new Triple(d, com1, com2));
                }
            }
        }

        System.out.println(distances.first().getD());
    }

    public Map<String, String> run() {
        final Map<String, String> refactorings = new HashMap<String, String>();

        double minD = 0.0;
        while (minD < 1.0 && !distances.isEmpty()) {
            final Triple min = distances.first();
            minD = min.getD();

            if (minD > 1.0) {
                continue;
            }

            final String id1 = min.getC1();
            final String id2 = min.getC2();
            mergeCommunities(id1, id2);
            System.out.println("Merge " + id1 + " and " + id2 +  " to " + communityIds.get(entityByName.get(id1)));
        }

        for (String center : communities.keySet()) {
            final String newName = receiveClassName(center);
            for (Entity entity : communities.get(center)) {
                if (!entity.getClassName().equals(newName)) {
                    refactorings.put(entity.getName(), newName);
                }
            }
        }

        return refactorings;
    }

    private String calculateClassName(String center) {
        String name = "";
        int maxClassCount = 0;
        final Map<String, Integer> classCounts = new HashMap<>();
        for (Entity entity : communities.get(center)) {
            final String className = entity.getClassName();
            if (!classCounts.containsKey(className)) {
                classCounts.put(className, 0);
            }

            int count = classCounts.get(className).intValue();
            count++;
            classCounts.put(className, Integer.valueOf(count));
        }

        for (String className : classCounts.keySet()) {
            final int currentClassCount = classCounts.get(className).intValue();
            if (maxClassCount < currentClassCount) {
                maxClassCount = currentClassCount;
                name = className;
            }
        }

        return name;
    }

    private String receiveClassName(String center) {
        String name = calculateClassName(center);

        if (name.isEmpty()) {
            newClassCount++;
            name = "NewClass" + newClassCount;
        }

        return name;
    }

    private void mergeCommunities(String id1, String id2) {
        String newName = id1;
        final Set<Entity> merged = new HashSet<>(communities.get(id1));
        merged.addAll(communities.get(id2));

        long maxInClass = 0L;
        for (Entity ent : merged) {
            if (ent.getCategory() == MetricCategory.Class) {
                final long inClass = merged.stream().filter(e -> e.getClassName().equals(ent.getName())).count();

                if (inClass > maxInClass) {
                    maxInClass = inClass;
                    newName = ent.getName();
                }
            }
        }

        communities.remove(id1);
        communities.remove(id2);
        communities.put(newName, merged);
        for (Entity ent : merged) {
            communityIds.put(ent, newName);
        }

        final double d = dists.get(id1).get(id2).doubleValue();

        dists.get(id1).remove(id2);
        dists.get(id2).remove(id1);

        distances.remove(new Triple(d, id1, id2));
        distances.remove(new Triple(d, id2, id1));

        for (String entity : communities.keySet()) {
            if (entity.equals(id1) || entity.equals(id2)) {
                continue;
            }

            final double d1 = dists.get(entity).get(id1).doubleValue();
            final double d2 = dists.get(entity).get(id2).doubleValue();
            final double newD = Math.max(d1, d2);

            dists.get(id1).put(entity, Double.valueOf(newD));
            dists.get(entity).put(id1, Double.valueOf(newD));
            dists.get(entity).remove(id2);
            dists.get(id2).remove(entity);

            distances.remove(new Triple(d1, entity, id1));
            distances.remove(new Triple(d1, id1, entity));
            distances.remove(new Triple(d2, entity, id2));
            distances.remove(new Triple(d2, id2, entity));

            if (id1.compareTo(entity) < 0) {
                distances.add(new Triple(newD, id1, entity));
            } else {
                distances.add(new Triple(newD, entity, id1));
            }
        }
    }
}
