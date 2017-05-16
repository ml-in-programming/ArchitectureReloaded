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
    public HAC(List<Entity> entityList) {
        entities = entityList;
        for (Entity entity : entities) {
            String name = entity.getName();
            HashSet<Entity> simpleCommunity = new HashSet<Entity>();
            simpleCommunity.add(entity);
            communities.put(name, simpleCommunity);
            communityIds.put(entity, name);
            entityByName.put(name, entity);
        }
    }

    public Map<String, String> run() {
        Map<String, String> refactorings = new HashMap<String, String>();

        double minD = 0.0;
        while (minD < 1.0) {
            minD = Double.MAX_VALUE;
            String id1 = "";
            String id2 = "";
            for (Entity ent1 : communityIds.keySet()) {
                final String i = communityIds.get(ent1);
                for (Entity ent2 : communityIds.keySet()) {
                    if (ent1.getCategory() == MetricCategory.Class && ent2.getCategory() == MetricCategory.Class) {
                        continue;
                    }
                    final String j = communityIds.get(ent2);
                    if (i.equals(j)) {
                        continue;
                    }

                    final double d = ent1.dist(ent2);
                    if (d < minD) {
                        if (entityByName.get(i).getCategory() == MetricCategory.Class
                                && entityByName.get(j).getCategory() == MetricCategory.Class) {
                            continue;
                        }

                        minD = d;
                        id1 = i;
                        id2 = j;
                    }
                }
            }

            if (minD > 1.0) {
                continue;
            }

            if (entityByName.get(id1).getCategory() == MetricCategory.Class
                    && entityByName.get(id2).getCategory() == MetricCategory.Class) {
                minD = Double.MAX_VALUE;
                continue;
            }

            mergeCommunities(id1, id2);
            System.out.println("Merge " + id1 + " and " + id2 +  " to " + communityIds.get(entityByName.get(id1)));
        }

        for (Entity ent : communityIds.keySet()) {
            if (!ent.getClassName().equals(communityIds.get(ent))) {
                refactorings.put(ent.getName(), communityIds.get(ent));

            }
        }

        return refactorings;
    }

    private void mergeCommunities(String id1, String id2) {
        String newName = id1;
        Set<Entity> merge = new HashSet<Entity>(communities.get(id1));
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
    }

    private HashMap<String, Set<Entity>> communities = new HashMap<String, Set<Entity>>();
    private HashMap<Entity, String> communityIds = new HashMap<Entity, String>();

    private List<Entity> entities;
    private HashMap<String, Entity> entityByName = new HashMap<String, Entity>();
}
