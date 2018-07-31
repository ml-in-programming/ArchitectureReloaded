package org.jetbrains.research.groups.ml_methods.generation.constraints;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.Objects;

public class BasicGenerationConstraint implements GenerationConstraint {
    @Override
    public boolean acceptTargetClass(PsiClass aClass) {
        return aClass.getQualifiedName() != null;
    }

    @Override
    public boolean acceptMethod(PsiMethod method, AnalysisScope scope) {
        final PsiClass containingClass = method.getContainingClass();
        return  containingClass != null && containingClass.getQualifiedName() != null;
    }

    @Override
    public boolean acceptRefactoring(PsiMethod method, PsiClass aClass) {
        return !Objects.requireNonNull(method.getContainingClass()).equals(aClass);
    }
}
