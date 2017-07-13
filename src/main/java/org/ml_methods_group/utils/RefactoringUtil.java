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

package org.ml_methods_group.utils;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.refactoring.makeStatic.MakeStaticHandler;
import com.intellij.refactoring.move.MoveHandler;
import com.sixrr.metrics.utils.MethodUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.sixrr.metrics.utils.MethodUtils.isStatic;

public final class RefactoringUtil {
    private RefactoringUtil() {
    }

    public static void moveRefactoring(Map<String, String> refactorings, Project project, AnalysisScope scope) {
        final Map<String, List<String>> groupedMovements = refactorings.keySet().stream()
                .collect(Collectors.groupingBy(refactorings::get, Collectors.toList()));

        for (Entry<String, List<String>> refactoring : groupedMovements.entrySet()) {
            final List<PsiMember> members = refactoring.getValue().stream()
                    .sequential()
                    .map(name -> findElement(name, scope))
                    .map(element -> makeStatic((PsiMember) element, scope))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            moveMembersRefactoring(members, refactoring.getKey(), project, scope);
        }
    }

    private static void moveMembersRefactoring(Collection<PsiMember> elements,
                                               String targetClass,
                                               Project project,
                                               AnalysisScope scope) {
        ApplicationManager.getApplication().assertReadAccessAllowed();

        final Map<PsiClass, List<PsiElement>> groupByCurrentClass = elements.stream()
                .collect(Collectors.groupingBy(PsiMember::getContainingClass, Collectors.toList()));

        for (Entry<PsiClass, List<PsiElement>> movement : groupByCurrentClass.entrySet()) {
            final PsiElement destination = findElement(targetClass, scope);
            final PsiElement[] elementsToMove = movement.getValue().toArray(new PsiElement[0]);
            MoveHandler.doMove(project, elementsToMove, destination, DataContext.EMPTY_CONTEXT, null);
        }
    }

    public static PsiMember makeStatic(PsiMember element, AnalysisScope scope) {
        if (!(element instanceof PsiMethod)) {
            return isStatic(element)? element : null;
        }

        final PsiMethod method = (PsiMethod) element;
        if (method.isConstructor()) {
            return null;
        }

        if (isStatic(method)) {
            return method;
        }

        MakeStaticHandler.invoke(method);
        final List<PsiMethod> methods = findMethodByName(method.getName(), scope)
                .stream()
                .filter(MethodUtils::isStatic)
                .filter(m -> MethodUtils.parametersCount(m) >= MethodUtils.parametersCount(method))
                .collect(Collectors.toList());
        return methods.isEmpty() ? null : methods.get(0);
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
                final PsiClass containingClass = field.getContainingClass();
                if (containingClass == null) {
                    return;
                }
                final String fieldName = containingClass.getQualifiedName() + "." + field.getName();
                if (humanReadableName.equals(fieldName)) {
                    resultHolder[0] = field;
                }
            }
        });
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
        return resultCollector;
    }

//    public static String createDescription(String unit, String moveTo, AnalysisScope scope) {
//        return ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {
//            final PsiElement element = findElement(unit, scope);
//            if (element instanceof PsiMethod) {
//                final PsiMethod method = (PsiMethod) element;
//                final String moveFrom = method.getContainingClass().getQualifiedName();
//                final String descriptionKey;
//                descriptionKey = (isStatic(method) ? "" : "make.static.and.") + "move.description";
//                return ArchitectureReloadedBundle.message(descriptionKey, method.getName(), moveFrom, moveTo);
//            }
//            return "Unsupported element";
//        });
//    }

    public static String getName(PsiElement element) {
        if (element instanceof PsiClass) {
            return ((PsiClass) element).getQualifiedName();
        }

        if (element instanceof PsiMethod) {
            return MethodUtils.calculateSignature((PsiMethod) element);
        }

        if (element instanceof PsiField) {
            final PsiField field = (PsiField) element;
            final PsiClass fieldClass = field.getContainingClass();
            if (fieldClass == null) {
                return null;
            }
            return fieldClass.getQualifiedName() + "." + field.getName();
        }

        return null;
    }
}
