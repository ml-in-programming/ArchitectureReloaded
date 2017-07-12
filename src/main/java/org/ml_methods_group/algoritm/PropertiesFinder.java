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

package org.ml_methods_group.algoritm;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.AnonymousClassElement;
import com.sixrr.metrics.utils.MethodUtils;

import java.util.*;
import java.util.stream.Collectors;

public class PropertiesFinder {
    private final Map<String, RelevantProperties> properties = new HashMap<>();
    private final Map<String, HashSet<PsiMethod>> methods = new HashMap<>();
    private final Map<String, HashSet<PsiField>> fields = new HashMap<>();
    private final Map<String, HashSet<PsiClass>> parents = new HashMap<>();
    private final Map<String, HashSet<PsiMethod>> superMethods = new HashMap<>();
    private final Map<String, PsiClass> classByName = new HashMap<>();
    private final Map<String, PsiMethod> methodByName = new HashMap<>();
    private final Map<String, PsiField> fieldByName = new HashMap<>();
    private final Stack<PsiMethod> methodStack = new Stack<>();
    private final Set<PsiClass> allClasses = new HashSet<>();

    public PsiElementVisitor createVisitor(final AnalysisScope analysisScope) {
        final PsiElementVisitor visitor = new FileClassesCounter();
        ProgressManager.getInstance().runProcess(() -> analysisScope.accept(visitor), new EmptyProgressIndicator());
        return new FileVisitor();
    }

    public boolean hasElement(String name) {
        return properties.containsKey(name);
    }

    public RelevantProperties getProperties(String name) {
        return properties.get(name);
    }

    public Set<String> getAllFields() {
        final Set<String> fields = new HashSet<>();
        for (String entity : properties.keySet()) {
            final RelevantProperties rp = properties.get(entity);
            final Set<PsiField> fs = rp.getAllFields();
            for (PsiField field : fs) {
                final PsiClass containingClass = field.getContainingClass();
                assert containingClass != null;
                if (!properties.containsKey(containingClass.getQualifiedName())) {
                    continue;
                }
                fields.add(field.getContainingClass().getQualifiedName() + "." + field.getName());
            }
        }

        return fields;
    }

    public Set<PsiClass> getAllClasses() {
        return allClasses;
    }

    public Set<String> getAllClassesNames() {
        return allClasses.stream().map(PsiClass::getQualifiedName).collect(Collectors.toSet());
    }

    public PsiElement getPsiElement(String name) {
        if (methodByName.containsKey(name)) {
            return methodByName.get(name);
        }
        if (classByName.containsKey(name)) {
            return classByName.get(name);
        }
        if (fieldByName.containsKey(name)) {
            return fieldByName.get(name);
        }

        return null;
    }

    private class ClassCounter extends JavaRecursiveElementVisitor {
        @Override
        public void visitClass(PsiClass aClass) {
            if (aClass.isEnum()) {
                return;
            }
            if (aClass instanceof AnonymousClassElement) {
                return;
            }

            if (aClass.getQualifiedName() == null) {
                return;
            }
            allClasses.add(aClass);
            super.visitClass(aClass);
        }
    }

    private class FileClassesCounter extends JavaElementVisitor {
        @Override
        public void visitFile(final PsiFile file) {
            System.out.println("!#! " + file.getName());

            final PsiElementVisitor counter = new ClassCounter();
            ProgressManager.getInstance().runProcess(() -> file.accept(counter), new EmptyProgressIndicator());
        }
    }

    private class FileVisitor extends JavaElementVisitor {
        @Override
        public void visitFile(final PsiFile file) {
            System.out.println("!#! " + file.getName());

            final PsiElementVisitor counter = new ClassCounter();
            ProgressManager.getInstance().runProcess(() -> file.accept(counter), new EmptyProgressIndicator());

            final PsiElementVisitor visitor = new EntityVisitor();
            ProgressManager.getInstance().runProcess(() -> file.accept(visitor), new EmptyProgressIndicator());

            processSuperClasses();
            processSuperMethods();

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

            for (String name : properties.keySet()) {
                properties.get(name).retainClasses(classByName.values());
                properties.get(name).retainMethods(methodByName.values());
            }

        }

