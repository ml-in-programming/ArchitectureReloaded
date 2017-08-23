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

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;
import org.ml_methods_group.algorithm.entity.MethodEntity;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.utils.LabelUtils;
import org.ml_methods_group.utils.LabeledRefactorings;
import org.ooxo.LProp;
import org.ooxo.openapi.Graph;
import org.ooxo.openapi.Label;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public final class LabelPropagationAdapter {
    private static Logger LOG = Logging.getLogger(LabelPropagationAdapter.class);

    private Map<Refactoring, Long> refactoringsToId = new HashMap<>();
    private Map<String, String> goodRefactorings = Collections.emptyMap();
    private Map<String, String> badRefactorings = Collections.emptyMap();
    private Map<Refactoring, Label> labeled = new HashMap<>();
    private final Map<String, String> result = new HashMap<>();

    public void calculate(final @NotNull EntitySearchResult entities,
                          final @NotNull Set<LabeledRefactorings.LabeledRefactoring> refactorings,
                          final @NotNull BiPredicate<MethodEntity, ClassEntity> p) {
        init();
        if (false) {
            Graph g = new Graph();
            g.add(1, Label.NO_LABEL, 2, new Label(1), 1.0);
            g.add(1, Label.NO_LABEL, 3, Label.NO_LABEL, 1.0);

            g.add(2, new Label(1), 1, new Label(0), 1.0);
            g.add(2, new Label(1), 3, new Label(0), 1.0);

            g.add(3, Label.NO_LABEL, 1, new Label(0), 1.0);
            g.add(3, Label.NO_LABEL, 2, new Label(1), 1.0);
            g.add(3, Label.NO_LABEL, 4, new Label(0), 1.0);

            g.add(4, Label.NO_LABEL, 3, new Label(0), 1.0);
            g.add(4, Label.NO_LABEL, 5, new Label(0), 1.0);
            g.add(4, Label.NO_LABEL, 8, new Label(0), 1.0);

            g.add(5, Label.NO_LABEL, 4, new Label(0), 1.0);
            g.add(5, Label.NO_LABEL, 6, new Label(2), 1.0);
            g.add(5, Label.NO_LABEL, 7, new Label(0), 1.0);

            g.add(6, new Label(2), 5, new Label(0), 1.0);
            g.add(6, new Label(2), 7, new Label(0), 1.0);

            g.add(7, Label.NO_LABEL, 5, new Label(0), 1.0);
            g.add(7, Label.NO_LABEL, 6, new Label(2), 1.0);

            g.add(8, Label.NO_LABEL, 4, new Label(0), 1.0);
            g.add(8, Label.NO_LABEL, 9, new Label(2), 1.0);

            g.add(9, new Label(2), 8, new Label(0), 1.0);

            final Map<Long, Label> res = LProp.setLabels(g, 1e-4, 500);
            res.entrySet().stream()
                    .map(e -> String.format("id %s, l %s", e.getKey(), e.getValue().getId()))
                    .forEachOrdered(LOG::debug);
        }
        long id = 0;
        labeled.putAll(refactorings.stream()
                        .collect(Collectors.toMap(
                                l -> l.refactoring,
                                l -> l.label
                        )));
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
        for (Refactoring r : labeled.keySet()) {
            refactoringsToId.put(r, id++);
        }
        for (Refactoring r1 : refactoringsToId.keySet()) {
            for (Refactoring r2 : refactoringsToId.keySet()) {
                if (r1.equals(r2)) {
                    continue;
                }
                double w = getWeight(r1, r2);
                if (w < MIN_WEIGHT) {
                    continue;
                }
                g.add(refactoringsToId.get(r1), getLabel(r1),
                        refactoringsToId.get(r2), getLabel(r2),
                        w);
            }
        }

        final Map<Long, Label> res = LProp.setLabels(g, 1e-6, 500);
        final Map<String, Integer> ind = new HashMap<>();
        for (Map.Entry<Refactoring, Long> e : refactoringsToId.entrySet()) {
            if (e.getKey().isSameClass()) {
                continue;
            }
            if (!res.containsKey(e.getValue()) || !(LabelUtils.isGood(res.get(e.getValue())))) {
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
//        return result;
//        return refactoringsToId.entrySet().stream()
//                .filter(e -> !e.getKey().isSameClass())
//                .filter(e -> res.containsKey(e.getValue()) && res.get(e.getValue()).getId() == GOOD_LABEL_ID)
//                .map(Map.Entry::getKey)
//                .collect(Collectors.toMap(
//                        (Refactoring r) -> r.getMethod().getName(),
//                        (Refactoring r) -> r.getTo().getName())
//                );
    }

    private void init() {
        result.clear();
        refactoringsToId.clear();
        labeled.clear();
    }

    public Map<String, String> getResult() {
        return Collections.unmodifiableMap(result);
    }

    private Label getLabel(@NotNull final Refactoring r) {
        return labeled.getOrDefault(r, LabelUtils.getEmptyLabel());
//        final String key = r.getMethod().getName();
//        final String value = r.getTo().getName();
//        if (goodRefactorings.containsKey(key) && value.equals(goodRefactorings.get(key))) {
//            return LabelUtils.getGoodLabel();
//        }
//        if (badRefactorings.containsKey(key) && value.equals(badRefactorings.get(key))) {
//            return LabelUtils.getBadLabel();
//        }
//        if (r.isSameClass()) {
//            return new Label(GOOD_LABEL_ID);
//        }
//        return LabelUtils.getEmptyLabel();
    }

    private final static double MIN_WEIGHT = 20.0;

    private static double getWeight(Refactoring from, Refactoring to) {
        double w = 0.0;
        w += 2 * getWeight(from.getMethod(), to.getMethod());
        w += getWeight(from.getFrom(), to.getFrom());
        w += getWeight(from.getTo(), to.getTo());
//        return (Entity.DIMENSION - 2) * 4 - w;
//        w = Math.max(0, 1 - w) * 100;
        w = Math.min(100.0, 1.0 / w);
//        LOG.debug(String.format("w = %4f : %s - %s", w, from, to));
        return w;
    }

    private static double getWeight(Entity from, Entity to) {
        double w = 0.0;
        for (int i = 2; i < Entity.DIMENSION; i++) {
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

    public static class Refactoring implements Serializable {
        private static final long serialVersionUID = -5457316138660101143L;
        @NotNull
        private final MethodEntity method;
        @NotNull
        private final ClassEntity from;
        @NotNull
        private final ClassEntity to;
        private final boolean sameClass;


        public Refactoring(@NotNull MethodEntity method, @NotNull ClassEntity from, @NotNull ClassEntity to) {
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
        public String toString() {
            return String.format("%s -> %s", method.getName(), to.getName());
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
