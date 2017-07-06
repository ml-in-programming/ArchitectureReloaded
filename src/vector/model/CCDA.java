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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.utils.MethodUtils;
import vector.model.entity.Entity;

import java.util.*;

public class CCDA {
    private final Map<String, Integer> communityIds;
    private final List<String> idCommunity;
    private final List<Integer> aCoefficients;
    private Map<String, Set<String>> graph;
    private final List<Entity> nodes;

    private double quality;
    private double edges;
    private static final double eps = 5e-4;

    public CCDA(Iterable<Entity> entities) {
        communityIds = new HashMap<>();
        idCommunity = new ArrayList<>();
        nodes = new ArrayList<>();
        quality = 0.0;

        for (Entity ent : entities) {
            if (ent.getCategory() == MetricCategory.Class) {
                communityIds.put(ent.getName(), Integer.valueOf(communityIds.size() + 1));
                idCommunity.add(ent.getName());
            }
        }

        for (Entity ent : entities) {
            if (ent.getCategory() != MetricCategory.Class && communityIds.containsKey(ent.getClassName())) {
                nodes.add(ent);
                communityIds.put(ent.getName(), communityIds.get(ent.getClassName()));
            }
        }

        aCoefficients = new ArrayList<>(Collections.nCopies(idCommunity.size() + 1, Integer.valueOf(0)));
        buildGraph();
    }

    public void applyRefactorings(Map<String, String> refactorings) {
        for (String entity : refactorings.keySet()) {
            final String com = refactorings.get(entity);
            if (!communityIds.containsKey(com)) {
                communityIds.put(com, Integer.valueOf(communityIds.size() + 1));
                idCommunity.add(com);
            }
            communityIds.put(entity, communityIds.get(com));
        }
    }

    private void buildGraph() {
        graph = new HashMap<>();
        for (Entity entity : nodes) {
            final RelevantProperties properties = entity.getRelevantProperties();
            final Set<String> neighbors = graph.getOrDefault(entity.getName(), new HashSet<>());
            final Set<PsiMethod> methods = properties.getAllMethods();

            methods.stream().map(MethodUtils::calculateSignature).forEach(name -> addNode(name, entity, neighbors));

            for (PsiField field : properties.getAllFields()) {
                final PsiClass containingClass = field.getContainingClass();
                assert containingClass != null;
                final String qualifiedFieldName = containingClass.getQualifiedName() + "." + field.getName();
                addNode(qualifiedFieldName, entity, neighbors);
            }

            graph.put(entity.getName(), neighbors);
        }

        System.out.println("Graph built:");
        for (String ent : graph.keySet()) {
            System.out.println(ent);
            for (String neighbor : graph.get(ent)) {
                System.out.println("  -> " + neighbor);
            }
        }
        System.out.println("-----");
    }

    private void addNode(String entityName, Entity entity, Collection<String> neighbors) {
        if (entityName.equals(entity.getName()) || !communityIds.containsKey(entityName)) {
            return;
        }
        neighbors.add(entityName);

        if (!graph.containsKey(entityName)) {
            graph.put(entityName, new HashSet<>());
        }

        graph.get(entityName).add(entity.getName());
    }

    public Map<String, String> run() {
        final Map<String, String> refactorings = new HashMap<>();
        quality = calculateQualityIndex();
        System.out.println(quality);

        double dq = 1.0;
        System.out.println("Running...");
        while (dq > eps) {
            dq = 0.0;
            Entity targetEntity = null;
            int community = -1;

            for (Entity entity : nodes) {
                for (int i = 1; i <= idCommunity.size(); ++i) {
                    if (i == communityIds.get(entity.getName()).intValue()) {
                        continue;
                    }
                    final double curdq = move(entity, i, true);
                    if (curdq > dq) {
                        dq = curdq;
                        targetEntity = entity;
                        community = i;
                    }
                }
            }

            if (dq > eps) {
                refactorings.put(targetEntity.getName(), idCommunity.get(community - 1));
                move(targetEntity, community, false);
                communityIds.put(targetEntity.getName(), Integer.valueOf(community));

                System.out.println("move " + targetEntity.getName() + " to " + idCommunity.get(community - 1));
                System.out.println("quality index is now: " + quality);
                System.out.println();
            }
        }

        return refactorings;
    }

    private double move(Entity ent, int to, boolean rollback) {
        final String name = ent.getName();
        final int from = communityIds.get(name).intValue();
        double dq = 0.0;
        dq += Math.pow(aCoefficients.get(from).doubleValue() * 1.0 / edges, 2.0);
        dq += Math.pow(aCoefficients.get(to).doubleValue() * 1.0 / edges, 2.0);

        int aFrom = 0;
        int aTo = 0;
        int de = 0;

        for (String neighbor : graph.get(name)) {
            if (communityIds.get(neighbor).intValue() == from) {
                de--;
            } else {
                aFrom++;
            }

            if (communityIds.get(neighbor).intValue() == to) {
                de++;
            } else {
                aTo++;
            }
        }

        aFrom = aCoefficients.get(from).intValue() - aFrom;
        aTo = aCoefficients.get(to).intValue() + aTo;

        if (!rollback) {
            aCoefficients.add(from, Integer.valueOf(aFrom));
            aCoefficients.add(to, Integer.valueOf(aTo));
        }

        dq += (double) de * 1.0 / edges;
        dq -= Math.pow((double) aFrom * 1.0 / edges, 2.0);
        dq -= Math.pow((double) aTo * 1.0 / edges, 2.0);

        if (!rollback) {
            quality += dq;
        }

        return dq;
    }

    public double calculateQualityIndex() {
        System.out.println("Calculating Q...");
        double qualityIndex = 0.0;

        edges = 0.0;
        for (String node : graph.keySet()) {
            edges += (double) graph.get(node).size();
        }
        edges /= 2.0;
        System.out.println(edges);

        for (int i = 1; i <= idCommunity.size(); ++i) {
            final String community = idCommunity.get(i - 1);
            int e = 0;
            int a = 0;

            for (String node : graph.keySet()) {
                if (!communityIds.containsKey(node)) {
                    System.out.println("ERROR: unknown community");
                    System.out.println(node);
                }

                if (!communityIds.get(node).equals(communityIds.get(community))) {
                    continue;
                }
                for (String neighbor : graph.get(node)) {
                    if (communityIds.get(neighbor).equals(communityIds.get(community))) {
                        e++;
                    } else {
                        a++;
                    }
                }
            }

            e /= 2;
            a += e;
            aCoefficients.add(i, Integer.valueOf(a));
            qualityIndex += ((double) e * 1.0 / edges) - Math.pow((double) a * 1.0 / edges, 2.0);
        }

        return qualityIndex;
    }
}
