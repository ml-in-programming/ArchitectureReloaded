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
import org.ml_methods_group.utils.SetsUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.ml_methods_group.utils.PsiSearchUtil.getHumanReadableName;

public class RelevantProperties {

    private final HashSet<String> methods = new HashSet<>();
    private final HashSet<String> classes = new HashSet<>();
    private final HashSet<String> fields = new HashSet<>();
    private final HashSet<String> privateMembers = new HashSet<>();
    private final HashSet<String> allMethods = new HashSet<>();


    void removeMethod(String method) {
        methods.remove(method);
    }

    void addMethod(String method) {
        methods.add(method);
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

    int numberOfMethods() {
        return methods.size();
    }

    public Set<String> getAllFields() {
        return Collections.unmodifiableSet(fields);
    }

    public Set<String> getAllMethods() {
        return Collections.unmodifiableSet(methods);
    }

    public int size() {
        return classes.size() + fields.size() + allMethods.size();
    }

    int sizeOfIntersection(RelevantProperties properties) {
        int result = 0;
        result += SetsUtil.intersection(classes, properties.classes);
        result += SetsUtil.intersection(allMethods, properties.allMethods);
        result += SetsUtil.intersection(fields, properties.fields);
        return result;
    }

    boolean hasCommonPrivateMember(RelevantProperties properties) {
        return !SetsUtil.isIntersectionEmpty(privateMembers, properties.privateMembers);
    }

    public RelevantProperties copy() {
        final RelevantProperties copy = new RelevantProperties();
        copy.classes.addAll(classes);
        copy.allMethods.addAll(allMethods);
        copy.methods.addAll(methods);
        copy.fields.addAll(fields);
        copy.privateMembers.addAll(privateMembers);
        return copy;
    }
}