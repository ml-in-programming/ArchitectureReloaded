package org.jetbrains.research.groups.ml_methods.generation.constraints;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

public interface GenerationConstraint {
    boolean acceptTargetClass(PsiClass aClass);
    boolean acceptMethod(PsiMethod method, AnalysisScope scope);
    boolean acceptRefactoring(PsiMethod method, PsiClass aClass);
}
