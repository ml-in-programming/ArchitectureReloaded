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
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.sixrr.metrics.utils.MethodUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Kivi on 11.04.2017.
 */
public class RelevantProperties {
    public RelevantProperties() {
        methods = new HashSet<PsiMethod>();
        classes = new HashSet<PsiClass>();
        fields = new HashSet<PsiField>();
        overrideMethods = new HashSet<PsiMethod>();
    };

    public void addMethod(PsiMethod method) {
        methods.add(method);
    }

    public void addClass(PsiClass aClass) {
        classes.add(aClass);
    }

    public void addField(PsiField field) {
        fields.add(field);
    }

    public void retainMethods(Collection<PsiMethod> methodsSet) {
        methods.retainAll(methodsSet);
    }

    public void retainClasses(Collection<PsiClass> classSet) {
        classes.retainAll(classSet);
    }

    public void addOverrideMethod(PsiMethod method) {
        overrideMethods.add(method);
    }

    public Integer numberOfMethods() {
        return methods.size();
    }

    public Set<PsiField> getAllFields() {
        return new HashSet<PsiField>(fields);
    }

    public Set<PsiMethod> getAllMethods() {
        return new HashSet<PsiMethod>(methods);
    }

    public int size() {
        return classes.size() + fields.size() + methods.size();
    }

    public int sizeOfIntersect(RelevantProperties rp) {
        int ans = 0;
        Set<PsiClass> c = new HashSet<PsiClass>(classes);
        c.retainAll(rp.classes);
        ans += c.size();

        Set<PsiMethod> m = new HashSet<PsiMethod>(methods);
        m.addAll(overrideMethods);
        Set<PsiMethod> rpm = rp.methods;
        rpm.addAll(rp.overrideMethods);
        m.retainAll(rpm);
        ans += m.size();

        Set<PsiField> f = new HashSet<PsiField>(fields);
        f.retainAll(rp.fields);
        ans += f.size();

        return ans;
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
        for (PsiField field : fields) {
            System.out.print(field.getContainingClass().getQualifiedName() + "." + field.getName() + " ");
        }
        System.out.println();
    }

    private HashSet<PsiMethod> methods;
    private HashSet<PsiClass> classes;
    private HashSet<PsiField> fields;
    private HashSet<PsiMethod> overrideMethods;
}
