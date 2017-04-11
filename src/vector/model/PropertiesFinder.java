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

/**
 * Created by Kivi on 11.04.2017.
 */
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

import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.*;


import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiReferenceListImpl;

import java.util.HashMap;

/**
 * Created by Kivi on 10.04.2017.
 */
public class PropertiesFinder {

    public PsiElementVisitor createVisitor() {return new FileVisitor();}

    public RelevantProperties getProperties(String name) {
        return properties.get(name);
    }

    private HashMap<String, RelevantProperties> properties = new HashMap<String, RelevantProperties>();

    private class FileVisitor extends JavaElementVisitor {

        @Override
        public void visitFile(final PsiFile file) {
            System.out.println("!#! " + file.getName());

            final PsiElementVisitor classVisitor = new EntityVisitor();
            ProgressManager.getInstance().runProcess(new Runnable() {
                @Override
                public void run() {
                    file.accept(classVisitor);
                }
            }, new EmptyProgressIndicator());
        }
    }

    private class EntityVisitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);
            System.out.println("    !@! " + aClass.getName() + " : " + aClass.getQualifiedName());

            PsiField[] fields = aClass.getAllFields();
            for (PsiField field : fields) {
                System.out.println("        " + field.getName());
            }
            System.out.println();
            PsiMethod[] methods = aClass.getAllMethods();
            for (PsiMethod method : methods) {

                System.out.println("        " + method.getContainingClass().getName() + "." + method.getName());
            }
            System.out.println();

            PsiClass[] supers = aClass.getSupers();
            for (PsiClass sup : supers) {
                if (sup.isInterface()) {
                    System.out.println("        interface@ " + sup.getQualifiedName());
                } else {
                    System.out.println("        superclass@ " + sup.getQualifiedName());
                }
            }

            System.out.println();
        }

        @Override
        public void visitMethod(PsiMethod method) {
            super.visitMethod(method);
            System.out.println("    !%! " + method.getContainingClass().getQualifiedName() + "." + method.getName());

            PsiReference[] refs = method.getReferences();
            for (PsiReference ref : refs) {
                System.out.println("        " + ref.getCanonicalText());
            }

            PsiMethod[] methods = method.findSuperMethods();
            for (PsiMethod met : methods) {
                System.out.println("        " + met.getContainingClass().getQualifiedName() + "." + met.getName());
            }

            System.out.println();
        }
    }
}
