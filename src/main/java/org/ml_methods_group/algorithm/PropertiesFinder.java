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

package org.ml_methods_group.algorithm;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.AnonymousClassElement;
import org.ml_methods_group.utils.PsiSearchUtil;

import java.util.*;
import java.util.stream.Collectors;

import static org.ml_methods_group.utils.PsiSearchUtil.getHumanReadableName;

public class PropertiesFinder {
    private final Map<PsiElement, RelevantProperties> properties = new HashMap<>();
    private final Set<PsiClass> allClasses = new HashSet<>();
    private final Set<PsiField> allFields = new HashSet<>();
    private final Map<String, PsiElement> elementByName = new HashMap<>();
    private final AnalysisScope scope;

    private PropertiesFinder(AnalysisScope scope) {
        this.scope = scope;
    }

    public static PropertiesFinder analyze(AnalysisScope scope) {
        final PropertiesFinder finder = new PropertiesFinder(scope);
        finder.runCalculations();
        return finder;
    }

    private void runCalculations() {
        scope.accept(new UnitsFinder());
        scope.accept(new PropertiesCalculator());
        for (Map.Entry<PsiElement, RelevantProperties> entry : properties.entrySet()) {
            System.out.println("Properties for " + getHumanReadableName(entry.getKey()));
            entry.getValue().printAll();
        }
    }

    public RelevantProperties getProperties(PsiElement element) {
        return getOrCreateProperties(element);
    }

    public Set<String> getAllFields() {
        return allFields.stream()
                .map(PsiSearchUtil::getHumanReadableName)
                .collect(Collectors.toSet());
    }

    public Set<PsiClass> getAllClasses() {
        return allClasses;
    }

    public PsiElement elementForName(String name) {
        return elementByName.get(name);
    }

    private class UnitsFinder extends JavaRecursiveElementVisitor {

        @Override
        public void visitFile(PsiFile file) {
            System.out.println("!#! " + file.getName());
            super.visitFile(file);
        }

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
            elementByName.put(getHumanReadableName(aClass), aClass);
            super.visitClass(aClass);
        }

        @Override
        public void visitField(PsiField field) {
            allFields.add(field);
            elementByName.put(getHumanReadableName(field), field);
            super.visitField(field);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            elementByName.put(getHumanReadableName(method), method);
            super.visitMethod(method);
        }
    }

    private class PropertiesCalculator extends JavaRecursiveElementVisitor {
        private PsiMethod currentMethod;

        @Override
        public void visitClass(PsiClass aClass) {
            final RelevantProperties classProperties = getOrCreateProperties(aClass);
            classProperties.addClass(aClass);
            for (PsiClass superClass : PSIUtil.getAllSupers(aClass, allClasses)) {
                if (superClass.isInterface()) {
                    classProperties.addClass(superClass);
                } else {
                    getOrCreateProperties(superClass).addClass(aClass);
                }
            }
            Arrays.stream(aClass.getAllMethods()).forEach(classProperties::addMethod);
            Arrays.stream(aClass.getAllFields()).forEach(classProperties::addField);
            super.visitClass(aClass);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            final RelevantProperties methodProperties = getOrCreateProperties(method);
            methodProperties.addMethod(method);
            Optional.ofNullable(method.getContainingClass())
                    .ifPresent(methodProperties::addClass);
            if (currentMethod == null) {
                currentMethod = method;
            }
            PSIUtil.getAllSupers(method, allClasses)
                    .forEach(m -> getOrCreateProperties(m).addMethod(method));
            Arrays.stream(method.getParameterList().getParameters())
                    .map(PsiParameter::getType)
                    .map(PsiType::getCanonicalText)
                    .map(PropertiesFinder.this::elementForName)
                    .filter(Objects::nonNull)
                    .map(PsiClass.class::cast)
                    .forEach(methodProperties::addClass);
            super.visitMethod(method);
            if (currentMethod == method) {
                currentMethod = null;
            }
        }

        @Override
        public void visitReferenceExpression(PsiReferenceExpression expression) {
            PsiElement element = expression.resolve();
            if (currentMethod != null && element instanceof PsiField) {
                getOrCreateProperties(currentMethod).addField((PsiField) element);
                getOrCreateProperties(element).addMethod(currentMethod);
            }
            super.visitReferenceExpression(expression);
        }

        @Override
        public void visitField(PsiField field) {
            RelevantProperties properties = getOrCreateProperties(field);
            properties.addField(field);
            final PsiClass containingClass = field.getContainingClass();
            if (containingClass != null) {
                properties.addClass(containingClass);
            }
            super.visitField(field);
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            PsiElement element = expression.getMethodExpression().resolve();
            if (currentMethod != null && element instanceof PsiMethod) {
                getOrCreateProperties(currentMethod).addMethod((PsiMethod) element);
            }
            super.visitMethodCallExpression(expression);
        }
    }

    private RelevantProperties getOrCreateProperties(PsiElement element) {
        return properties.computeIfAbsent(element, x -> new RelevantProperties());
    }
}
