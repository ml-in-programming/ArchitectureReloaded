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
public class ARI {
    public ARI(List<Entity> entityList) {
        methodsAndFields = new ArrayList<Entity>();
        classes = new ArrayList<ClassEntity>();
        for (Entity entity : entityList) {
            String name = entity.getName();
            entityByName.put(name, entity);
            if (entity.getCategory() == MetricCategory.Class) {
                classes.add((ClassEntity) entity);
            } else {
                methodsAndFields.add(entity);
            }
        }
    }

    public Map<String, String> run() {
        Map<String, String> refactorings = new HashMap<String, String>();

        for (Entity method : methodsAndFields) {
            double minD = Double.MAX_VALUE;
            int classId = -1;
            for (int i = 0; i < classes.size(); i++) {
                ClassEntity cl = classes.get(i);
                double d = method.dist(cl);
                if (method.getName().equals("class_B.methodB1()")) {
                    System.out.println(cl.getName() + " " + d);

                }
                if (d < minD) {
                    minD = d;
                    classId = i;
                }
            }

            if (classId == -1) {
                System.out.println("HOW??? " + method.getName());
            }

            ClassEntity cl = classes.get(classId);
            if (communityIds.containsKey(cl)) {
                putMethodOrField(method, cl);
            } else {
                createCommunity(method, cl);
            }

            System.out.println("Add " + method.getName() + " to " + cl.getName());
        }

        for (Entity ent : methodsAndFields) {
            if (!ent.getClassName().equals(communityIds.get(ent))) {
                refactorings.put(ent.getName(), communityIds.get(ent));
            }
        }

        return refactorings;
    }

    private void createCommunity(Entity method, Entity cl) {
        communityIds.put(cl, cl.getName());
        communityIds.put(method, cl.getName());
        Set<Entity> newCommunity = new HashSet<Entity>();
        newCommunity.add(method);
        newCommunity.add(cl);
        communities.put(cl.getName(), newCommunity);
    }

    private void putMethodOrField(Entity method, Entity cl) {
        communityIds.put(method, cl.getName());
        communities.get(cl.getName()).add(method);
    }

    private HashMap<String, Set<Entity>> communities = new HashMap<String, Set<Entity>>();
    private HashMap<Entity, String> communityIds = new HashMap<Entity, String>();

    private List<Entity> methodsAndFields;
    private List<ClassEntity> classes;
    private HashMap<String, Entity> entityByName = new HashMap<String, Entity>();
}
