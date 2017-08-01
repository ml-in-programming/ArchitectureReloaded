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

package org.ml_methods_group.algorithm.entity;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.ml_methods_group.utils.PsiSearchUtil.getHumanReadableName;

public class RelevantProperties {

    private final Map<String, Integer> methods = new HashMap<>();
    private final Map<String, Integer> classes = new HashMap<>();
    private final Map<String, Integer> fields = new HashMap<>();
    private final Map<String, Integer> allMethods = new HashMap<>();

    private final Integer DEFAULT_PROPERTY_WEIGHT = 1;

    void removeMethod(String method) {
        methods.remove(method);
    }

    void addMethod(String method) {
        addMethod(method, DEFAULT_PROPERTY_WEIGHT);
    }

    void addMethod(PsiMethod method) {
        addMethod(method, DEFAULT_PROPERTY_WEIGHT);
    }

    void addMethod(PsiMethod method, Integer weight) {
        addMethod(getHumanReadableName(method), weight);
    }

    void addMethod(String method, Integer weight) {
        if (methods.getOrDefault(method, 0) < weight) {
            methods.put(method, weight);
            allMethods.put(method, weight);
        }
    }

    void addClass(PsiClass aClass) {
        addClass(aClass, DEFAULT_PROPERTY_WEIGHT);
    }

    void addClass(PsiClass aClass, Integer weight) {
        String name = getHumanReadableName(aClass);
        if (classes.getOrDefault(name, 0) < weight) {
            classes.put(name, weight);
        }
    }

    void addField(PsiField field) {
        addField(field, DEFAULT_PROPERTY_WEIGHT);
    }

    void addField(PsiField field, Integer weight) {
        final String name = getHumanReadableName(field);
        if (fields.getOrDefault(name , 0) < weight) {
            fields.put(name, weight);
        }
    }

    void addOverrideMethod(PsiMethod method) {
        addOverrideMethod(method, DEFAULT_PROPERTY_WEIGHT);
    }

    void addOverrideMethod(PsiMethod method, Integer weight) {
        String name = getHumanReadableName(method);
        if (allMethods.getOrDefault(name, 0) < weight) {
            allMethods.put(getHumanReadableName(method), weight);
        }
    }

    int numberOfMethods() {
        return methods.size();
    }

    public Set<String> getAllFields() {
        return Collections.unmodifiableSet(fields.keySet());
    }

    public Set<String> getAllMethods() {
        return Collections.unmodifiableSet(methods.keySet());
    }
    public int size() {
        return getWeightedSize(classes) + getWeightedSize(fields) + getWeightedSize(methods);
    }

    private int getWeightedSize(Map<?, Integer> m) {
        return m.values().stream().mapToInt(Integer::valueOf).sum();
    }

    int sizeOfIntersection(RelevantProperties properties) {
        int result = 0;
        result += sizeOfIntersectWeighted(classes, properties.classes);
        result += sizeOfIntersectWeighted(allMethods, properties.methods);
        result += sizeOfIntersectWeighted(fields, properties.fields);

        return result;
    }

    private static int sizeOfIntersectWeighted(Map<?, Integer> m1, Map<?, Integer> m2) {
        return m1.entrySet().stream()
                .filter(e -> m2.containsKey(e.getKey()))
                .mapToInt(e -> Math.min(e.getValue(), m2.get(e.getKey())))
                .sum();
    }

    boolean hasCommonPrivateMember(RelevantProperties properties) {
        return false;
    }

    public RelevantProperties copy() {
        final RelevantProperties copy = new RelevantProperties();
        copy.classes.putAll(classes);
        copy.allMethods.putAll(allMethods);
        copy.methods.putAll(methods);
        copy.fields.putAll(fields);
        return copy;
    }
}