package org.jetbrains.research.groups.ml_methods.algorithm.entity.builders;

import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.FieldEntity;

public class FieldEntityBuilder extends CodeEntityBuilder {
    private @Nullable FieldEntity result = null;

    private final @NotNull PsiField psiField;

    private final @NotNull ClassEntityBuilder containingClassBuilder;

    public FieldEntityBuilder(
        final @NotNull PsiField psiField,
        final @NotNull ClassEntityBuilder containingClassBuilder
    ) {
        this.psiField = psiField;
        this.containingClassBuilder = containingClassBuilder;
    }

    public @NotNull FieldEntity build() {
        if (result == null) {
            result = new FieldEntity(psiField, containingClassBuilder.build(), relevantProperties);
        }

        return result;
    }
}
