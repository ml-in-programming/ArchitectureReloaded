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

package org.ooxo.openapi;

import java.util.*;

public final class Graph {
    private final Map<Vertex, Set<Edge>> graph = new HashMap<>();

    public class Edge {
        private Vertex a;
        private Vertex b;
        private double w;

        public Edge(Vertex a1, Vertex b1, double w1) {
            this.a = a1;
            this.b = b1;

            this.w = w1;
        }

        public Vertex getA() {
            return a;
        }

        public Vertex getB() {
            return b;
        }

        public double getW() {
            return w;
        }

        @Override
        public int hashCode() {
            return 31 * 31 * a.hashCode() + 31 * b.hashCode() + 31 * Double.hashCode(w);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (obj instanceof Edge) {
                final Edge e = (Edge) obj;
                return Objects.equals(a, e.a) && Objects.equals(b, e.b) && w == e.w;
            }
            return false;
        }
    }

    public class Vertex {
        private long id;
        private Label label;

        public Vertex(long id, Label label) {
            this.id = id;
            this.label = label;
        }

        public long getId() {
            return id;
        }

        public Label getLabel() {
            return label;
        }
    }

    public Graph() {

    }

    public void add(long from, Label lFrom, long to, Label lTo, double weight) {
        final Vertex v1 = new Vertex(from, lFrom);
        checkVertex(v1);
        graph.get(v1).add(new Edge(v1, new Vertex(to, lTo), weight));
    }

    public Set<Vertex> getVertices() {
        return Collections.unmodifiableSet(graph.keySet());
    }

    public Set<Edge> getEdges(final Vertex v) {
        return graph.containsKey(v) ? Collections.unmodifiableSet(graph.get(v)) : Collections.emptySet();
    }

    private void checkVertex(Vertex v) {
        if (!graph.containsKey(v)) {
            graph.put(v, new HashSet<>());
        }
    }

}
