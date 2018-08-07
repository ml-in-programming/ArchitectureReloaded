package org.jetbrains.research.groups.ml_methods.algorithm.entity.builders;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.ClassEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.RelevantProperties;

public class ClassEntityBuilder extends CodeEntityBuilder {
    private @Nullable ClassEntity result = null;

    private final @NotNull PsiClass psiClass;

    public ClassEntityBuilder(final @NotNull PsiClass psiClass) {
        this.psiClass = psiClass;
    }

    public @NotNull ClassEntity build() {
        if (result == null) {
            result = new ClassEntity(psiClass, new RelevantProperties());
        }

        return result;
    }
}