        private void processSuperMethods() {
            for (String methodName : superMethods.keySet()) {
                final PsiMethod method = methodByName.get(methodName);
                for (PsiMethod parent : superMethods.get(methodName)) {
                    final String parentName = MethodUtils.calculateSignature(parent);
                    if (!properties.containsKey(parentName)) {
                        continue;
                    }
                    properties.get(parentName).addOverrideMethod(method);
                }
            }
        }

        private void processSuperClasses() {
            for (String className : parents.keySet()) {
                final PsiClass psiClass = classByName.get(className);
                for (PsiClass parent : parents.get(className)) {
                    final String parentName = parent.getQualifiedName();
                    if (!properties.containsKey(parentName)) {
                        continue;
                    }
                    properties.get(parentName).addClass(psiClass);
                }
            }
        }
    }

    private class EntityVisitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitClass(PsiClass psiClass) {
            if (psiClass.isEnum()) {
                return;
            }

            if (psiClass instanceof AnonymousClassElement) {
                return;
            }

            if (psiClass.getQualifiedName() == null) {
                return;
            }

            final RelevantProperties props = new RelevantProperties();
            final String fullName = psiClass.getQualifiedName();
            classByName.put(fullName, psiClass);
            props.addClass(psiClass);

            super.visitClass(psiClass);

            Arrays.stream(psiClass.getAllFields()).forEach(props::addField);
            Arrays.stream(psiClass.getAllMethods()).forEach(props::addMethod);

            final Set<PsiClass> supers = PSIUtil.getAllSupers(psiClass);
            for (PsiClass sup : supers) {
                if (sup.isInterface()) {
                    props.addClass(sup);
                } else {
                    if (!parents.containsKey(fullName)) {
                        parents.put(fullName, new HashSet<>());
                    }

                    parents.get(fullName).add(sup);
                }
            }

            PropertiesFinder.this.properties.put(fullName, props);
        }

        @Override
        public void visitReferenceExpression(PsiReferenceExpression expression) {
            final PsiElement elem = expression.resolve();
            if (!(elem instanceof PsiField)) {
                return;
            }

            if (methodStack.empty()) {
                return;
            }

            final PsiMethod method = methodStack.peek();
            final PsiField field = (PsiField) elem;
            final String fullMethodName = MethodUtils.calculateSignature(method);
            final PsiClass containingClass = field.getContainingClass();
            assert containingClass != null;

            final String fullFieldName = containingClass.getQualifiedName() + "." + field.getName();
            if (!fields.containsKey(fullMethodName)) {
                fields.put(fullMethodName, new HashSet<>());
            }
            fields.get(fullMethodName).add(field);

            if (!methods.containsKey(fullFieldName)) {
                methods.put(fullFieldName, new HashSet<>());
            }
            methods.get(fullFieldName).add(method);
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            if (methodStack.empty()) {
                return;
            }

            final PsiMethod element = expression.resolveMethod();
            final PsiMethod caller = methodStack.peek();
            final String callerName = MethodUtils.calculateSignature(caller);
            if (!methods.containsKey(callerName)) {
                methods.put(callerName, new HashSet<>());
            }
            methods.get(callerName).add(element);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            final PsiClass containingClass = method.getContainingClass();
            if (containingClass == null || containingClass.isInterface()) {
                return;
            }

            final String methodName = MethodUtils.calculateSignature(method);
            methodByName.put(methodName, method);

            final RelevantProperties props = new RelevantProperties();
            props.addMethod(method);
            props.addClass(containingClass);

            properties.put(methodName, props);
            methodStack.push(method);
            super.visitMethod(method);
            methodStack.pop();

            superMethods.put(methodName, new HashSet<>(PSIUtil.getAllSupers(method, allClasses)));
        }

        @Override
        public void visitField(PsiField field) {
            if (field.getContainingClass() == null) {
                return;
            }

            final String name = field.getContainingClass().getQualifiedName() + "." + field.getName();

            if (!properties.containsKey(name)) {
                properties.put(name, new RelevantProperties());
            }
            properties.get(name).addClass(field.getContainingClass());
            properties.get(name).addField(field);
            fieldByName.put(name, field);
        }
    }
}
