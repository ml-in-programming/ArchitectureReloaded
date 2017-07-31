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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RelevantProperties {
    private final Map<PsiMethod, Integer> methods = new HashMap<>();
    private final Map<PsiClass, Integer> classes = new HashMap<>();
    private final Map<PsiField, Integer> fields = new HashMap<>();
    private final Collection<Property<PsiMethod>> overrideMethods = new HashSet<>();

    private final Integer DEFAULT_PROPERTY_WEIGHT = 1;

    public void removeMethod(PsiMethod method) {
        methods.remove(method);
    }

    public void removeField(PsiField field) {
        fields.remove(field);
    }

    public void removeClass(PsiClass aClass) {
        classes.remove(aClass);
    }

    public void addMethod(PsiMethod method) {
        addMethod(method, DEFAULT_PROPERTY_WEIGHT);
    }

    public void addMethod(PsiMethod method, Integer weight) {
        if (methods.getOrDefault(method, 0) < weight) {
            methods.put(method, weight);
        }
    }

    public void addClass(PsiClass aClass) {
        addClass(aClass, DEFAULT_PROPERTY_WEIGHT);
    }

    public void addClass(PsiClass aClass, Integer weight) {
        if (classes.getOrDefault(aClass, 0) < weight) {
            classes.put(aClass, weight);
        }
    }

    public void addField(PsiField field) {
        addField(field, DEFAULT_PROPERTY_WEIGHT);
    }

    public void addField(PsiField field, Integer weight) {
        if (fields.getOrDefault(field, 0) < weight) {
            fields.put(field, weight);
        }
    }

    public void retainMethods(Collection<PsiMethod> methodsSet) {
        methods.keySet().retainAll(methodsSet);
    }

    public void retainClasses(Collection<PsiClass> classSet) {
        classes.keySet().retainAll(classSet);
    }

    public void addOverrideMethod(PsiMethod method) {
        overrideMethods.add(new Property<>(method, DEFAULT_PROPERTY_WEIGHT));
    }

    public int numberOfMethods() {
        return methods.size();
    }

    public Set<PsiField> getAllFields() {
        return new HashSet<>(fields.keySet());
    }

    public Set<PsiMethod> getAllMethods() {
        return new HashSet<>(methods.keySet());
    }

    public Set<PsiClass> getAllClasses() {
        return new HashSet<>(classes.keySet());
    }

    public int size() {
//        return classes.size() + fields.size() + methods.size();
        return getWeightedSize(classes) + getWeightedSize(fields) + getWeightedSize(methods);
    }

    private int getWeightedSize(Map<?, Integer> m) {
        return m.values().stream().mapToInt(Integer::valueOf).sum();
    }

    public int sizeOfIntersect(RelevantProperties properties) {
        int result = 0;
        result += sizeOfIntersectWeighted(classes, properties.classes);
        result += sizeOfIntersectWeighted(methods, properties.methods);
        result += sizeOfIntersectWeighted(fields, properties.fields);
//        result +=
//        final Collection<PsiClass> commonClasses = new HashSet<>(classes);
//        commonClasses.retainAll(properties.classes);
//        result += commonClasses.size();
//
//        final Collection<PsiMethod> commonMethods = new HashSet<>(methods);
//        commonMethods.addAll(overrideMethods);
//        final Set<PsiMethod> rpMethods = properties.methods;
//        rpMethods.addAll(properties.overrideMethods);
//        commonMethods.retainAll(rpMethods);
//        result += commonMethods.size();
//
//        final Collection<PsiField> commonFields = new HashSet<>(fields);
//        commonFields.retainAll(properties.fields);
//        result += commonFields.size();

        return result;
    }

    private static int sizeOfIntersectWeighted(Map<?, Integer> m1, Map<?, Integer> m2) {
        return m1.entrySet().stream()
                .filter(e -> m2.containsKey(e.getKey()))
                .mapToInt(e -> Math.min(e.getValue(), m2.get(e.getKey())))
                .sum();
    }

    public void printAll() {
        System.out.print("    ");
        for (PsiClass aClass : classes.keySet()) {
            System.out.print(aClass.getQualifiedName() + " ");
        }
        System.out.println();

        System.out.print("    ");
        for (PsiMethod method : methods.keySet()) {
            System.out.print(MethodUtils.calculateSignature(method) + ' ');
        }
        System.out.println();

        System.out.print("    ");
        for (PsiField field : fields.keySet()) {
            System.out.print(field.getContainingClass().getQualifiedName() + "." + field.getName() + " ");
        }
        System.out.println();
    }

    private class Property<T extends PsiElement> {
        private final T member;
        private final int weight;
        private final double eps = 1e-7;

        private Property(@NotNull T member, int weight) {
            this.member = member;
            this.weight = weight;
        }

        public T getMember() {
            return member;
        }

        public int getWeight() {
            return weight;
        }

        @Override
        public int hashCode() {
            return getMember().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (super.equals(obj)) {
                return true;
            }
            if (obj instanceof Property<?>) {
                Property<?> prop = (Property<?>) obj;
                return getMember().equals(prop.getMember());
            } else if (obj instanceof PsiElement) {
                return obj.equals(getMember());
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("{%s; %d}", member, weight);
        }
    }
}