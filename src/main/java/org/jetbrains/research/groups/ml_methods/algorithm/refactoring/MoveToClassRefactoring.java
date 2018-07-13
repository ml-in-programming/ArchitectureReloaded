package org.jetbrains.research.groups.ml_methods.algorithm.refactoring;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MoveToClassRefactoring extends Refactoring {
    private final @NotNull PsiClass targetClass;

    public MoveToClassRefactoring(
        final @NotNull PsiElement entity,
        final @NotNull PsiClass targetClass,
        final double accuracy
    ) {
        super(entity, targetClass, accuracy);
        this.targetClass = targetClass;
    }

    /**
     * Returns class which contains moved entity.
     */
    public abstract @Nullable PsiClass getContainingClass();

    /**
     * Returns class in which entity is placed in this refactoring
     */
    public @NotNull PsiClass getTargetClass() {
        return targetClass;
    }
}
