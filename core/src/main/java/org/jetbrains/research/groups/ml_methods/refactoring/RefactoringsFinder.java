package org.jetbrains.research.groups.ml_methods.refactoring;

import com.google.common.collect.Sets;
import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.MethodUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

// TODO: understand why we come into one method more than once.
public class RefactoringsFinder extends JavaRecursiveElementVisitor {
    private static final @NotNull Logger LOGGER = Logger.getLogger(RefactoringsFinder.class);

    private final Map<RefactoringTextRepresentation, RefactoringPair> refactorings = new HashMap<>();
    private final List<RefactoringTextRepresentation> textualRefactorings;
    private int visitedMethods = 0;
    private int visitedClasses = 0;

    private RefactoringsFinder(List<RefactoringTextRepresentation> textualRefactorings) {
        this.textualRefactorings = textualRefactorings;
    }

    @NotNull
    public static List<MoveMethodRefactoring> find(AnalysisScope scope, List<RefactoringTextRepresentation> textualRefactorings) {
        RefactoringsFinder refactoringFinder = new RefactoringsFinder(textualRefactorings);
        LOGGER.info("Started finder...");
        scope.accept(refactoringFinder);
        LOGGER.info("Finder finished. Gathering results.");
        return refactoringFinder.createFoundRefactorings();
    }

    private List<MoveMethodRefactoring> createFoundRefactorings() {
        LOGGER.info("Visited methods: " + visitedMethods);
        LOGGER.info("Visited classes: " + visitedClasses);
        checkSearchResult();
        return refactorings.values().stream().
                map(refactoringPair ->
                        new MoveMethodRefactoring(refactoringPair.method, refactoringPair.aClass)).
                collect(Collectors.toList());
    }

    private void checkSearchResult() {
        // Check that method and class are found both
        for (Map.Entry<RefactoringTextRepresentation, RefactoringPair> entry : refactorings.entrySet()) {
            RefactoringTextRepresentation textFormRefactoring = entry.getKey();
            RefactoringPair refactoringPair = entry.getValue();
            if (refactoringPair.method == null) {
                throw new IllegalStateException("No method found for: " + textFormRefactoring.getMethodsSignature() +
                        " -> " + textFormRefactoring.getTargetClassQualifiedName());
            }
            if (refactoringPair.aClass == null) {
                throw new IllegalStateException("No class found for: " + textFormRefactoring.getMethodsSignature() +
                        " -> " + textFormRefactoring.getTargetClassQualifiedName());
            }
        }
        // Check that we haven't found something extra
        Set<RefactoringTextRepresentation> textualRefactoringsSet = new HashSet<>(textualRefactorings);
        Sets.SetView<RefactoringTextRepresentation> foundWithoutNeeded =
                Sets.difference(refactorings.keySet(), textualRefactoringsSet);
        foundWithoutNeeded.immutableCopy().stream().findAny().ifPresent(textFormRefactoring -> {
            throw new IllegalStateException("Found unnecessary: " + textFormRefactoring.getMethodsSignature() +
                    " -> " + textFormRefactoring.getTargetClassQualifiedName());
        });
        // Check that we found all that is necessary
        Sets.SetView<RefactoringTextRepresentation> neededWithoutFound =
                Sets.difference(textualRefactoringsSet, refactorings.keySet());
        neededWithoutFound.immutableCopy().stream().findAny().ifPresent(textFormRefactoring -> {
            throw new IllegalStateException("Not found: " + textFormRefactoring.getMethodsSignature() +
                    " -> " + textFormRefactoring.getTargetClassQualifiedName());
        });
    }

    @Override
    public void visitMethod(PsiMethod method) {
        visitedMethods++;
        List<RefactoringTextRepresentation> refactoringsOfPsiMethod =
                RefactoringUtils.getRefactoringsOfGivenMethod(textualRefactorings, method);
        refactoringsOfPsiMethod.forEach(textFormRefactoring ->
                refactorings.computeIfAbsent(textFormRefactoring, k -> new RefactoringPair()).
                        setMethod(method));
        super.visitMethod(method);
    }

    @Override
    public void visitClass(PsiClass aClass) {
        visitedClasses++;
        for (RefactoringTextRepresentation refactoringToPsiClass :
                RefactoringUtils.getRefactoringsToGivenClass(textualRefactorings, aClass)) {
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

                throw new IllegalStateException("Refactorings set is ambiguous. Candidates: " +
                        oldSignature + ", " + newSignature);
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