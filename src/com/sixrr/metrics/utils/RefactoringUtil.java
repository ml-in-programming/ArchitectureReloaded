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
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.move.MoveCallback;
import com.intellij.refactoring.move.MoveHandler;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Артём on 05.07.2017.
 */
public class RefactoringUtil {
    public static void moveRefactoring(Map<String, String> refactorings, Project project, AnalysisScope scope) {
        final Set<String> elementsNames = new HashSet<>();
        elementsNames.addAll(refactorings.keySet());
        elementsNames.addAll(refactorings.values());
        final Map<String, PsiElement> psiElements = find(elementsNames, project, scope);
        final Map<String, List<String>> groupedMovements = refactorings.keySet().stream()
                .collect(Collectors.groupingBy(refactorings::get, Collectors.toList()));
        for (Entry<String, List<String>> refactoring : groupedMovements.entrySet()) {
            final PsiElement movement = psiElements.get(refactoring.getKey());
            final PsiElement[] methods = new PsiElement[refactoring.getValue().size()];
            for (int i = 0; i < methods.length; i++) {
                methods[i] = psiElements.get(refactoring.getValue().get(i));
            }
            MoveHandler.doMove(project, methods, movement, null, null);
        }
    }

    public static Map<String, PsiElement> find(Collection<String> names, Project project, AnalysisScope analysisScope) {
        final Map<String, PsiElement> result = new ConcurrentHashMap<>();
        analysisScope.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                final String identifier = MethodUtils.calculateSignature(method);
                if (names.contains(identifier)) {
                    result.put(identifier, method);
                }
            }

            @Override
            public void visitClass(PsiClass aClass) {
                super.visitClass(aClass);
                final String identifier = aClass.getQualifiedName();
                if (identifier != null && names.contains(identifier)) {
                    result.put(identifier, aClass);
                }
            }
        });
        return result;
    }
}
