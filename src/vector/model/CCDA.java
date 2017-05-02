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
            } else {
                nodes.add(ent);
            }
        }

        buildGraph();
    }

    public void buildGraph() {
        graph = new HashMap<String, HashSet<String>>();
        for (Entity ent : nodes) {
            communityId.put(ent.getName(), communityId.get(ent.getClassName()));
            RelevantProperties rp = ent.getRelevantProperties();
            HashSet<String> neighbors = new HashSet<String>();
            if (graph.containsKey(ent.getName())) {
                neighbors = graph.get(ent.getName());
            }
            Set<PsiMethod> methods = rp.getAllMethods();
            for (PsiMethod method : methods) {
                String name = MethodUtils.calculateSignature(method);
                if (name.equals(ent.getName())) {
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
                neighbors.add(name);
                if (!graph.containsKey(name)) {
                    graph.put(name, new HashSet<String>());
                }

                graph.get(name).add(ent.getName());
            }

            graph.put(ent.getName(), neighbors);
        }
    }

    public Map<String, String> run() {
        Map<String, String> refactorings = new HashMap<String, String>();
        q = calculateQualityIndex();
        System.out.println(q);

        return refactorings;
    }

    public Double calculateQualityIndex() {
        Double qI = 0.0;
        Integer edges = 0;
        for (String node : graph.keySet()) {
            edges += graph.get(node).size();
        }

        edges /= 2;

        System.out.println(edges);
        for (String com : idCommunity) {
            System.out.println(com);
            Integer e = 0;
            Integer a = 0;
            System.out.println(graph.size());

            for (String node : graph.keySet()) {
                System.out.println("  " + node);

                if (!communityId.containsKey(node)) {
                    System.out.println("ERROR: unknown community");
                }

                if (!communityId.get(node).equals(communityId.get(com))) {
                    continue;
                }
                for (String neighbor : graph.get(node)) {
                    System.out.println("  -> " + neighbor);
                    if (communityId.get(neighbor).equals(communityId.get(com))) {
                        e++;
                    } else {
                        a++;
                    }
                }
            }

            e /= 2;
            a += e;

            System.out.println("  " + e + " " + a);
            qI += (e * 1.0 / edges) + Math.pow((a * 1.0 / edges), 2);
            System.out.println("Q: " );
        }

        return qI;
    }

    private Map<String, Integer> communityId;
    private ArrayList<String> idCommunity;

    private Map<String, HashSet<String>> graph;

    private List<Entity> nodes;

    private Double q;
}
