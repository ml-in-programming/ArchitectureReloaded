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
import com.sixrr.metrics.utils.MethodUtils;

import java.util.*;

import static org.ml_methods_group.utils.PsiSearchUtil.getHumanReadableName;

public class RelevantProperties {

    private static final Comparator<String> FAST_COMPARATOR = Comparator.comparingInt(String::length)
            .thenComparingInt(String::hashCode)
            .thenComparing(String::compareTo);

    private final ArrayList<String> methods = new ArrayList<>();
    private final ArrayList<String> classes = new ArrayList<>();
    private final ArrayList<String> fields = new ArrayList<>();
    private final ArrayList<String> privateMembers = new ArrayList<>();
    private final ArrayList<String> allMethods = new ArrayList<>();

    @Deprecated
    public void removeMethod(String method) {
        methods.remove(method);
    }

    @Deprecated
    public void removeField(String field) {
        fields.remove(field);
    }

    void addMethod(PsiMethod method) {
        final String name = getHumanReadableName(method);
        methods.add(name);
        allMethods.add(name);
        if (MethodUtils.isPrivate(method)) {
            privateMembers.add(name);
        }
    }

    void addClass(PsiClass aClass) {
        classes.add(getHumanReadableName(aClass));
    }

    void addField(PsiField field) {
        final String name = getHumanReadableName(field);
        fields.add(name);
        if (MethodUtils.isPrivate(field)) {
            privateMembers.add(name);
        }
    }

    void addOverrideMethod(PsiMethod method) {
        allMethods.add(getHumanReadableName(method));
    }

    void prepare() {
        prepareList(privateMembers);
        prepareList(classes);
        prepareList(allMethods);
        prepareList(fields);
    }

    private void prepareList(ArrayList<String> list) {
        removeCopies(list);
        list.sort(FAST_COMPARATOR);
        list.trimToSize();
    }

    int numberOfMethods() {
        return methods.size();
    }

    public List<String> getAllFields() {
        return Collections.unmodifiableList(fields);
    }

    public List<String> getAllMethods() {
        return Collections.unmodifiableList(methods);
    }

    public int size() {
        return classes.size() + fields.size() + allMethods.size();
    }

    public int sizeOfIntersect(RelevantProperties properties) {
        int result = 0;
        result += sizeOfIntersection(classes, properties.classes);
        result += sizeOfIntersection(allMethods, properties.allMethods);
        result += sizeOfIntersection(fields, properties.fields);
        return result;
    }

    public boolean hasCommonPrivateMember(RelevantProperties properties) {
        return !isIntersectionEmpty(privateMembers, properties.privateMembers);
    }

    private void removeCopies(Collection<String> collection) {
        final Set<String> distinctValues = new HashSet<>(collection);
        collection.clear();
        collection.addAll(distinctValues);
    }

    private int sizeOfIntersection(List<String> first, List<String> second) {
        int intersection = 0;
        int firstIndex = 0;
        int secondIndex = 0;
        while (firstIndex < first.size() && secondIndex < second.size()) {
            int cmp = FAST_COMPARATOR.compare(first.get(firstIndex), second.get(secondIndex));
            if (cmp == 0) {
                intersection++;
                firstIndex++;
                secondIndex++;
            } else if (cmp < 0) {
                firstIndex++;
            } else {
                secondIndex++;
            }
        }
        return intersection;
    }

    private boolean isIntersectionEmpty(List<String> first, List<String> second) {
        int firstIndex = 0;
        int secondIndex = 0;
        while (firstIndex < first.size() && secondIndex < second.size()) {
            int cmp = FAST_COMPARATOR.compare(first.get(firstIndex), second.get(secondIndex));
            if (cmp == 0) {
                assert first.get(firstIndex).equals(second.get(secondIndex));
                return false;
            } else if (cmp < 0) {
                firstIndex++;
            } else {
                secondIndex++;
            }
        }
        return true;
    }

    @Deprecated
    public void moveTo(String targetClass) {
        classes.clear();
        classes.add(targetClass);
    }

    void printAll() {
        System.out.print("    ");
        for (String aClass : classes) {
            System.out.print(aClass + " ");
        }
        System.out.println();

        System.out.print("    ");
        for (String method : allMethods) {
            System.out.print(method + ' ');
        }
        System.out.println();

        System.out.print("    ");
        for (String field : fields) {
            System.out.print(field + " ");
        }
        System.out.println();
    }
}