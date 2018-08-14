package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class FieldFilters {
    private FieldFilters() {}

    public static @NotNull Predicate<PsiField> sameClass(final @NotNull PsiClass clazz) {
        return field -> clazz.equals(field.getContainingClass());
    }
}
