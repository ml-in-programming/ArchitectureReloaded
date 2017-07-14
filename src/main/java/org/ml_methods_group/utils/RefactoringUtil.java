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
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.refactoring.makeStatic.MakeStaticHandler;
import com.intellij.refactoring.move.MoveHandler;
import com.intellij.refactoring.move.moveInstanceMethod.MoveInstanceMethodDialog;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sixrr.metrics.utils.MethodUtils.isStatic;
import static org.ml_methods_group.utils.PsiSearchUtil.*;

public final class RefactoringUtil {

    private RefactoringUtil() {
    }

    public static void moveRefactoring(@NotNull Map<String, String> refactorings,
                                       @NotNull Project project,
                                       @NotNull AnalysisScope scope) {
        ApplicationManager.getApplication().runReadAction(() -> {
            final Map<String, List<String>> groupedMovements = refactorings.keySet().stream()
                    .collect(Collectors.groupingBy(refactorings::get, Collectors.toList()));

            for (Entry<String, List<String>> refactoring : groupedMovements.entrySet()) {
                String targetClass = refactoring.getKey();
                final List<PsiMember> members = refactoring.getValue().stream()
                        .sequential()
                        .filter(unit -> !tryMoveInstanceMethod(unit, targetClass, scope))
                        .map(unit -> tryMakeStatic(unit, scope)) // no effect for already static members
                        .map(o -> o.map(unit -> findElement(unit, scope, PsiMember.class::cast).orElse(null)))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                moveMembersRefactoring(members, refactoring.getKey(), project, scope);
            }
        });
    }

    private static void moveMembersRefactoring(Collection<PsiMember> elements, String targetClass,
                                               Project project, AnalysisScope scope) {
        final Map<PsiClass, List<PsiElement>> groupByCurrentClass = elements.stream()
                .collect(Collectors.groupingBy(PsiMember::getContainingClass, Collectors.toList()));

        for (Entry<PsiClass, List<PsiElement>> movement : groupByCurrentClass.entrySet()) {
            final Optional<PsiElement> destination = findElement(targetClass, scope);
            if (!destination.isPresent()) {
                return;
            }

            final PsiElement[] array = movement.getValue().toArray(new PsiElement[0]);
            TransactionGuard.getInstance().submitTransactionAndWait(() ->
                    MoveHandler.doMove(project, array, destination.get(), DataContext.EMPTY_CONTEXT, null));
        }
    }

    private static Optional<String> tryMakeStatic(String unit, AnalysisScope scope) {
        return findElement(unit, scope, element -> tryMakeStatic((PsiMember) element, scope));
    }

    private static String tryMakeStatic(PsiMember element, AnalysisScope scope) {
        if (isStatic(element)) {
            return getHumanReadableName(element);
        }
        if (!(element instanceof PsiMethod)) {
            return null;
        }
        PsiMethod method = (PsiMethod) element;
        if (method.isConstructor()) {
            return null;
        }
        TransactionGuard.getInstance().submitTransactionAndWait(() -> MakeStaticHandler.invoke(method));
        return findMethodByName(method.getName(), scope)
                .filter(MethodUtils::isStatic)
                .map(PsiSearchUtil::getHumanReadableName)
                .orElse(null);
    }

    private static boolean tryMoveInstanceMethod(String unit, String target, AnalysisScope scope) {
        return findElement(unit, scope, e -> e instanceof PsiMethod && !isStatic((PsiMethod) e)
                && tryMoveInstanceMethod((PsiMethod) e, target)).orElse(false);
    }

    private static boolean tryMoveInstanceMethod(@NotNull PsiMethod method, String target) {
        PsiClass containingClass = method.getContainingClass();
        Stream<PsiParameter> parameters = Arrays.stream(method.getParameterList().getParameters());
        Stream<PsiField> fields = containingClass == null? Stream.empty() : Arrays.stream(containingClass.getFields());
        PsiField[] available = Stream.concat(parameters, fields)
                .filter(p -> target.equals(p.getType().getCanonicalText()))
                .toArray(PsiField[]::new);
        if (available.length == 0) {
            return false;
        }
        MoveInstanceMethodDialog dialog = new MoveInstanceMethodDialog(method, available);
        dialog.show();
        return dialog.isOK();
    }
}
