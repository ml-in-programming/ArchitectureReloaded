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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRunImpl;

import java.util.*;

public abstract class Entity {

    public Entity(String entity_name, MetricsRunImpl metricsRun, PropertiesFinder propertiesFinder) {
        name = entity_name;
        vector = initializeVector(metricsRun);
        relevantProperties = propertiesFinder.getProperties(name);
        psiEntity = propertiesFinder.getPsiElement(entity_name);
    }

    public double dist(Entity entity) {
        double ans = 0.0;
        for (int i = 0; i < Dimension; i++) {
            ans += Math.pow(vector[i] - entity.vector[i], 2);
        }

        int rpIntersect = entity.relevantProperties.sizeOfIntersect(relevantProperties);
        if (rpIntersect == 0) {
            return Double.MAX_VALUE;
        }
        ans += 2.0 * (1 - (rpIntersect) / (1.0 * relevantProperties.size() + entity.relevantProperties.size() -
                rpIntersect));

        ans /= (Dimension + 2);
        return Math.sqrt(ans);
    }

    public static void normalize(ArrayList<Entity> entities) {
        for (int i = 0; i < Dimension; i++) {
            Double mx = 0.0;
            for (Entity entity : entities) {
                mx = Math.max(mx, entity.vector[i]);
            }

            if (mx == 0.0) {
                continue;
            }

            for (int j = 0; j < entities.size(); j++) {
                entities.get(j).vector[i] /= mx;
            }
        }
    }

    public void moveToClass(PsiClass newClass) {
        Set<PsiClass> oldClasses = relevantProperties.getAllClasses();
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

    public static final int Dimension = 4;

    public RelevantProperties getRelevantProperties() {
        return relevantProperties;
    }

    abstract MetricCategory getCategory();

    public Double[] getVector() {
        return vector;
    }

    public String getName() {
        return name;
    }

    abstract public String getClassName();

    protected Double[] vector;
    private RelevantProperties relevantProperties;
    private String name;

    protected abstract Double[] initializeVector(MetricsRunImpl metricsRun);
    protected abstract HashSet<String> findRelevantProperties();

    protected static final Map<String, Integer> components;
    static {
        Map<String, Integer> comps = new HashMap<String, Integer>();
        comps.put("DIT", 0);
        comps.put("NOC", 1);
        comps.put("FIC", 2);
        comps.put("FOC", 3);
        comps.put("FIM", 2);
        comps.put("FOM", 3);
        components = Collections.unmodifiableMap(comps);
    }

    public void print() {
        System.out.println(name + ": " + getCategory().name());
        System.out.print("    ");
        for (Double comp : vector) {
            System.out.print(comp);
            System.out.print(" ");
        }
        System.out.println();

        relevantProperties.printAll();
    }

    private PsiElement psiEntity;

    public PsiElement getPsiElement() {
        return psiEntity;
    }
}
