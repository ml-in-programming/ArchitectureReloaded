package org.jetbrains.research.groups.ml_methods.generation.constraints;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

public class AcceptAnyGenerationConstraint implements GenerationConstraint {
    @Override
    public boolean acceptTargetClass(PsiClass aClass) {
        return true;
    }

    @Override
    public boolean acceptMethod(PsiMethod method, AnalysisScope scope) {
        return true;
    }

    @Override
    public boolean acceptRefactoring(PsiMethod method, PsiClass aClass) {
        return method.getContainingClass() == null || !method.getContainingClass().equals(aClass);
    }
}
