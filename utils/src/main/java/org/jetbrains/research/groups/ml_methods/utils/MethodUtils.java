package org.jetbrains.research.groups.ml_methods.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;

public final class MethodUtils {
    public static boolean isPublic(PsiMethod method) {
        if (method.hasModifierProperty(PsiModifier.PUBLIC)) {
            return true;
        }

        PsiClass containingClass = method.getContainingClass();

        return containingClass != null && containingClass.isInterface();
    }
}
