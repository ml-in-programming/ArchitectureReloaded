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

package com.sixrr.metrics.utils;

import com.intellij.analysis.AnalysisScope;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.impl.JavaFileTypeFactory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.makeStatic.MakeStaticHandler;
import com.intellij.refactoring.move.MoveHandler;
import com.intellij.ui.EditorTextField;
import org.jetbrains.kotlin.js.translate.utils.PsiUtils;
import org.jetbrains.plugins.groovy.lang.documentation.MethodUtil;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Created by Артём on 05.07.2017.
 */
public final class RefactoringUtil {
    private RefactoringUtil() {
    }

    public static void moveRefactoring(Map<String, String> refactorings, Project project, AnalysisScope scope) {
        System.out.println("refactorings:");
        for (Entry<String, String> e : refactorings.entrySet()) {
            System.out.println(e.getKey() + " to " + e.getValue());
        }

        final Map<String, List<String>> groupedMovements = refactorings.keySet().stream()
                .collect(Collectors.groupingBy(refactorings::get, Collectors.toList()));
        for (Entry<String, List<String>> refactoring : groupedMovements.entrySet()) {
            final List<PsiElement> members = refactoring.getValue().stream()
                    .map(name -> findElement(name, scope))
                    .map(element -> makeStatic(element, scope))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            moveMembersRefactoring(members, refactoring.getKey(), project, scope);
        }
    }

    private static void moveMembersRefactoring(Collection<PsiElement> elements, String targetClass, Project project,
                                               AnalysisScope scope) {
        final Map<PsiClass, List<PsiElement>> groupByCurrentClass = elements.stream()
                .collect(Collectors.groupingBy(RefactoringUtil::containingClass, Collectors.toList()));
        for (Entry<PsiClass, List<PsiElement>> movement : groupByCurrentClass.entrySet()) {
            final PsiElement destiny = findElement(targetClass, scope);
            System.out.println("move from " + movement.getKey().getQualifiedName() + " to " + destiny);
            final PsiElement[] array = movement.getValue().stream().toArray(PsiElement[]::new);
            MoveHandler.doMove(project, array, destiny, DataContext.EMPTY_CONTEXT, null);
        }
    }

    private static PsiClass containingClass(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, PsiClass.class);
    }

    public static PsiElement makeStatic(PsiElement element, AnalysisScope scope) {
        if (!(element instanceof PsiMethod)) {
            return element;
        }
        final PsiMethod method = (PsiMethod) element;
        if (MethodUtils.isStatic(method)) {
            return method;
        }
        MakeStaticHandler.invoke(method);
        final List<PsiMethod> methods = findMethodByName(method.getName(), scope)
                .stream()
                .filter(MethodUtils::isStatic)
                .filter(m -> MethodUtils.parametersCount(m) >= MethodUtils.parametersCount(method))
                .collect(Collectors.toList());
        return methods.isEmpty()? null : methods.get(0);
    }

    public static PsiElement findElement(String humanReadableName, AnalysisScope scope) {
        final PsiElement[] resultHolder = new PsiElement[1];
        scope.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                if (MethodUtils.calculateSignature(method).equals(humanReadableName)) {
                    resultHolder[0] = method;
                }
            }

            @Override
            public void visitClass(PsiClass aClass) {
                super.visitClass(aClass);
                if (humanReadableName.equals(aClass.getQualifiedName())) {
                    resultHolder[0] = aClass;
                }
            }

            @Override
            public void visitField(PsiField field) {
                super.visitField(field);
                //todo use correct comparision
                if (humanReadableName.equals(field.getName())) {
                    resultHolder[0] = field;
                }
            }
        });
        if (resultHolder[0] == null) {
            System.out.println("Not found: " + humanReadableName);
        }
        return resultHolder[0];
    }

    public static List<PsiMethod> findMethodByName(String name, AnalysisScope scope) {
        final List<PsiMethod> resultCollector = new ArrayList<>();
        scope.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                if (method.getName().equals(name)) {
                    resultCollector.add(method);
                }
            }
        });
        if (resultCollector.isEmpty()) { // todo remove debug output
            System.out.println(name);
        }
        return resultCollector;
    }
}
