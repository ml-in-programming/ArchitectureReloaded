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
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.move.MoveHandler;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Created by Артём on 05.07.2017.
 */
public final class RefactoringUtil {
    private RefactoringUtil() {
    }

    public static void moveRefactoring(Map<PsiElement, PsiElement> refactorings, Project project, AnalysisScope scope) {
        final Map<PsiElement, List<PsiElement>> groupedMovements = refactorings.keySet().stream()
                .collect(Collectors.groupingBy(refactorings::get, Collectors.toList()));
        for (Entry<PsiElement, List<PsiElement>> refactoring : groupedMovements.entrySet()) {
            final PsiElement movement = refactoring.getKey();
            final PsiElement[] methods = refactoring.getValue().stream()
                    .toArray(PsiElement[]::new);
//            Arrays.stream(methods).forEach(e -> MakeStaticHandler.invoke((PsiTypeParameterListOwner) e));
                MoveHandler.doMove(project, methods, movement, DataContext.EMPTY_CONTEXT, null);
        }
    }

    public static Map<PsiElement, PsiElement> filterRefactorings(Map<PsiElement, PsiElement> refactorings) {
        return refactorings.entrySet().stream()
                .filter(e -> !e.getValue().getContainingFile().equals(e.getKey().getContainingFile())) // todo
                .filter(e -> MoveHandler.canMove(new PsiElement[]{e.getKey()}, e.getValue())) // todo
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}
