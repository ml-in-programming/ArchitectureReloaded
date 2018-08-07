package org.jetbrains.research.groups.ml_methods.algorithm.entity.builders;

import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.MethodEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.RelevantProperties;

public class MethodEntityBuilder extends CodeEntityBuilder {
    private @Nullable MethodEntity result = null;

    private final @NotNull PsiMethod psiMethod;

    private final @NotNull ClassEntityBuilder containingClassBuilder;

    public MethodEntityBuilder(
        final @NotNull PsiMethod psiMethod,
        final @NotNull ClassEntityBuilder containingClassBuilder
    ) {
        this.psiMethod = psiMethod;
        this.containingClassBuilder = containingClassBuilder;
    }

    public @NotNull MethodEntity build() {
        if (result == null) {
            result = new MethodEntity(psiMethod, containingClassBuilder.build(), new RelevantProperties());
        }

        return result;
    }
}
