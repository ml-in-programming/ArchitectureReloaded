package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.utils.MethodUtils;

import java.util.function.Predicate;

public class MethodFilters {
    private MethodFilters() {}

    public static final Predicate<PsiMethod> isPublic = MethodUtils::isPublic;

    public static final Predicate<PsiMethod> isNotPublic =
            psiMethod -> !MethodUtils.isPublic(psiMethod);

    public static final Predicate<PsiMethod> isStatic =
            com.sixrr.metrics.utils.MethodUtils::isStatic;

    public static final Predicate<PsiMethod> isNotStatic =
            psiMethod -> !com.sixrr.metrics.utils.MethodUtils.isStatic(psiMethod);

    public static @NotNull Predicate<PsiMethod> sameClass(final @NotNull PsiClass clazz) {
        return method -> clazz.equals(method.getContainingClass());
    }
}
