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
import org.ml_methods_group.utils.ListUtil;

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
    private boolean prepared = true;

    void prepare() {
        prepareList(privateMembers);
        prepareList(classes);
        prepareList(allMethods);
        prepareList(fields);
        prepared = true;
    }

    private void prepareList(ArrayList<String> list) {
        ListUtil.removeCopies(list);
        list.sort(FAST_COMPARATOR);
        list.trimToSize();
    }

    void removeMethod(String method) {
        methods.remove(method);
        prepared = false;
    }

    void addMethod(PsiMethod method) {
        final String name = getHumanReadableName(method);
        methods.add(name);
        allMethods.add(name);
        if (MethodUtils.isPrivate(method)) {
            privateMembers.add(name);
        }
        prepared = false;
    }

    void addClass(PsiClass aClass) {
        classes.add(getHumanReadableName(aClass));
        prepared = false;
    }

    void addField(PsiField field) {
        final String name = getHumanReadableName(field);
        fields.add(name);
        if (MethodUtils.isPrivate(field)) {
            privateMembers.add(name);
        }
        prepared = false;
    }

    void addOverrideMethod(PsiMethod method) {
        allMethods.add(getHumanReadableName(method));
        prepared = false;
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

    int sizeOfIntersection(RelevantProperties properties) {
        if (!prepared) {
            throw new RuntimeException("Properties wasn't prepared");
        }
        int result = 0;
        result += ListUtil.sizeOfIntersection(classes, properties.classes, FAST_COMPARATOR);
        result += ListUtil.sizeOfIntersection(allMethods, properties.allMethods, FAST_COMPARATOR);
        result += ListUtil.sizeOfIntersection(fields, properties.fields, FAST_COMPARATOR);
        return result;
    }

    boolean hasCommonPrivateMember(RelevantProperties properties) {
        if (!prepared) {
            throw new RuntimeException("Properties wasn't prepared");
        }
        return !ListUtil.isIntersectionEmpty(privateMembers, properties.privateMembers, FAST_COMPARATOR);
    }

    void moveTo(String targetClass) {
        classes.clear();
        classes.add(targetClass);
        prepared = false;
    }
}