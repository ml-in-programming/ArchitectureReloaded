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

import java.util.HashSet;

/**
 * Created by Kivi on 11.04.2017.
 */
public class RelevantProperties {
    public RelevantProperties() {
        methods = new HashSet<PsiMethod>();
        classes = new HashSet<PsiClass>();
        fields = new HashSet<PsiField>();
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
}
