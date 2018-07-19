package org.jetbrains.research.groups.ml_methods.extraction.refactoring;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static org.jetbrains.research.groups.ml_methods.extraction.refactoring.TextFormRefactoring.*;

// TODO: understand why we come into one method more than once.
public class RefactoringsFinder extends JavaRecursiveElementVisitor {
    private final Map<TextFormRefactoring, RefactoringPair> refactorings = new HashMap<>();
    private final List<TextFormRefactoring> textualRefactorings;

    private RefactoringsFinder(List<TextFormRefactoring> textualRefactorings) {
        this.textualRefactorings = textualRefactorings;
    }

    @NotNull
    static List<Refactoring> find(AnalysisScope scope, List<TextFormRefactoring> textualRefactorings) {
        RefactoringsFinder refactoringFinder = new RefactoringsFinder(textualRefactorings);
        System.out.println("Started finder...");
        scope.accept(refactoringFinder);
        return refactoringFinder.refactorings.values().stream().
                filter(refactoringPair ->
                        refactoringPair.method != null && refactoringPair.aClass != null).
                map(refactoringPair ->
                        new Refactoring(refactoringPair.method, refactoringPair.aClass)).
                collect(Collectors.toList());
    }

    @Override
    public void visitMethod(PsiMethod method) {
        System.out.println("Visited method: " + method.getName());
        Optional<TextFormRefactoring> refactoringOfPsiMethod = getRefactoringOfGivenMethod(textualRefactorings, method);
        refactoringOfPsiMethod.ifPresent(textFormRefactoring -> {
            refactorings.computeIfAbsent(textFormRefactoring, k -> new RefactoringPair()).setMethod(method);
        });
        super.visitMethod(method);
    }

    @Override
    public void visitClass(PsiClass aClass) {
        System.out.println("Visited class: " + aClass.getName());
        for (TextFormRefactoring refactoringToPsiClass : getRefactoringsToGivenClass(textualRefactorings, aClass)) {
            refactorings.computeIfAbsent(refactoringToPsiClass, k -> new RefactoringPair()).setClass(aClass);
        }
        super.visitClass(aClass);
    }

    private class RefactoringPair {
        private PsiMethod method;
        private PsiClass aClass;

        private void setMethod(@NotNull PsiMethod method) {
            PsiMethod oldMethod = this.method;
            this.method = method;

            if (oldMethod != null) {
                String oldSignature = MethodUtils.calculateSignature(oldMethod);
                String newSignature = MethodUtils.calculateSignature(method);

                throw new IllegalStateException("Refactorings list is ambiguous. Candidates: " + oldSignature + ", " + newSignature);
            }
        }

        private void setClass(@NotNull PsiClass aClass) {
            PsiClass oldClass = this.aClass;
            this.aClass = aClass;

            if (oldClass != null) {
                String oldName = oldClass.getQualifiedName();
                String newName = aClass.getQualifiedName();

                throw new IllegalStateException("Refactorings list is ambiguous. Candidates: " + oldName + ", " + newName);
            }
        }
    }
}