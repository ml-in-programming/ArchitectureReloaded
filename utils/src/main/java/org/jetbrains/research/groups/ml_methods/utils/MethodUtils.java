package org.jetbrains.research.groups.ml_methods.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public final class MethodUtils {
    public static boolean isPublic(PsiMethod method) {
        if (method.hasModifierProperty(PsiModifier.PUBLIC)) {
            return true;
        }

        PsiClass containingClass = method.getContainingClass();

        return containingClass != null && containingClass.isInterface();
    }

    public static String extractMethodDeclaration(final @NotNull PsiMethod method) {
        String code = method.getText();

        code = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL).matcher(code).replaceAll("");
        code = Pattern.compile("//.*?$", Pattern.DOTALL | Pattern.MULTILINE).matcher(code).replaceAll("");

        code = Pattern.compile("\\{.*}", Pattern.DOTALL).matcher(code).replaceAll("");
        return code.trim();
    }
}
