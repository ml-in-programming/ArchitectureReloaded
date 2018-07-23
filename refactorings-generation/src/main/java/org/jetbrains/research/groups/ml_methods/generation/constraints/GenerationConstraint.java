package org.jetbrains.research.groups.ml_methods.generation.constraints;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;

public interface GenerationConstraint {
    boolean acceptTargetClass(PsiClass aClass);
    boolean acceptMethod(PsiMethod method);
    boolean acceptRefactoring(PsiMethod method, PsiClass aClass);
}
