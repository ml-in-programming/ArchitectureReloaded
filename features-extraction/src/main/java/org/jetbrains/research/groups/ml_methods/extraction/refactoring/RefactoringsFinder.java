package org.jetbrains.research.groups.ml_methods.extraction.refactoring;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        List<TextFormRefactoring> matchedRefactorings = textualRefactorings.stream().
                filter(textualRefactoring -> {
                    String methodsSignature = MethodUtils.calculateSignature(method);
                    String methodsSignatureWithoutParams = methodsSignature.split("\\(")[0];
                    String refactoringSignature = textualRefactoring.getMethodsSignature();
                    return refactoringSignature.equals(methodsSignature) || refactoringSignature.equals(methodsSignatureWithoutParams);
                }).collect(Collectors.toList());
        if (matchedRefactorings.size() > 1) {
            throw new IllegalStateException("Refactorings list is ambiguous");
        }
        if (matchedRefactorings.size() == 1) {
            TextFormRefactoring textFormRefactoring = matchedRefactorings.get(0);
            if (!refactorings.computeIfAbsent(textFormRefactoring, k -> new RefactoringPair()).setMethod(method)) {
                throw new IllegalStateException("Refactorings list is ambiguous");
            }
        }
        super.visitMethod(method);
    }

    @Override
    public void visitClass(PsiClass aClass) {
        System.out.println("Visited class: " + aClass.getName());
        String classQualifiedName = aClass.getQualifiedName();
        List<TextFormRefactoring> refactoringsWithSameTargetClass = textualRefactorings.stream().
                filter(textualRefactoring -> textualRefactoring.getClassQualifiedName().equals(classQualifiedName)).
                collect(Collectors.toList());
        for (TextFormRefactoring matchedRefactoring : refactoringsWithSameTargetClass) {
            if (!refactorings.computeIfAbsent(matchedRefactoring, k -> new RefactoringPair()).setClass(aClass)) {
                throw new IllegalStateException("Refactorings list is ambiguous");
            }
        }
        super.visitClass(aClass);
    }

    private class RefactoringPair {
        private PsiMethod method;
        private PsiClass aClass;

        private boolean setMethod(@NotNull PsiMethod method) {
            PsiMethod oldMethod = this.method;
            this.method = method;
            return oldMethod == null || MethodUtils.calculateSignature(oldMethod).equals(MethodUtils.calculateSignature(method));
        }

        private boolean setClass(@NotNull PsiClass aClass) {
            PsiClass oldClass = this.aClass;
            this.aClass = aClass;
            return oldClass == null || Objects.equals(oldClass.getQualifiedName(), aClass.getQualifiedName());
        }
    }
}