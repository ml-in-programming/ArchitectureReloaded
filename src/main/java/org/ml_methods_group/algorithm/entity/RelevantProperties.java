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

import java.util.*;

public class RelevantProperties {
    private final Set<PsiMethod> methods = new HashSet<>();
    private final Set<PsiClass> classes = new HashSet<>();
    private final Set<PsiField> fields = new HashSet<>();
    private final Set<PsiMember> privateMembers = new HashSet<>();
    private final Set<PsiMethod> overrideMethods = new HashSet<>();

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
        methods.add(method);
        if (MethodUtils.isPrivate(method)) {
            privateMembers.add(method);
        }
    }

    public void addClass(PsiClass aClass) {
        classes.add(aClass);
    }

    public void addField(PsiField field) {
        fields.add(field);
        if (MethodUtils.isPrivate(field)) {
            privateMembers.add(field);
        }
    }

    public void addOverrideMethod(PsiMethod method) {
        overrideMethods.add(method);
    }

    public int numberOfMethods() {
        return methods.size();
    }

    public Set<PsiField> getAllFields() {
        return Collections.unmodifiableSet(fields);
    }

    public Set<PsiMethod> getAllMethods() {
        return Collections.unmodifiableSet(methods);
    }

    public Set<PsiClass> getAllClasses() {
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

    public void printAll() {
        System.out.print("    ");
        for (PsiClass aClass : classes) {
            System.out.print(aClass.getQualifiedName() + " ");
        }
        System.out.println();

        System.out.print("    ");
        for (PsiMethod method : methods) {
            System.out.print(MethodUtils.calculateSignature(method) + ' ');
        }
        System.out.println();

        System.out.print("    ");
        for (PsiMethod method : overrideMethods) {
            System.out.print(MethodUtils.calculateSignature(method) + ' ');
        }
        System.out.println();

        System.out.print("    ");
        for (PsiField field : fields) {
            System.out.print(field.getContainingClass().getQualifiedName() + "." + field.getName() + " ");
        }
        System.out.println();
    }
}