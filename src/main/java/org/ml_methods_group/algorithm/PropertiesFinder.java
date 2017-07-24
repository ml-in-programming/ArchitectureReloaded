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
import com.sixrr.metrics.utils.MethodUtils;
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
        System.out.println("Size = " + properties.size());
        System.out.println("Size2 = " + elementByName.size());
    }

    public RelevantProperties getProperties(PsiElement element) {
        return properties.get(element);
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
        PsiElement element = elementByName.get(name);
        return elementByName.get(name);
    }

    private class UnitsFinder extends JavaRecursiveElementVisitor {

        @Override
        public void visitFile(PsiFile file) {
            if (isSourceFile(file)) {
                System.out.println("!#! " + file.getName());
                super.visitFile(file);
            }
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
            properties.put(aClass, new RelevantProperties());
            super.visitClass(aClass);
        }

        @Override
        public void visitField(PsiField field) {
            allFields.add(field);
            elementByName.put(getHumanReadableName(field), field);
            properties.put(field, new RelevantProperties());
            super.visitField(field);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            elementByName.put(getHumanReadableName(method), method);
            properties.put(method, new RelevantProperties());
            super.visitMethod(method);
        }
    }

    private class PropertiesCalculator extends JavaRecursiveElementVisitor {
        private PsiMethod currentMethod;

        @Override
        public void visitFile(PsiFile file) {
            if (isSourceFile(file)) {
                super.visitFile(file);
            }
        }

        @Override
        public void visitClass(PsiClass aClass) {
            final RelevantProperties classProperties = properties.get(aClass);
            if (classProperties == null) {
                super.visitClass(aClass);
                return;
            }
            classProperties.addClass(aClass);
            for (PsiClass superClass : PSIUtil.getAllSupers(aClass, allClasses)) {
                if (superClass.isInterface()) {
                    classProperties.addClass(superClass);
                } else {
                    propertiesFor(superClass).ifPresent(p -> p.addClass(aClass));
                }
            }
            Arrays.stream(aClass.getAllMethods())
                    .filter(m -> isProperty(aClass, m))
                    .forEach(classProperties::addMethod);
            Arrays.stream(aClass.getAllFields())
                    .filter(m -> isProperty(aClass, m))
                    .forEach(classProperties::addField);
            super.visitClass(aClass);
        }

        private boolean isProperty(PsiClass aClass, PsiMember member) {
            return aClass.equals(member.getContainingClass()) || !MethodUtils.isPrivate(member);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            final RelevantProperties methodProperties = properties.get(method);
            if (methodProperties == null) {
                super.visitMethod(method);
                return;
            }
            methodProperties.addMethod(method);
            Optional.ofNullable(method.getContainingClass())
                    .ifPresent(methodProperties::addClass);
            if (currentMethod == null) {
                currentMethod = method;
            }
            PSIUtil.getAllSupers(method, allClasses).stream()
                    .filter(properties::containsKey)
                    .map(properties::get)
                    .forEach(properties -> properties.addOverrideMethod(method));
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
                propertiesFor(currentMethod)
                        .ifPresent(p -> p.addField((PsiField) element));
                propertiesFor(element)
                        .ifPresent(p -> p.addMethod(currentMethod));
            }
            super.visitReferenceExpression(expression);
        }

        @Override
        public void visitField(PsiField field) {
            RelevantProperties fieldProperties = properties.get(field);
            if (fieldProperties == null) {
                super.visitField(field);
                return;
            }
            fieldProperties.addField(field);
            final PsiClass containingClass = field.getContainingClass();
            if (containingClass != null) {
                fieldProperties.addClass(containingClass);
            }
            super.visitField(field);
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            PsiElement element = expression.getMethodExpression().resolve();
            if (currentMethod != null && element instanceof PsiMethod) {
                propertiesFor(currentMethod)
                        .ifPresent(p -> p.addMethod((PsiMethod) element));
            }
            super.visitMethodCallExpression(expression);
        }
    }

    private Optional<RelevantProperties> propertiesFor(PsiElement element) {
        return Optional.ofNullable(properties.get(element));
    }

    private boolean isSourceFile(PsiFile file) {
        return file.getName().endsWith(".java");
    }
}
