package org.ml_methods_group.algorithm.entity;

import com.intellij.psi.PsiClass;
import com.sixrr.metrics.MetricCategory;
import org.jetbrains.annotations.NotNull;

public class ClassEntity extends CodeEntity {
    private final @NotNull PsiClass psiClass;

    public ClassEntity(
        final @NotNull PsiClass psiClass,
        final @NotNull RelevantProperties relevantProperties
    ) {
        super(relevantProperties);
        this.psiClass = psiClass;
    }

    @Override
    public @NotNull String getIdentifier() {
        return psiClass.getQualifiedName();
    }

    @Override
    public boolean isMovable() {
        return true;
    }

    @Override
    public @NotNull String getContainingClassName() {
        return getIdentifier();
    }

    @Override
    public @NotNull MetricCategory getMetricCategory() {
        return MetricCategory.Class;
    }

    public @NotNull PsiClass getPsiClass() {
        return psiClass;
    }
}
