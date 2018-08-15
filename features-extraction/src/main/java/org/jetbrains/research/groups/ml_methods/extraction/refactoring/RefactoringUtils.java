package org.jetbrains.research.groups.ml_methods.extraction.refactoring;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.List;
import java.util.stream.Collectors;

public class RefactoringUtils {
    public static List<RefactoringTextRepresentation>
    getRefactoringsOfGivenMethod(List<RefactoringTextRepresentation> textualRefactorings,
                                 PsiMethod method) {
        return textualRefactorings.stream().
                filter(textualRefactoring -> textualRefactoring.isOfGivenMethod(method)).collect(Collectors.toList());
    }

    public static List<RefactoringTextRepresentation>
    getRefactoringsToGivenClass(List<RefactoringTextRepresentation> textualRefactorings,
                                PsiClass aClass) {
        return textualRefactorings.stream().
                filter(textualRefactoring -> textualRefactoring.isToGivenPsiClass(aClass)).
                collect(Collectors.toList());
    }
}
