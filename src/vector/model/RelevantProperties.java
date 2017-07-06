/*
 *  Copyright 2017 Machine Learning Methods in Software Engineering Research Group
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
import com.sixrr.metrics.utils.MethodUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RelevantProperties {
    private final Set<PsiMethod> methods = new HashSet<>();
    private final Set<PsiClass> classes = new HashSet<>();
    private final Set<PsiField> fields = new HashSet<>();
    private final Collection<PsiMethod> overrideMethods = new HashSet<>();

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

    public int numberOfMethods() {
        return methods.size();
    }

    public Set<PsiField> getAllFields() {
        return new HashSet<PsiField>(fields);
    }

    public Set<PsiMethod> getAllMethods() {
        return new HashSet<PsiMethod>(methods);
    }

    public Set<PsiClass> getAllClasses() {
        return new HashSet<PsiClass>(classes);
    }

    public int size() {
        return classes.size() + fields.size() + methods.size();
    }

    public int sizeOfIntersect(RelevantProperties properties) {
        int result = 0;
        final Collection<PsiClass> commonClasses = new HashSet<>(classes);
        commonClasses.retainAll(properties.classes);
        result += commonClasses.size();

        final Collection<PsiMethod> commonMethods = new HashSet<>(methods);
        commonMethods.addAll(overrideMethods);
        final Set<PsiMethod> rpMethods = properties.methods;
        rpMethods.addAll(properties.overrideMethods);
        commonMethods.retainAll(rpMethods);
        result += commonMethods.size();

        final Collection<PsiField> commonFields = new HashSet<>(fields);
        commonFields.retainAll(properties.fields);
        result += commonFields.size();

        return result;
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
}