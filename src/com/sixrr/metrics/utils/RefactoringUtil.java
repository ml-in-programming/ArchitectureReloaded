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
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import com.intellij.refactoring.makeStatic.MakeStaticHandler;
import com.intellij.refactoring.move.MoveHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sixrr.metrics.utils.MethodUtils.calculateSignature;
import static com.sixrr.metrics.utils.MethodUtils.isStatic;

/**
 * Created by Артём on 05.07.2017.
 */
public final class RefactoringUtil {
    private static final Function<Object, String> NULL_SUPPLIER = s -> null;

    private RefactoringUtil() {
    }

    public static void moveRefactoring(Map<String, String> refactorings, Project project, AnalysisScope scope) {
        ApplicationManager.getApplication().runReadAction(() -> {
            final Map<String, List<String>> groupedMovements = refactorings.keySet().stream()
                    .collect(Collectors.groupingBy(refactorings::get, Collectors.toList()));
            for (Entry<String, List<String>> refactoring : groupedMovements.entrySet()) {
                final List<PsiMember> members = refactoring.getValue().stream()
                        .sequential()
                        .map(name -> findElement(name, scope))
                        .filter(Optional::isPresent)
                        .map(element -> makeStatic((PsiMember) element.get(), scope))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                moveMembersRefactoring(members, refactoring.getKey(), project, scope);
            }
        });
    }

    private static void moveMembersRefactoring(Collection<PsiMember> elements, String targetClass, Project project,
                                               AnalysisScope scope) {
        final Map<PsiClass, List<PsiElement>> groupByCurrentClass = elements.stream()
                .collect(Collectors.groupingBy(PsiMember::getContainingClass, Collectors.toList()));
        for (Entry<PsiClass, List<PsiElement>> movement : groupByCurrentClass.entrySet()) {
            final Optional<PsiElement> destiny = findElement(targetClass, scope);
            if (!destiny.isPresent()) {
                return;
            }
            final PsiElement[] array = movement.getValue().stream().toArray(PsiElement[]::new);
            MoveHandler.doMove(project, array, destiny.get(), DataContext.EMPTY_CONTEXT, null);
        }
    }

    private static Optional<PsiMember> makeStatic(PsiMember element, AnalysisScope scope) {
        if (!(element instanceof PsiMethod)) {
            return Optional.of(element).filter(MethodUtils::isStatic);
        }
        final PsiMethod method = (PsiMethod) element;
        if (method.isConstructor()) {
            return Optional.empty();
        }
        if (isStatic(method)) {
            return Optional.of(method);
        }
        MakeStaticHandler.invoke(method);
        return findMethodByName(method.getName(), scope)
                .filter(MethodUtils::isStatic)
                .filter(m -> MethodUtils.parametersCount(m) >= MethodUtils.parametersCount(method))
                .map(m -> (PsiMember) m);
    }

    private static Optional<PsiElement> findElement(String humanReadableName, AnalysisScope scope) {
        return runSearch(RefactoringUtil::getHumanReadableName,
                RefactoringUtil::getHumanReadableName,
                RefactoringUtil::getHumanReadableName,
                scope, humanReadableName);
    }

    private static Optional<PsiMethod> findMethodByName(String name, AnalysisScope scope) {
        return runSearch(NULL_SUPPLIER, PsiMethod::getName, NULL_SUPPLIER, scope, name)
                .map(e -> (PsiMethod) e);
    }

    public static Optional<String> getElementText(String unit, AnalysisScope scope) {
        return ApplicationManager.getApplication()
                .runReadAction((Computable<Optional<String>>) () -> findElement(unit, scope).map(PsiElement::getText));
    }

    public static String createDescription(String unit, String moveTo, AnalysisScope scope) {
        return ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {
            final Optional<PsiElement> element = findElement(unit, scope);
            if (!element.isPresent()) {
                return "Element not found";
            }
            if (element.get() instanceof PsiMethod) {
                final PsiMethod method = (PsiMethod) element.get();
                final String moveFrom = getHumanReadableName(method.getContainingClass());
                final String descriptionKey = (isStatic(method) ? "" : "make.static.and.") + "move.description";
                return ArchitectureReloadedBundle.message(descriptionKey, method.getName(), moveFrom, moveTo);
            }
            return "Unsupported element";
        });
    }

    public static String getHumanReadableName(@Nullable PsiElement element) {
        if (element instanceof PsiMethod) {
            return calculateSignature((PsiMethod) element);
        } else if (element instanceof PsiClass) {
            return ((PsiClass) element).getQualifiedName();
        } else if (element instanceof PsiField) {
            final PsiMember field = (PsiMember) element;
            return getHumanReadableName(field.getContainingClass()) + "." + field.getName();
        }
        return "???";
    }

    private static Optional<PsiElement> runSearch(Function<? super PsiClass, String> classToString,
                                                  Function<? super PsiMethod, String> methodToString,
                                                  Function<? super PsiField, String> fieldToString,
                                                  AnalysisScope scope, String request) {
        final PsiElement[] resultHolder = new PsiElement[0];
        scope.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);
                if (request.equals(methodToString.apply(method))) {
                    resultHolder[0] = method;
                }
            }

            @Override
            public void visitClass(PsiClass aClass) {
                super.visitClass(aClass);
                if (request.equals(classToString.apply(aClass))) {
                    resultHolder[0] = aClass;
                }
            }

            @Override
            public void visitField(PsiField field) {
                super.visitField(field);
                if (request.equals(fieldToString.apply(field))) {
                    resultHolder[0] = field;
                }
            }
        });
        return Optional.ofNullable(resultHolder[0]);
    }
}
