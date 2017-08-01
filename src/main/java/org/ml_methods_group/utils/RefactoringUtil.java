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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import com.intellij.refactoring.makeStatic.MakeStaticHandler;
import com.intellij.refactoring.move.moveInstanceMethod.MoveInstanceMethodDialog;
import com.intellij.refactoring.move.moveMembers.MoveMembersDialog;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sixrr.metrics.utils.MethodUtils.isStatic;
import static java.util.stream.Collectors.groupingBy;
import static org.ml_methods_group.utils.PsiSearchUtil.findAllElements;
import static org.ml_methods_group.utils.PsiSearchUtil.getHumanReadableName;

public final class RefactoringUtil {

    private RefactoringUtil() {
    }

    public static void moveRefactoring(@NotNull Map<String, String> refactorings,
                                       @NotNull AnalysisScope scope) {
        final Map<PsiClass, List<PsiElement>> groupedRefactorings = prepareRefactorings(refactorings, scope);
        ApplicationManager.getApplication().runReadAction(() -> {
            for (Entry<PsiClass, List<PsiElement>> refactoring : groupedRefactorings.entrySet()) {
                final PsiClass target = refactoring.getKey();
                final List<PsiMember> members = refactoring.getValue().stream()
                        .sequential()
                        .filter(unit -> !(unit instanceof PsiMethod) || !moveInstanceMethod((PsiMethod) unit, target))
                        .map(RefactoringUtil::makeStatic) // no effect for already static members
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                moveMembersRefactoring(members, refactoring.getKey(), scope);
            }
        });
    }

    private static void moveMembersRefactoring(Collection<PsiMember> elements, PsiClass targetClass,
                                               AnalysisScope scope) {
        final Map<PsiClass, Set<PsiMember>> groupByCurrentClass = elements.stream()
                .collect(groupingBy(PsiMember::getContainingClass, Collectors.toSet()));

        for (Entry<PsiClass, Set<PsiMember>> movement : groupByCurrentClass.entrySet()) {
            MoveMembersDialog dialog = new MoveMembersDialog(scope.getProject(), movement.getKey(), targetClass,
                    movement.getValue(), null);
            TransactionGuard.getInstance().submitTransactionAndWait(dialog::show);
        }
    }

    private static PsiMember makeStatic(PsiElement element) {
        if (!(element instanceof PsiMember)) {
            return null;
        }
        final PsiMember member = (PsiMember) element;
        if (isStatic(member)) {
            return member;
        }
        if (!(member instanceof PsiMethod)) {
            return null;
        }
        PsiMethod method = (PsiMethod) element;
        if (method.isConstructor()) {
            return null;
        }
        TransactionGuard.getInstance().submitTransactionAndWait(() -> MakeStaticHandler.invoke(method));
        return isStatic(member) ? member : null;
    }

    private static PsiVariable[] getAvailableVariables(PsiMethod method, String target) {
        if (target == null) {
            return new PsiVariable[0];
        }
        final PsiClass psiClass = method.getContainingClass();
        Stream<PsiVariable> parameters = Arrays.stream(method.getParameterList().getParameters());
        Stream<PsiVariable> fields = psiClass == null ? Stream.empty() : Arrays.stream(psiClass.getFields());
        return Stream.concat(parameters, fields)
                .filter(Objects::nonNull)
                .filter(p -> target.equals(p.getType().getCanonicalText()))
                .toArray(PsiVariable[]::new);
    }

    private static boolean moveInstanceMethod(@NotNull PsiMethod method, PsiClass target) {
        PsiVariable[] available = getAvailableVariables(method, target.getQualifiedName());
        if (available.length == 0) {
            return false;
        }
        MoveInstanceMethodDialog dialog = new MoveInstanceMethodDialog(method, available);
        dialog.setTitle("Move Instance Method " + getHumanReadableName(method));
        dialog.show();
        return true;
    }

    public static List<String> getWarnings(List<String> units, List<String> targets, AnalysisScope scope) {
        final Set<String> allUnits = new HashSet<>(units);
        final Map<String, PsiElement> psiElements = PsiSearchUtil.findAllElements(allUnits, scope, Function.identity());
        List<String> warnings = new ArrayList<>();
        for (int i = 0; i < units.size(); i++) {
            final PsiElement element = psiElements.get(units.get(i));
            final String target = targets.get(i);
            String warning = "";
            if (element != null) {
                warning = ApplicationManager.getApplication()
                        .runReadAction((Computable<String>) () -> getWarning(element, target));
            }
            warnings.add(warning);
        }
        return warnings;
    }

    private static String getWarning(PsiElement element, String target) {
        if (element instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) element;
            if (!isStatic(method) && getAvailableVariables(method, target).length == 0) {
                return "    Can't move " + getHumanReadableName(element) +
                        " like instance method. It will be converted to static method first";
            }
            if (method.isConstructor()) {
                return "    Sorry, can't move constructor";
            }
        } else if (element instanceof PsiField) {
            if (!isStatic((PsiField) element)) {
                return "    Sorry, can't move instance fields";
            }
        } else {
            return "    Sorry, can't move such elements";
        }
        return "";
    }

    private static Map<PsiClass, List<PsiElement>> prepareRefactorings(Map<String, String> refactorings,
                                                                       AnalysisScope scope) {
        final Set<String> names = new HashSet<>();
        names.addAll(refactorings.values());
        names.addAll(refactorings.keySet());
        final Map<String, PsiElement> elements = findAllElements(names, scope, Function.identity());
        final HashMap<PsiClass, List<PsiElement>> result = new HashMap<>();
        for (Entry<String, String> refactoring : refactorings.entrySet()) {
            final PsiClass target = (PsiClass) elements.get(refactoring.getValue());
            final PsiElement element = elements.get(refactoring.getKey());
            result.computeIfAbsent(target, x -> new ArrayList<>()).add(element);
        }
        return result;
    }
}
