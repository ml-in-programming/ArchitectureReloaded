package org.jetbrains.research.groups.ml_methods.extraction.refactoring;

import com.google.common.collect.Sets;
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
    private final Set<TextFormRefactoring> textualRefactorings;

    private RefactoringsFinder(Set<TextFormRefactoring> textualRefactorings) {
        this.textualRefactorings = textualRefactorings;
    }

    @NotNull
    static Set<Refactoring> find(AnalysisScope scope, Set<TextFormRefactoring> textualRefactorings) {
        RefactoringsFinder refactoringFinder = new RefactoringsFinder(textualRefactorings);
        System.out.println("Started finder...");
        scope.accept(refactoringFinder);
        return refactoringFinder.createFoundRefactorings();
    }

    private Set<Refactoring> createFoundRefactorings() {
        checkSearchResult();
        return refactorings.values().stream().
                map(refactoringPair ->
                        new Refactoring(refactoringPair.method, refactoringPair.aClass)).
                collect(Collectors.toSet());
    }

    private void checkSearchResult() {
        // Check that method and class are found both
        for (Map.Entry<TextFormRefactoring, RefactoringPair> entry : refactorings.entrySet()) {
            TextFormRefactoring textFormRefactoring = entry.getKey();
            RefactoringPair refactoringPair = entry.getValue();
            if (refactoringPair.method == null) {
                throw new IllegalStateException("No method found for: " + textFormRefactoring.getMethodsSignature() +
                        " -> " + textFormRefactoring.getClassQualifiedName());
            }
            if (refactoringPair.aClass == null) {
                throw new IllegalStateException("No class found for: " + textFormRefactoring.getMethodsSignature() +
                        " -> " + textFormRefactoring.getClassQualifiedName());
            }
        }
        // Check that we haven't found something extra
        Sets.SetView<TextFormRefactoring> foundWithoutNeeded =
                Sets.difference(refactorings.keySet(), new HashSet<>(textualRefactorings));
        foundWithoutNeeded.immutableCopy().stream().findAny().ifPresent(textFormRefactoring -> {
            throw new IllegalStateException("Found unnecessary: " + textFormRefactoring.getMethodsSignature() +
                    " -> " + textFormRefactoring.getClassQualifiedName());
        });
        // Check that we found all that is necessary
        Sets.SetView<TextFormRefactoring> neededWithoutFound =
                Sets.difference(new HashSet<>(textualRefactorings), refactorings.keySet());
        neededWithoutFound.immutableCopy().stream().findAny().ifPresent(textFormRefactoring -> {
            throw new IllegalStateException("Not found: " + textFormRefactoring.getMethodsSignature() +
                    " -> " + textFormRefactoring.getClassQualifiedName());
        });
    }

    @Override
    public void visitMethod(PsiMethod method) {
        Optional<TextFormRefactoring> refactoringOfPsiMethod = getRefactoringOfGivenMethod(textualRefactorings, method);
        refactoringOfPsiMethod.ifPresent(textFormRefactoring -> {
            refactorings.computeIfAbsent(textFormRefactoring, k -> new RefactoringPair()).setMethod(method);
        });
        super.visitMethod(method);
    }

    @Override
    public void visitClass(PsiClass aClass) {
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

                throw new IllegalStateException("Refactorings set is ambiguous. Candidates: " + oldSignature + ", " + newSignature);
            }
        }

        private void setClass(@NotNull PsiClass aClass) {
            PsiClass oldClass = this.aClass;
            this.aClass = aClass;

            if (oldClass != null) {
                String oldName = oldClass.getQualifiedName();
                String newName = aClass.getQualifiedName();

                throw new IllegalStateException("Refactorings set is ambiguous. Candidates: " + oldName + ", " + newName);
            }
        }
    }
}