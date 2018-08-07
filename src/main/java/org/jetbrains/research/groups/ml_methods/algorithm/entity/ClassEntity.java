package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiClass;
import com.sixrr.metrics.MetricCategory;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ClassEntity extends CodeEntity {
    private final @NotNull PsiClass psiClass;

    public ClassEntity(final @NotNull PsiClass psiClass) {
        this.psiClass = psiClass;
    }

    @Override
    public @NotNull String getIdentifier() {
        return ApplicationManager.getApplication().runReadAction((Computable<String>) psiClass::getQualifiedName);
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
    public <R> R accept(@NotNull CodeEntityVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public @NotNull MetricCategory getMetricCategory() {
        return MetricCategory.Class;
    }

    public @NotNull PsiClass getPsiClass() {
        return psiClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassEntity that = (ClassEntity) o;
        return Objects.equals(psiClass, that.psiClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(psiClass);
    }
}
