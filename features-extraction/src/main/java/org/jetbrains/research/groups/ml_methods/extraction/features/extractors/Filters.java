package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiMethod;
import org.jetbrains.research.groups.ml_methods.utils.MethodUtils;

import java.util.function.Predicate;

public class Filters {
    private Filters() {}

    public static final Predicate<PsiMethod> isPublic = MethodUtils::isPublic;

    public static final Predicate<PsiMethod> isNotPublic = psiMethod -> !MethodUtils.isPublic(psiMethod);
}
