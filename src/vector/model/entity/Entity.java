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

package vector.model.entity;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import vector.model.PropertiesFinder;
import vector.model.RelevantProperties;

import java.util.*;

public abstract class Entity {
    private final PsiElement psiEntity;
    protected double[] vector;
    private final RelevantProperties relevantProperties;
    private final String name;
    protected static final Map<String, Integer> components;

    public static final int DIMENSION = 4;

    static {
        final Map<String, Integer> comps = new HashMap<>();
        comps.put("DIT", 0);
        comps.put("NOC", 1);
        comps.put("FIC", 2);
        comps.put("FOC", 3);
        comps.put("FIM", 2);
        comps.put("FOM", 3);
        components = Collections.unmodifiableMap(comps);
    }

    public Entity(String name, MetricsRunImpl metricsRun, PropertiesFinder propertiesFinder) {
        this.name = name;
        vector = initializeVector(metricsRun);
        relevantProperties = propertiesFinder.getProperties(this.name);
        psiEntity = propertiesFinder.getPsiElement(name);
    }

    public double dist(Entity entity) {
        double ans = 0.0;
        for (int i = 0; i < DIMENSION; i++) {
            ans += Math.pow(vector[i] - entity.vector[i], 2);
        }

        final int rpIntersect = entity.relevantProperties.sizeOfIntersect(relevantProperties);
        if (rpIntersect == 0) {
            return Double.MAX_VALUE;
        }
        ans += 2.0 * (1 - rpIntersect /
                (1.0 * relevantProperties.size() + entity.relevantProperties.size() - rpIntersect));

        ans /= DIMENSION + 2;
        return Math.sqrt(ans);
    }

    public static void normalize(Iterable<Entity> entities) {
        for (int i = 0; i < DIMENSION; i++) {
            double mx = 0.0;
            for (Entity entity : entities) {
                mx = Math.max(mx, entity.vector[i]);
            }

            if (mx == 0.0) {
                continue;
            }

            for (Entity entity : entities) {
                entity.vector[i] /= mx;
            }
        }
    }

    public void moveToClass(PsiClass newClass) {
        final Set<PsiClass> oldClasses = relevantProperties.getAllClasses();
        for (PsiClass oldClass : oldClasses) {
            relevantProperties.removeClass(oldClass);
        }
        relevantProperties.addClass(newClass);
    }

    public void removeFromClass(PsiMethod method) {
        relevantProperties.removeMethod(method);
    }

    public void removeFromClass(PsiField field) {
        relevantProperties.removeField(field);
    }

    public RelevantProperties getRelevantProperties() {
        return relevantProperties;
    }

    public double[] getVector() {
        return vector;
    }

    public String getName() {
        return name;
    }

    public void print() {
        System.out.println(name + ": " + getCategory().name());
        System.out.print("    ");
        for (double comp : vector) {
            System.out.print(comp);
            System.out.print(" ");
        }
        System.out.println();

        relevantProperties.printAll();
    }

    public PsiElement getPsiElement() {
        return psiEntity;
    }

    protected static void processEntity(String name, MetricCategory category, MetricsResult results
            , MetricsRun metricsRun, double[] vector) {
        for (Metric metric : metricsRun.getMetrics()) {
            if (metric.getCategory() == category) {
                final int id = components.get(metric.getAbbreviation()).intValue();
                if (results.getValueForMetric(metric, name) != null) {
                    vector[id] = results.getValueForMetric(metric, name);
                }
            }
        }
    }

    protected abstract double[] initializeVector(MetricsRunImpl metricsRun);
    protected abstract HashSet<String> findRelevantProperties();
    abstract public MetricCategory getCategory();
    abstract public String getClassName();
}
