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

import com.google.common.collect.Sets;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.MethodUtils;
import org.ml_methods_group.utils.PsiSearchUtil;

import java.util.*;

import static org.ml_methods_group.utils.PsiSearchUtil.getHumanReadableName;

public class RelevantProperties {
    private final Set<String> methods = new HashSet<>();
    private final Set<String> classes = new HashSet<>();
    private final Set<String> fields = new HashSet<>();
    private final Set<String> privateMembers = new HashSet<>();
    private final Set<String> overrideMethods = new HashSet<>();

    @Deprecated
    public void removeMethod(String method) {
        methods.remove(method);
    }

    @Deprecated
    public void removeField(String field) {
        fields.remove(field);
    }

    public void addMethod(PsiMethod method) {
        methods.add(getHumanReadableName(method));
        if (MethodUtils.isPrivate(method)) {
            privateMembers.add(getHumanReadableName(method));
        }
    }

    public void addClass(PsiClass aClass) {
        classes.add(getHumanReadableName(aClass));
    }

    public void addField(PsiField field) {
        fields.add(getHumanReadableName(field));
        if (MethodUtils.isPrivate(field)) {
            privateMembers.add(getHumanReadableName(field));
        }
    }

    public void addOverrideMethod(PsiMethod method) {
        overrideMethods.add(getHumanReadableName(method));
    }

    public int numberOfMethods() {
        return methods.size();
    }

    public Set<String> getAllFields() {
        return Collections.unmodifiableSet(fields);
    }

    public Set<String> getAllMethods() {
        return Collections.unmodifiableSet(methods);
    }

    public Set<String> getAllClasses() {
        return Collections.unmodifiableSet(classes);
    }

    public int size() {
        return classes.size() + fields.size() + methods.size() + overrideMethods.size();
    }

    public int sizeOfIntersect(RelevantProperties properties) {
        int result = 0;
        result += Sets.intersection(fields, properties.fields).size();
        result += Sets.intersection(classes, properties.classes).size();
        result += Sets.intersection(Sets.union(methods, overrideMethods),
                Sets.union(properties.methods, properties.overrideMethods)).size();
        return result;
    }

    public boolean hasCommonPrivateMember(RelevantProperties properties) {
        return !Sets.intersection(privateMembers, properties.privateMembers).isEmpty();
    }

    @Deprecated
    public void moveTo(String targetClass) {
        classes.clear();
        classes.add(targetClass);
    }

    public void printAll() {
        System.out.print("    ");
        for (String aClass : classes) {
            System.out.print(aClass + " ");
        }
        System.out.println();

        System.out.print("    ");
        for (String method : methods) {
            System.out.print(method + ' ');
        }
        System.out.println();

        System.out.print("    ");
        for (String method : overrideMethods) {
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