package org.jetbrains.research.groups.ml_methods.ui;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import com.intellij.refactoring.makeStatic.MakeStaticHandler;
import com.intellij.refactoring.move.moveInstanceMethod.MoveInstanceMethodDialog;
import com.intellij.refactoring.move.moveMembers.MoveMembersDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.refactoring.*;
import org.jetbrains.research.groups.ml_methods.utils.PsiSearchUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sixrr.metrics.utils.MethodUtils.isStatic;
import static java.util.stream.Collectors.groupingBy;
import static org.jetbrains.research.groups.ml_methods.utils.PsiSearchUtil.getHumanReadableName;

public class RefactoringsApplier {
    private RefactoringsApplier() {
    }

    public static Map<CalculatedRefactoring, String> getWarnings(List<CalculatedRefactoring> refactorings, AnalysisScope scope) {
        final Set<String> allUnits = refactorings.stream()
                .map(it -> it.getRefactoring().getEntityName())
                .collect(Collectors.toSet());
        final Map<String, PsiElement> psiElements = PsiSearchUtil.findAllElements(allUnits, scope, Function.identity());
        Map<CalculatedRefactoring, String> warnings = new HashMap<>();
        for (CalculatedRefactoring refactoring : refactorings) {
            final PsiElement element = psiElements.get(refactoring.getRefactoring().getEntityName());
            final String target = refactoring.getRefactoring().getTargetName();
            String warning = "";
            if (element != null) {
                warning = ApplicationManager.getApplication()
                        .runReadAction((Computable<String>) () -> getWarning(element, target));
            }
            warnings.put(refactoring, warning);
        }
        return warnings;
    }

    public static void moveRefactoring(@NotNull List<MoveToClassRefactoring> refactorings,
                                       @NotNull AnalysisScope scope,
                                       @Nullable RefactoringsTableModel model) {
        if (!checkValid(refactorings)) {
            throw new IllegalArgumentException("Units in refactorings list must be unique!");
        }

        final Map<PsiClass, List<MoveToClassRefactoring>> groupedRefactorings = prepareRefactorings(refactorings);
        ApplicationManager.getApplication().runReadAction(() -> {
            for (Map.Entry<PsiClass, List<MoveToClassRefactoring>> refactoring : groupedRefactorings.entrySet()) {
                final Set<MoveToClassRefactoring> accepted = new HashSet<>();

                final PsiClass target = refactoring.getKey();
                final List<MoveToClassRefactoring> filteredRefactorings = refactoring.getValue().stream()
                        .sequential()
                        .filter(r -> r.accept(new RefactoringVisitor<Boolean>() {
                            @Override
                            public @NotNull Boolean visit(final @NotNull MoveMethodRefactoring refactoring) {
                                if (moveInstanceMethod(refactoring.getMethod(), target)) {
                                    accepted.add(refactoring);
                                    return false;
                                } else {
                                    return true;
                                }
                            }

                            @Override
                            public @NotNull Boolean visit(final @NotNull MoveFieldRefactoring refactoring) {
                                return true;
                            }
                        }))
                        .filter(r -> makeStatic(r.getEntity())) // no effect for already static members
                        .collect(Collectors.toList());

                accepted.addAll(moveMembersRefactoring(filteredRefactorings, target, scope));

                if (model != null) {
                    model.setAcceptedRefactorings(accepted.stream().map(m -> new CalculatedRefactoring(m, 0)).collect(Collectors.toSet()));
                }
            }
        });
    }

    private static boolean checkValid(Collection<MoveToClassRefactoring> refactorings) {
        final long uniqueUnits = refactorings.stream()
                .map(MoveToClassRefactoring::getEntity)
                .distinct()
                .count();
        return uniqueUnits == refactorings.size();
    }

    private static @NotNull Map<PsiClass, List<MoveToClassRefactoring>> prepareRefactorings(
            final @NotNull List<MoveToClassRefactoring> refactorings
    ) {
        return refactorings.stream().collect(
                Collectors.groupingBy(MoveToClassRefactoring::getTargetClass, Collectors.toList())
        );
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

    private static boolean makeStatic(PsiElement element) {
        if (!(element instanceof PsiMember)) {
            return false;
        }
        final PsiMember member = (PsiMember) element;
        if (isStatic(member)) {
            return true;
        }
        if (!(member instanceof PsiMethod)) {
            return false;
        }
        PsiMethod method = (PsiMethod) element;
        if (method.isConstructor()) {
            return false;
        }
        TransactionGuard.getInstance().submitTransactionAndWait(() -> MakeStaticHandler.invoke(method));
        return isStatic(member);
    }

    private static Set<MoveToClassRefactoring> moveMembersRefactoring(Collection<MoveToClassRefactoring> elements, PsiClass targetClass,
                                                                      AnalysisScope scope) {
        final Map<PsiClass, Set<MoveToClassRefactoring>> groupByCurrentClass = elements.stream()
                .collect(groupingBy((MoveToClassRefactoring it) -> it.getEntity().getContainingClass(), Collectors.toSet()));

        final Set<MoveToClassRefactoring> accepted = new HashSet<>();
        for (Map.Entry<PsiClass, Set<MoveToClassRefactoring>> movement : groupByCurrentClass.entrySet()) {
            final Set<PsiMember> members = movement.getValue().stream().map(MoveToClassRefactoring::getEntity).collect(Collectors.toSet());
            MoveMembersDialog dialog = new MoveMembersDialog(scope.getProject(), movement.getKey(), targetClass,
                    members, null);
            TransactionGuard.getInstance().submitTransactionAndWait(dialog::show);
            if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                accepted.addAll(movement.getValue());
            }
        }
        return accepted;
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
}
