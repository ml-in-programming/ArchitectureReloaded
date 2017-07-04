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

import com.sixrr.metrics.MetricCategory;

import java.util.*;

/**
 * Created by Kivi on 16.05.2017.
 */
public class HAC {

    private class Triple implements Comparable<Triple> {

        Triple(Double _d, String _c1, String _c2) {
            d = _d;
            c1 = _c1;
            c2 = _c2;
        }

        public int compareTo(Triple t) {
            int compD = d.compareTo(t.d);
            if (compD != 0) {
                return compD;
            }

            int compC1 = c1.compareTo(t.c1);
            if (compC1 != 0) {
                return compC1;
            }

            int compC2 = c2.compareTo(t.c2);
            if (compC2 != 0) {
                return compC2;
            }

            return 0;
        }

        public Double getD() {
            return d;
        }

        public String getC1() {
            return c1;
        }

        public String getC2() {
            return c2;
        }

        private Double d;

        private String c1, c2;
    }
    public HAC(List<Entity> entityList) {
        entities = entityList;
        for (Entity entity : entities) {
            final String name = entity.getName();
            final HashSet<Entity> simpleCommunity = new HashSet<Entity>();
            simpleCommunity.add(entity);
            communities.put(name, simpleCommunity);
            communityIds.put(entity, name);
            entityByName.put(name, entity);
        }

        for (String com1 : entityByName.keySet()) {
            dists.put(com1, new HashMap<String, Double>());
            for (String com2 : entityByName.keySet()) {
                Double d = entityByName.get(com1).dist(entityByName.get(com2));
                dists.get(com1).put(com2, d);
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
            Triple min = distances.first();
            minD = min.getD();
            String id1 = min.getC1();
            String id2 = min.getC2();

            if (minD > 1.0) {
                continue;
            }

            /*if (entityByName.get(id1).getCategory() == MetricCategory.Class
                    && entityByName.get(id2).getCategory() == MetricCategory.Class) {
                minD = Double.MAX_VALUE;
                continue;
            }*/

            mergeCommunities(id1, id2);
            System.out.println("Merge " + id1 + " and " + id2 +  " to " + communityIds.get(entityByName.get(id1)));
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

    private String calculateClassName(String center) {
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

        return name;
    }

    private String receiveClassName(String center) {
        String name = calculateClassName(center);

        if (name.equals("")) {
            newClassCount++;
            name = "NewClass" + newClassCount;
        }

        return name;
    }

    private void mergeCommunities(String id1, String id2) {
        String newName = id1;
        final Set<Entity> merge = new HashSet<Entity>(communities.get(id1));
        merge.addAll(communities.get(id2));
        int maxInClass = 0;
        for (Entity ent : merge) {
            if (ent.getCategory() == MetricCategory.Class) {
                int inClass = 0;
                for (Entity entity : merge) {
                    if (entity.getClassName().equals(ent.getName())) {
                        inClass++;
                    }
                }

                if (inClass > maxInClass) {
                    maxInClass = inClass;
                    newName = ent.getName();
                }
            }
        }

        communities.remove(id1);
        communities.remove(id2);
        communities.put(newName, merge);
        for (Entity ent : merge) {
            communityIds.put(ent, newName);
        }

        Double d = dists.get(id1).get(id2);

        dists.get(id1).remove(id2);
        dists.get(id2).remove(id1);

        distances.remove(new Triple(d, id1, id2));
        distances.remove(new Triple(d, id2, id1));

        for (String entity : communities.keySet()) {
            if (entity.equals(id1) || entity.equals(id2)) {
                continue;
            }
            Double d1 = dists.get(entity).get(id1);
            Double d2 = dists.get(entity).get(id2);
            Double newD = Math.max(d1, d2);
            dists.get(id1).put(entity, newD);
            dists.get(entity).put(id1, newD);
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

    private HashMap<String, Set<Entity>> communities = new HashMap<String, Set<Entity>>();
    private HashMap<Entity, String> communityIds = new HashMap<Entity, String>();
    private Map<String, HashMap<String, Double>> dists = new HashMap<String, HashMap<String, Double>>();

    private List<Entity> entities;
    private HashMap<String, Entity> entityByName = new HashMap<String, Entity>();

    private SortedSet<Triple> distances = new TreeSet<Triple>();
    private static int SampleSize = 5;
    private int newClassCount = 0;
}
