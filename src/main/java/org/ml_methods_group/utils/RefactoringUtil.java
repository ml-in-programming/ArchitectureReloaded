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
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import com.intellij.refactoring.makeStatic.MakeStaticHandler;
import com.intellij.refactoring.move.moveInstanceMethod.MoveInstanceMethodDialog;
import com.intellij.refactoring.move.moveMembers.MoveMembersDialog;
import com.sixrr.metrics.utils.MethodUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.refactoring.Refactoring;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.ui.RefactoringsTableModel;

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
    private static Logger LOG = Logging.getLogger(RefactoringUtil.class);

    private static class CachedMember {
        public final PsiMember member;
        public final String oldName;
        public CachedMember(@NotNull PsiMember member, @NotNull String oldName) {
            this.member = member;
            this.oldName = oldName;
        }

        public PsiMember getMember() {
            return member;
        }

        public String getOldName() {
            return oldName;
        }
    }

    private RefactoringUtil() {
    }

    public static void moveRefactoring(@NotNull List<Refactoring> refactorings,
                                       @NotNull AnalysisScope scope,
                                       @Nullable RefactoringsTableModel model) {
        if (!checkValid(refactorings)) {
            throw new IllegalArgumentException("Units in refactorings list must be unique!");
        }
        final Map<PsiClass, List<PsiElement>> groupedRefactorings = prepareRefactorings(refactorings, scope);
        ApplicationManager.getApplication().runReadAction(() -> {
            for (Entry<PsiClass, List<PsiElement>> refactoring : groupedRefactorings.entrySet()) {
                final PsiClass target = refactoring.getKey();
                final List<CachedMember> members = refactoring.getValue().stream()
                        .sequential()
                        .filter(unit -> !(unit instanceof PsiMethod) || !moveInstanceMethod((PsiMethod) unit, target))
                        .map(RefactoringUtil::makeStatic) // no effect for already static members
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                final Set<String> accepted = moveMembersRefactoring(members, target, scope);
                model.setAcceptedRefactorings(accepted.stream().map(m -> Refactoring.createRefactoring(m, PsiSearchUtil.getHumanReadableName(target), 0, true, scope)).collect(Collectors.toSet()));
            }
        });
    }

    private static Set<String> moveMembersRefactoring(Collection<CachedMember> elements, PsiClass targetClass,
                                               AnalysisScope scope) {
        final Map<PsiClass, Set<CachedMember>> groupByCurrentClass = elements.stream()
                .collect(groupingBy((CachedMember cm) -> cm.member.getContainingClass(), Collectors.toSet()));

        final Set<String> accepted = new HashSet<>();
        for (Entry<PsiClass, Set<CachedMember>> movement : groupByCurrentClass.entrySet()) {
            final Set<String> names = movement.getValue().stream().map(CachedMember::getOldName).collect(Collectors.toSet());
            final Set<PsiMember> members = movement.getValue().stream().map(CachedMember::getMember).collect(Collectors.toSet());
            MoveMembersDialog dialog = new MoveMembersDialog(scope.getProject(), movement.getKey(), targetClass,
                    members, null);
            TransactionGuard.getInstance().submitTransactionAndWait(dialog::show);
            if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                accepted.addAll(names);
            }
        }
        return accepted;
    }

    private static CachedMember makeStatic(PsiElement element) {
        if (!(element instanceof PsiMember)) {
            return null;
        }
        final PsiMember member = (PsiMember) element;
        final String oldName = PsiSearchUtil.getHumanReadableName(member);
        if (isStatic(member)) {
            return new CachedMember(member, oldName);
        }
        if (!(member instanceof PsiMethod)) {
            return null;
        }
        PsiMethod method = (PsiMethod) element;
        if (method.isConstructor()) {
            return null;
        }
        TransactionGuard.getInstance().submitTransactionAndWait(() -> MakeStaticHandler.invoke(method));
        return isStatic(member) ? new CachedMember(member, oldName) : null;
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

    public static Map<Refactoring, String> getWarnings(List<Refactoring> refactorings, AnalysisScope scope) {
        final Set<String> allUnits = refactorings.stream()
                .map(Refactoring::getEntityName)
                .collect(Collectors.toSet());
        final Map<String, PsiElement> psiElements = PsiSearchUtil.findAllElements(allUnits, scope, Function.identity());
        Map<Refactoring, String> warnings = new HashMap<>();
        for (Refactoring refactoring : refactorings) {
            final PsiElement element = psiElements.get(refactoring.getEntityName());
            final String target = refactoring.getTargetName();
            String warning = "";
            if (element != null) {
                warning = ApplicationManager.getApplication()
                        .runReadAction((Computable<String>) () -> getWarning(element, target));
            }
            warnings.put(refactoring, warning);
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

    private static Map<PsiClass, List<PsiElement>> prepareRefactorings(List<Refactoring> refactorings,
                                                                       AnalysisScope scope) {
        final Set<String> names = new HashSet<>();
        refactorings.stream()
                .peek(refactoring -> names.add(refactoring.getEntityName()))
                .forEach(refactoring -> names.add(refactoring.getTargetName()));
        final Map<String, PsiElement> elements = findAllElements(names, scope, Function.identity());
        final HashMap<PsiClass, List<PsiElement>> result = new HashMap<>();
        for (Refactoring refactoring : refactorings) {
            final PsiClass target = (PsiClass) elements.get(refactoring.getTargetName());
            final PsiElement element = elements.get(refactoring.getEntityName());
            result.computeIfAbsent(target, x -> new ArrayList<>()).add(element);
        }
        return result;
    }

    public static boolean checkValid(Collection<Refactoring> refactorings) {
        final long uniqueUnits = refactorings.stream()
                .map(Refactoring::getEntityName)
                .distinct()
                .count();
        return uniqueUnits == refactorings.size();
    }

    public static List<Refactoring> filter(List<Refactoring> refactorings, AnalysisScope scope) {
        final Set<String> allUnits = refactorings.stream()
                .map(Refactoring::getEntityName)
                .collect(Collectors.toSet());
        final Map<String, PsiElement> psiElements = PsiSearchUtil.findAllElements(allUnits, scope, Function.identity());
        final List<Refactoring> validRefactorings = new ArrayList<>();
        for (Refactoring refactoring : refactorings) {
            final PsiElement element = psiElements.get(refactoring.getEntityName());
            if (element != null) {
                final boolean isMovable = ApplicationManager.getApplication()
                        .runReadAction((Computable<Boolean>) () -> isMovable(element));
                if (isMovable) {
                    validRefactorings.add(refactoring);
                }
            }
        }
        return validRefactorings;
    }

    private static boolean isMovable(PsiElement psiElement) {
        if (psiElement instanceof PsiField) {
            return MethodUtils.isStatic((PsiField) psiElement);
        } else if (psiElement instanceof PsiMethod) {
            final PsiMethod method = (PsiMethod) psiElement;
            return !MethodUtils.isAbstract(method) && !PSIUtil.isOverriding(method) && !method.isConstructor();
        }
        return false;
    }

    public static Map<String, String> toMap(List<Refactoring> refactorings) {
        return refactorings.stream().collect(Collectors.toMap(Refactoring::getEntityName, Refactoring::getTargetName));
    }

    public static List<Refactoring> intersect(Collection<List<Refactoring>> refactorings) {
        return refactorings.stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(refactoring -> refactoring.getEntityName() + "&" + refactoring.getTargetName(),
                        Collectors.toList()))
                .values().stream()
                .filter(collection -> collection.size() == refactorings.size())
                .map(RefactoringUtil::intersect)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static Refactoring intersect(List<Refactoring> refactorings) {
        return refactorings.stream()
                .min(Comparator.comparing(Refactoring::getAccuracy))
                .orElse(null);
    }

    /**
     * @param scope this argument is only required for backward compatibility with old version of
     * {@link Refactoring} class. It is needed to infer {@link PsiElement} from its name. If all
     * usages of {@link Refactoring#createRefactoring} are eliminated then this argument can also
     * be removed.
     */
    public static List<Refactoring> combine(Collection<List<Refactoring>> refactorings, AnalysisScope scope) {
        return refactorings.stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(Refactoring::getEntityName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> combine(entry.getValue(), entry.getKey(), refactorings.size(), scope))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * @param scope this argument is only required for backward compatibility with old version of
     * {@link Refactoring} class. It is needed to infer {@link PsiElement} from its name. If all
     * usages of {@link Refactoring#createRefactoring} are eliminated then this argument can also
     * be removed.
     */
    private static Refactoring combine(List<Refactoring> refactorings, String unit, int algorithmsCount, AnalysisScope scope) {
        boolean isUnitField = refactorings.get(0).isMoveFieldRefactoring();
        final Map<String, Double> target = refactorings.stream()
                .collect(Collectors.toMap(Refactoring::getTargetName, RefactoringUtil::getSquaredAccuarcy, Double::sum));
        return target.entrySet().stream()
                .max(Entry.comparingByValue())
                .map(entry -> Refactoring.createRefactoring(unit, entry.getKey(), Math.sqrt(entry.getValue() / algorithmsCount), isUnitField, scope))
                .orElse(null);
    }

    private static double getSquaredAccuarcy(Refactoring refactoring) {
        return refactoring.getAccuracy() * refactoring.getAccuracy();
    }
}
