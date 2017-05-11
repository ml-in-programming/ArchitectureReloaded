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
import com.intellij.psi.impl.source.tree.java.AnonymousClassElement;
import com.sixrr.metrics.utils.MethodUtils;

import java.util.*;

/**
 * Created by Kivi on 10.04.2017.
 */
public class PropertiesFinder {

    public PsiElementVisitor createVisitor() {return new FileVisitor();}

    public RelevantProperties getProperties(String name) {

        return properties.get(name);
    }

    public Set<String> getAllFields() {
        Set<String> fields = new HashSet<String>();
        for (String entity : properties.keySet()) {
            RelevantProperties rp = properties.get(entity);
            Set<PsiField> fs = rp.getAllFields();
            for (PsiField field : fs) {
                if (!properties.containsKey(field.getContainingClass().getQualifiedName())) {
                    continue;
                }
                String name = field.getContainingClass().getQualifiedName() + "." + field.getName();
                fields.add(name);
            }
        }

        return fields;
    }

    private HashMap<String, RelevantProperties> properties = new HashMap<String, RelevantProperties>();

    private HashMap<String, HashSet<PsiMethod>> methods = new HashMap<String, HashSet<PsiMethod>>();
    private HashMap<String, HashSet<PsiField>> fields = new HashMap<String, HashSet<PsiField>>();

    protected Map<String, HashSet<PsiClass>> parents = new HashMap<String, HashSet<PsiClass>>();

    protected Map<String, HashSet<PsiMethod>> superMethods = new HashMap<String, HashSet<PsiMethod>>();

    protected Map<String, PsiClass> classByName = new HashMap<String, PsiClass>();

    protected Map<String, PsiMethod> methodByName = new HashMap<String, PsiMethod>();

    protected Map<String, PsiField> fieldByName = new HashMap<String, PsiField>();

    private Stack<PsiMethod> methodStack = new Stack<PsiMethod>();

    private class FileVisitor extends JavaElementVisitor {

        @Override
        public void visitFile(final PsiFile file) {
            System.out.println("!#! " + file.getName());

            final PsiElementVisitor visitor = new EntityVisitor();
            ProgressManager.getInstance().runProcess(new Runnable() {
                @Override
                public void run() {
                    file.accept(visitor);
                }
            }, new EmptyProgressIndicator());

            for (String className : parents.keySet()) {
                PsiClass aClass = classByName.get(className);
                for (PsiClass parent : parents.get(className)) {
                    String parentName = parent.getQualifiedName();
                    if (!properties.containsKey(parentName)) {
                        continue;
                    }
                    properties.get(parentName).addClass(aClass);
                }
            }


            for (String methodName : superMethods.keySet()) {
                PsiMethod method = methodByName.get(methodName);
                for (PsiMethod parent : superMethods.get(methodName)) {
                    String parentName = MethodUtils.calculateSignature(parent);
                    if (!properties.containsKey(parentName)) {
                        continue;
                    }
                    properties.get(parentName).addOverrideMethod(method);
                }
            }

            for (String name : methods.keySet()) {
                if (!methodByName.containsKey(name) && !fieldByName.containsKey(name)
                        && !classByName.containsKey(name)) {
                    continue;
                }
                for (PsiMethod method : methods.get(name)) {
                    if (methodByName.containsValue(method)) {
                        properties.get(name).addMethod(method);
                    }
                }
            }

            for (String name : fields.keySet()) {
                for (PsiField field : fields.get(name)) {
                    if (fieldByName.containsValue(field)) {
                        properties.get(name).addField(field);
                    }
                }
            }
        }
    }

    private class EntityVisitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitClass(PsiClass aClass) {
            if (aClass instanceof AnonymousClassElement) {
                return;
            }

            RelevantProperties rp = new RelevantProperties();
            String fullName = aClass.getQualifiedName();

            classByName.put(fullName, aClass);
            rp.addClass(aClass);
            //System.out.println("    !@! " + aClass.getName() + " : " + aClass.getQualifiedName());
            super.visitClass(aClass);
            PsiField[] fields = aClass.getAllFields();
            for (PsiField field : fields) {
                //System.out.println("        " + field.getName());
                rp.addField(field);
            }
            //System.out.println();
            PsiMethod[] methods = aClass.getAllMethods();
            for (PsiMethod method : methods) {
                rp.addMethod(method);
                //System.out.println("        " + method.getContainingClass().getName() + "." + method.getName());
            }
            //System.out.println();

            PsiClass[] supers = aClass.getSupers();
            for (PsiClass sup : supers) {
                if (sup.isInterface()) {
                    rp.addClass(sup);
                } else {
                    if (!parents.containsKey(fullName)) {
                        parents.put(fullName, new HashSet<PsiClass>());
                    }

                    parents.get(fullName).add(sup);
                }
            }

            properties.put(fullName, rp);

            //System.out.println();
        }

        @Override
        public void visitReferenceExpression(PsiReferenceExpression expression) {
            //System.out.println("            !*! " + expression.getText());
            PsiElement elem = expression.resolve();
            //System.out.println("                " + elem.getClass().getName());
            if (elem instanceof PsiField) {
                PsiField field = (PsiField) elem;
                if (methodStack.empty()) {
                    return;
                }
                PsiMethod method = methodStack.peek();
                String fullMethodName = MethodUtils.calculateSignature(method);
                String fullFieldName = field.getContainingClass().getQualifiedName() + "." + field.getName();
                //properties.get(fullMethodName).addField(field);
                if (!fields.containsKey(fullMethodName)) {
                    fields.put(fullMethodName, new HashSet<PsiField>());
                }
                fields.get(fullMethodName).add(field);

                if (!methods.containsKey(fullFieldName)) {
                    methods.put(fullFieldName, new HashSet<PsiMethod>());
                }

                methods.get(fullFieldName).add(method);
            }

            //System.out.println();
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            PsiMethod element = expression.resolveMethod();
            if (methodStack.empty()) {
                return;
            }
            PsiMethod caller = methodStack.peek();
            String callerName = MethodUtils.calculateSignature(caller);
            if (!methods.containsKey(callerName)) {
                methods.put(callerName, new HashSet<PsiMethod>());
            }
            methods.get(callerName).add(element);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            if (method.getContainingClass().equals(null)) {
                return;
            }
            String methodName = MethodUtils.calculateSignature(method);
            methodByName.put(methodName, method);
            //System.out.println("        !%! " + methodName);

            RelevantProperties rp = new RelevantProperties();
            rp.addMethod(method);
            rp.addClass(method.getContainingClass());
            properties.put(methodName, rp);
            methodStack.push(method);
            super.visitMethod(method);
            methodStack.pop();

            PsiMethod[] methods = method.findSuperMethods();
            superMethods.put(methodName, new HashSet<PsiMethod>());
            for (PsiMethod met : methods) {
                superMethods.get(methodName).add(met);
                //System.out.println("        " + met.getContainingClass().getQualifiedName() + "." + met.getName());
            }

            //System.out.println();
        }

        @Override
        public void visitField(PsiField field) {
            if (field.getContainingClass().equals(null)) {
                return;
            }
            String name = field.getContainingClass().getQualifiedName() + "." + field.getName();
            //System.out.println("        !$! " + name);

            if (!properties.containsKey(name)) {
                RelevantProperties rp = new RelevantProperties();
                properties.put(name, rp);
            }
            properties.get(name).addClass(field.getContainingClass());
            properties.get(name).addField(field);
            fieldByName.put(name, field);
        }
    }
}
