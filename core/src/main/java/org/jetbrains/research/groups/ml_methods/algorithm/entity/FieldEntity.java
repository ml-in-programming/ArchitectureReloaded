package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiField;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static org.jetbrains.research.groups.ml_methods.utils.PSIUtil.getUniqueName;

public class FieldEntity extends ClassInnerEntity {
    private final @NotNull PsiField psiField;

    private final boolean isMovable;

    public FieldEntity(
        final @NotNull PsiField psiField,
        final @NotNull ClassEntity containingClass
    ) {
        super(containingClass);
        this.psiField = psiField;

        isMovable = ApplicationManager.getApplication().runReadAction(
            (Computable<Boolean>) () ->  MethodUtils.isStatic(psiField)
        );
    }

    @Override
    public @NotNull String getIdentifier() {
        return ApplicationManager.getApplication().runReadAction(
            (Computable<String>) () -> getUniqueName(psiField.getContainingClass()) + "." + psiField.getName()
        );
    }

    @Override
    public boolean isMovable() {
        return isMovable;
    }

    @Override
    public <R> R accept(@NotNull CodeEntityVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public @NotNull MetricCategory getMetricCategory() {
        throw new UnsupportedOperationException(
            "Metrics reloaded doesn't support field metrics."
        );
    }

    public @NotNull PsiField getPsiField() {
        return psiField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldEntity that = (FieldEntity) o;
        return Objects.equals(psiField, that.psiField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(psiField);
    }
}
