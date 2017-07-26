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
    private boolean modifiable = true;

    private void checkModifiable() {
        if (!modifiable) {
            throw new UnsupportedOperationException("Properties already prepared and can't be modified now");
        }
    }

    void prepare() {
        prepareList(privateMembers);
        prepareList(classes);
        prepareList(allMethods);
        prepareList(fields);
        modifiable = false;
    }

    private void prepareList(ArrayList<String> list) {
        ListUtil.removeCopies(list);
        list.sort(FAST_COMPARATOR);
        list.trimToSize();
    }

    public void removeMethod(String method) {
        checkModifiable();
        methods.remove(method);
    }

    @Deprecated
    public void removeField(String field) {
        checkModifiable();
        fields.remove(field);
    }

    void addMethod(PsiMethod method) {
        checkModifiable();
        final String name = getHumanReadableName(method);
        methods.add(name);
        allMethods.add(name);
        if (MethodUtils.isPrivate(method)) {
            privateMembers.add(name);
        }
    }

    void addClass(PsiClass aClass) {
        checkModifiable();
        classes.add(getHumanReadableName(aClass));
    }

    void addField(PsiField field) {
        checkModifiable();
        final String name = getHumanReadableName(field);
        fields.add(name);
        if (MethodUtils.isPrivate(field)) {
            privateMembers.add(name);
        }
    }

    void addOverrideMethod(PsiMethod method) {
        checkModifiable();
        allMethods.add(getHumanReadableName(method));
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

    public int sizeOfIntersection(RelevantProperties properties) {
        int result = 0;
        result += ListUtil.sizeOfIntersection(classes, properties.classes, FAST_COMPARATOR);
        result += ListUtil.sizeOfIntersection(allMethods, properties.allMethods, FAST_COMPARATOR);
        result += ListUtil.sizeOfIntersection(fields, properties.fields, FAST_COMPARATOR);
        return result;
    }

    public boolean hasCommonPrivateMember(RelevantProperties properties) {
        return !ListUtil.isIntersectionEmpty(privateMembers, properties.privateMembers, FAST_COMPARATOR);
    }

    public void moveTo(String targetClass) {
        checkModifiable();
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