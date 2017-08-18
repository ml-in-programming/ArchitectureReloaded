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

import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;
import org.ml_methods_group.algorithm.entity.MethodEntity;
import org.ooxo.LProp;
import org.ooxo.openapi.Graph;
import org.ooxo.openapi.Label;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;

public final class LabelPropagationAdapter {
    private static final int GOOD_LABEL_ID = 1;
    private static final int BAD_LABEL_ID = 2;
    private Map<Refactoring, Long> refactoringsToId = new HashMap<>();
    private Map<String, String> goodRefactorings;
    private Map<String, String> badRefactorings;

    public Map<String, String> calculate(EntitySearchResult entities,
                                                Map<String, String> good,
                                                Map<String, String> bad,
                                                @NotNull BiPredicate<MethodEntity, ClassEntity> p) {
        goodRefactorings = good;
        badRefactorings = bad;
        long id = 0;
        final List<MethodEntity> methods = entities.getMethods();
        final List<ClassEntity> classes = entities.getClasses();
        final Graph g = new Graph();
        for (MethodEntity m : methods) {
            for (ClassEntity c : classes) {
                if (!p.test(m, c)) {
                    continue;
                }
                final Refactoring r = new Refactoring(m, getClassEntity(m, classes), c);
                refactoringsToId.put(r, id);
                id++;
            }
        }
        for (Refactoring r1 : refactoringsToId.keySet()) {
            for (Refactoring r2 : refactoringsToId.keySet()) {
                if (r1.equals(r2)) {
                    continue;
                }
                g.add(refactoringsToId.get(r1), getLabel(r1),
                        refactoringsToId.get(r2), getLabel(r2),
                        getWeight(r1, r2));
            }
        }
        final Map<Long, Label> res = LProp.setLabels(g, 1e-4, 500);
        final Map<String, String> result = new HashMap<>();
        final Map<String, Integer> ind = new HashMap<>();
        for (Map.Entry<Refactoring, Long> e : refactoringsToId.entrySet()) {
            if (e.getKey().isSameClass()) {
                continue;
            }
            if (!res.containsKey(e.getValue()) || !(res.get(e.getValue()).getId() == GOOD_LABEL_ID)) {
                continue;
            }
            final String key = e.getKey().getMethod().getName();
            final String value = e.getKey().getTo().getName();
            if (result.containsKey(key)) {
                final Integer i = ind.getOrDefault(key, 0);
                result.put(String.format("%s %d", key, i + 1), value);
                ind.put(key, i + 1);
                continue;
            }
            result.put(key, value);
        }
        return result;
//        return refactoringsToId.entrySet().stream()
//                .filter(e -> !e.getKey().isSameClass())
//                .filter(e -> res.containsKey(e.getValue()) && res.get(e.getValue()).getId() == GOOD_LABEL_ID)
//                .map(Map.Entry::getKey)
//                .collect(Collectors.toMap(
//                        (Refactoring r) -> r.getMethod().getName(),
//                        (Refactoring r) -> r.getTo().getName())
//                );
    }

    private Label getLabel(@NotNull final Refactoring r) {
        final String key = r.getMethod().getName();
        final String value = r.getTo().getName();
        if (goodRefactorings.containsKey(key) && value.equals(goodRefactorings.get(key))) {
            return new Label(GOOD_LABEL_ID);
        }
        if (badRefactorings.containsKey(key) && value.equals(badRefactorings.get(key))) {
            return new Label(BAD_LABEL_ID);
        }
        if (r.isSameClass()) {
            return new Label(GOOD_LABEL_ID);
        }
        return Label.NO_LABEL;
    }

    private static double getWeight(Refactoring from, Refactoring to) {
        double w = 0.0;
        w += getWeight(from.getMethod(), to.getMethod());
        w += getWeight(from.getFrom(), to.getFrom());
        w += 2 * getWeight(from.getTo(), to.getTo());
        return 1.0 / (w + 1);
    }

    private static double getWeight(Entity from, Entity to) {
        double w = 0.0;
        for (int i = 2; i < 4; i++) {
            w += square(from.getMetric(i) - to.getMetric(i));
        }
        return w;
    }

    private static double square(double x) {
        return x * x;
    }

    private static ClassEntity getClassEntity(MethodEntity m, List<ClassEntity> list) {
        final String className = m.getClassName();
        return list.stream()
                .filter(c -> className.equals(c.getClassName()))
                .findAny()
                .orElse(null);
    }

    private class Refactoring {
        @NotNull private final MethodEntity method;
        @NotNull private final ClassEntity from;
        @NotNull private final ClassEntity to;
        private final boolean sameClass;


        private Refactoring(@NotNull MethodEntity method, @NotNull ClassEntity from, @NotNull ClassEntity to) {
            this.method = method;
            this.from = from;
            this.to = to;
            sameClass = from.equals(to);
        }

        @NotNull
        public MethodEntity getMethod() {
            return method;
        }

        @NotNull
        public ClassEntity getFrom() {
            return from;
        }

        @NotNull
        public ClassEntity getTo() {
            return to;
        }

        public boolean isSameClass() {
            return sameClass;
        }

        @Override
        public int hashCode() {
            return 53 * 53 * method.hashCode() + 53 * from.hashCode() + to.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Refactoring) {
                final Refactoring other = (Refactoring) obj;
                return Objects.equals(method, other.method)
                        && Objects.equals(from, other.from)
                        && Objects.equals(to, other.to);
            }
            return false;
        }
    }
}
