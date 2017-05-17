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

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.utils.MethodUtils;

import java.util.*;

/**
 * Created by Kivi on 02.05.2017.
 */
public class CCDA {
    public CCDA(List<Entity> entities) {
        communityId = new HashMap<String, Integer>();
        idCommunity = new ArrayList<String>();
        nodes = new ArrayList<Entity>();
        q = 0.0;
        for (Entity ent : entities) {
            if (ent.getCategory().equals(MetricCategory.Class)) {
                Integer id = communityId.size() + 1;
                communityId.put(ent.getName(), id);
                idCommunity.add(ent.getName());
            }
        }
        for (Entity ent : entities) {
            if (!ent.getCategory().equals(MetricCategory.Class) && communityId.containsKey(ent.getClassName())) {
                nodes.add(ent);
                communityId.put(ent.getName(), communityId.get(ent.getClassName()));
            }
        }

        aCoefficients = new ArrayList<Integer>(Collections.nCopies(idCommunity.size() + 1, 0));
        buildGraph();
    }

    public void applyRefactorings(Map<String, String> refactorings) {
        for (String entity : refactorings.keySet()) {
            String com = refactorings.get(entity);
            if (!communityId.containsKey(com)) {
                communityId.put(com, communityId.size() + 1);
                idCommunity.add(com);
            }
            communityId.put(entity, communityId.get(com));
        }
    }

    public void buildGraph() {
        graph = new HashMap<String, HashSet<String>>();
        for (Entity ent : nodes) {
            RelevantProperties rp = ent.getRelevantProperties();
            HashSet<String> neighbors = new HashSet<String>();
            if (graph.containsKey(ent.getName())) {
                neighbors = graph.get(ent.getName());
            }
            Set<PsiMethod> methods = rp.getAllMethods();
            for (PsiMethod method : methods) {
                String name = MethodUtils.calculateSignature(method);
                if (name.equals(ent.getName()) || !communityId.containsKey(name)) {
                    continue;
                }

                neighbors.add(name);
                if (!graph.containsKey(name)) {
                    graph.put(name, new HashSet<String>());
                }

                graph.get(name).add(ent.getName());
            }

            for (PsiField field : rp.getAllFields()) {
                String name = field.getContainingClass().getQualifiedName() + "." + field.getName();
                if (name.equals(ent.getName()) || !communityId.containsKey(name)) {
                    continue;
                }
                neighbors.add(name);
                if (!graph.containsKey(name)) {
                    graph.put(name, new HashSet<String>());
                }

                graph.get(name).add(ent.getName());
            }

            graph.put(ent.getName(), neighbors);
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

    public Map<String, String> run() {
        Map<String, String> refactorings = new HashMap<String, String>();
        q = calculateQualityIndex();
        System.out.println(q);

        Double dq = 1.0;
        System.out.println("Running...");
        while (dq > eps) {
            dq = 0.0;
            int id = -1;
            Integer community = -1;
            for (int i = 0; i < nodes.size(); ++i) {
                Entity ent = nodes.get(i);
                for (int j = 1; j <= idCommunity.size(); ++j) {
                    if (j == communityId.get(ent.getName())) {
                        continue;
                    }
                    Double curdq = move(ent, j, true);
                    if (curdq > dq) {
                        dq = curdq;
                        id = i;
                        community = j;
                    }
                }
            }

            if (dq > eps) {
                refactorings.put(nodes.get(id).getName(), idCommunity.get(community - 1));
                move(nodes.get(id), community, false);
                communityId.put(nodes.get(id).getName(), community);
                System.out.println("move " + nodes.get(id).getName() + " to " + idCommunity.get(community - 1));
                System.out.println("quality index is now: " + q);
                System.out.println();
            }
        }

        return refactorings;
    }

    public Double move(Entity ent, Integer to, boolean rollback) {
        String name = ent.getName();
        Integer from = communityId.get(name);
        Double dq = 0.0;
        dq += Math.pow(aCoefficients.get(from) * 1.0 / edges, 2);
        dq += Math.pow(aCoefficients.get(to) * 1.0 / edges, 2);

        Integer aFrom = 0;
        Integer aTo = 0;
        Integer de = 0;

        for (String neighbor : graph.get(name)) {
            if (communityId.get(neighbor).equals(from)) {
                de--;
            } else {
                aFrom++;
            }

            if (communityId.get(neighbor).equals(to)) {
                de++;
            } else {
                aTo++;
            }
        }

        aFrom = aCoefficients.get(from) - aFrom;
        aTo = aCoefficients.get(to) + aTo;

        if (!rollback) {
            aCoefficients.add(from, aFrom);
            aCoefficients.add(to, aTo);
        }

        dq += de * 1.0 / edges;
        dq -= Math.pow(aFrom * 1.0 / edges, 2);
        dq -= Math.pow(aTo * 1.0 / edges, 2);

        if (!rollback) {
            q += dq;
        }

        return dq;
    }

    public Double calculateQualityIndex() {
        System.out.println("Calculating Q...");
        Double qI = 0.0;
        edges = 0;
        for (String node : graph.keySet()) {
            edges += graph.get(node).size();
        }

        edges /= 2;
        System.out.println(edges);
        for (int i = 1; i <= idCommunity.size(); ++i) {
            String com = idCommunity.get(i - 1);
            Integer e = 0;
            Integer a = 0;

            for (String node : graph.keySet()) {
                if (!communityId.containsKey(node)) {
                    System.out.println("ERROR: unknown community");
                    System.out.println(node);
                }

                if (!communityId.get(node).equals(communityId.get(com))) {
                    continue;
                }
                for (String neighbor : graph.get(node)) {
                    if (communityId.get(neighbor).equals(communityId.get(com))) {
                        e++;
                    } else {
                        a++;
                    }
                }
            }

            e /= 2;
            a += e;
            aCoefficients.add(i, a);
            qI += (e * 1.0 / edges) - Math.pow((a * 1.0 / edges), 2);
        }

        return qI;
    }

    private Map<String, Integer> communityId;
    private ArrayList<String> idCommunity;
    private ArrayList<Integer> aCoefficients;
    private Map<String, HashSet<String>> graph;

    private List<Entity> nodes;

    private Double q;
    private Integer edges;

    private static Double eps = 5e-4;
}
