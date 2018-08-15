package org.jetbrains.research.groups.ml_methods.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.MethodUtils;
import org.apache.log4j.Logger;
import org.jetbrains.research.groups.ml_methods.logging.Logging;
import org.jetbrains.research.groups.ml_methods.refactoring.CalculatedRefactoring;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveToClassRefactoring;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.jetbrains.research.groups.ml_methods.utils.PSIUtil.getHumanReadableName;

public final class RefactoringUtil {
    private static Logger LOG = Logging.getLogger(RefactoringUtil.class);

    private RefactoringUtil() {
    }


    public static List<CalculatedRefactoring> filter(List<CalculatedRefactoring> refactorings) {
        final List<CalculatedRefactoring> validRefactorings = new ArrayList<>();
        for (CalculatedRefactoring refactoring : refactorings) {
            final PsiElement element = refactoring.getRefactoring().getEntityOrThrow();
            final boolean isMovable = ApplicationManager.getApplication()
                    .runReadAction((Computable<Boolean>) () -> isMovable(element));
            if (isMovable) {
                validRefactorings.add(refactoring);
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

    // TODO: rewrite method, if it is intersection of several sets then arguments must be sets.
    public static List<CalculatedRefactoring> intersect(Collection<List<CalculatedRefactoring>> refactorings) {
        return refactorings.stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(refactoring ->
                                getHumanReadableName(refactoring.getRefactoring().getEntityOrThrow()) +
                                        "&" +
                                        getHumanReadableName(refactoring.getRefactoring().getTargetClassOrThrow()),
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
                .collect(Collectors.groupingBy(it -> it.getRefactoring().getEntityOrThrow(), Collectors.toList()))
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
