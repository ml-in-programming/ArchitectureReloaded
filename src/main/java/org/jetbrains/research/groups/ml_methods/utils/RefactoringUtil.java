package org.jetbrains.research.groups.ml_methods.utils;

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
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.*;
import org.jetbrains.research.groups.ml_methods.config.Logging;
import org.jetbrains.research.groups.ml_methods.ui.RefactoringsTableModel;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sixrr.metrics.utils.MethodUtils.isStatic;
import static java.util.stream.Collectors.groupingBy;
import static org.jetbrains.research.groups.ml_methods.utils.PsiSearchUtil.getHumanReadableName;

public final class RefactoringUtil {
    private static Logger LOG = Logging.getLogger(RefactoringUtil.class);

    private RefactoringUtil() {
    }

    public static void moveRefactoring(@NotNull List<MoveToClassRefactoring> refactorings,
                                       @NotNull AnalysisScope scope,
                                       @Nullable RefactoringsTableModel model) {
        if (!checkValid(refactorings)) {
            throw new IllegalArgumentException("Units in refactorings list must be unique!");
        }

        final Map<PsiClass, List<MoveToClassRefactoring>> groupedRefactorings = prepareRefactorings(refactorings);
        ApplicationManager.getApplication().runReadAction(() -> {
            for (Entry<PsiClass, List<MoveToClassRefactoring>> refactoring : groupedRefactorings.entrySet()) {
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
                        .filter(r -> makeStatic(r.getEntityOrThrow())) // no effect for already static members
                        .collect(Collectors.toList());

                 accepted.addAll(moveMembersRefactoring(filteredRefactorings, target, scope));

                if (model != null) {
                    model.setAcceptedRefactorings(accepted.stream().map(m -> new CalculatedRefactoring(m, 0)).collect(Collectors.toSet()));
                }
            }
        });
    }

    private static Set<MoveToClassRefactoring> moveMembersRefactoring(Collection<MoveToClassRefactoring> elements, PsiClass targetClass,
                                               AnalysisScope scope) {
        final Map<PsiClass, Set<MoveToClassRefactoring>> groupByCurrentClass = elements.stream()
                .collect(groupingBy((MoveToClassRefactoring it) -> it.getEntityOrThrow().getContainingClass(), Collectors.toSet()));

        final Set<MoveToClassRefactoring> accepted = new HashSet<>();
        for (Entry<PsiClass, Set<MoveToClassRefactoring>> movement : groupByCurrentClass.entrySet()) {
            final Set<PsiMember> members = movement.getValue().stream().map(MoveToClassRefactoring::getEntityOrThrow).collect(Collectors.toSet());
            MoveMembersDialog dialog = new MoveMembersDialog(scope.getProject(), movement.getKey(), targetClass,
                    members, null);
            TransactionGuard.getInstance().submitTransactionAndWait(dialog::show);
            if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                accepted.addAll(movement.getValue());
            }
        }
        return accepted;
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

    private static @NotNull Map<PsiClass, List<MoveToClassRefactoring>> prepareRefactorings(
        final @NotNull List<MoveToClassRefactoring> refactorings
    ) {
        return refactorings.stream().collect(
            Collectors.groupingBy(MoveToClassRefactoring::getTargetClassOrThrow, Collectors.toList())
        );
    }

    public static boolean checkValid(Collection<MoveToClassRefactoring> refactorings) {
        final long uniqueUnits = refactorings.stream()
                .map(MoveToClassRefactoring::getEntity)
                .distinct()
                .count();
        return uniqueUnits == refactorings.size();
    }

    public static List<CalculatedRefactoring> filter(List<CalculatedRefactoring> refactorings, AnalysisScope scope) {
        final Set<String> allUnits = refactorings.stream()
                .map(it -> it.getRefactoring().getEntityName())
                .collect(Collectors.toSet());
        final Map<String, PsiElement> psiElements = PsiSearchUtil.findAllElements(allUnits, scope, Function.identity());
        final List<CalculatedRefactoring> validRefactorings = new ArrayList<>();
        for (CalculatedRefactoring refactoring : refactorings) {
            final PsiElement element = psiElements.get(refactoring.getRefactoring().getEntityName());
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

    public static Map<String, String> toMap(List<CalculatedRefactoring> refactorings) {
        return refactorings.stream().collect(Collectors.toMap(it -> it.getRefactoring().getEntityName(),it -> it.getRefactoring().getTargetName()));
    }

    public static List<CalculatedRefactoring> intersect(Collection<List<CalculatedRefactoring>> refactorings) {
        return refactorings.stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(refactoring -> refactoring.getRefactoring().getEntityName() + "&" + refactoring.getRefactoring().getTargetName(),
                        Collectors.toList()))
                .values().stream()
                .filter(collection -> collection.size() == refactorings.size())
                .map(RefactoringUtil::intersect)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static CalculatedRefactoring intersect(List<CalculatedRefactoring> refactorings) {
        return refactorings.stream()
                .min(Comparator.comparing(CalculatedRefactoring::getAccuracy))
                .orElse(null);
    }

    public static List<CalculatedRefactoring> combine(Collection<List<CalculatedRefactoring>> refactorings) {
        return refactorings.stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(it -> it.getRefactoring().getEntity(), Collectors.toList()))
                .entrySet().stream()
                .map(entry -> combine(entry.getValue(), refactorings.size()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static CalculatedRefactoring combine(List<CalculatedRefactoring> refactorings, int numberOfAlgorithms) {
        final Map<MoveToClassRefactoring, Double> target = refactorings.stream()
                .collect(Collectors.groupingBy(CalculatedRefactoring::getRefactoring, Collectors.summingDouble(RefactoringUtil::getSquaredAccuracy)));

        return target.entrySet().stream()
                .max(Entry.comparingByValue())
                .map(entry -> new CalculatedRefactoring(entry.getKey(),  Math.sqrt(entry.getValue() / numberOfAlgorithms)))
                .orElse(null);
    }

    private static double getSquaredAccuracy(CalculatedRefactoring refactoring) {
        return refactoring.getAccuracy() * refactoring.getAccuracy();
    }
}
