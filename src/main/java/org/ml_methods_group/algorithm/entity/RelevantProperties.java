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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;

import static org.ml_methods_group.utils.PsiSearchUtil.getHumanReadableName;

public class RelevantProperties implements Serializable{

    private static final long serialVersionUID = -2410408921702449774L;
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

    public Set<String> getFields() {
        return Collections.unmodifiableSet(fields.keySet());
    }

    public Set<String> getMethods() {
        return Collections.unmodifiableSet(methods.keySet());
    }
    
    public Set<String> getAllMethods() {
        return Collections.unmodifiableSet(allMethods.keySet());
    }

    public Set<String> getClasses() {
        return Collections.unmodifiableSet(classes.keySet());
    }

    public int size() {
        return getWeightedSize(classes) + getWeightedSize(fields) + getWeightedSize(methods);
    }

    public int getWeight(String name) {
        return classes.getOrDefault(name, 0)
                + methods.getOrDefault(name, 0)
                + fields.getOrDefault(name, 0);
    }

    private int getWeightedSize(Map<?, Integer> m) {
        return m.values().stream().mapToInt(Integer::valueOf).sum();
    }

    public int sizeOfIntersection(RelevantProperties properties) {
        int result = 0;

        final BinaryOperator<Integer> bop = Math::min;
        result += sizeOfIntersectWeighted(classes, properties.classes, bop);
        result += sizeOfIntersectWeighted(allMethods, properties.allMethods, bop);
        result += sizeOfIntersectWeighted(fields, properties.fields, bop);

        return result;
    }

    private static int sizeOfIntersectWeighted(Map<?, Integer> m1, Map<?, Integer> m2, BinaryOperator<Integer> f) {
        return m1.entrySet().stream()
                .filter(e -> m2.containsKey(e.getKey()))
                .mapToInt(e -> f.apply(e.getValue(), m2.get(e.getKey())))
                .sum();
    }

    public int sizeOfUnion(RelevantProperties other) {
        int result = 0;

        final BinaryOperator<Integer> bop = Math::max;
        result += size() + other.size();
        result -= sizeOfIntersectWeighted(classes, other.classes, bop);
        result -= sizeOfIntersectWeighted(allMethods, other.allMethods, bop);
        result -= sizeOfIntersectWeighted(fields, other.fields, bop);
        return result;
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