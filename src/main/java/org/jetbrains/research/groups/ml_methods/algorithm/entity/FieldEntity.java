package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiField;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

import static org.jetbrains.research.groups.ml_methods.utils.PsiSearchUtil.getHumanReadableName;

public class FieldEntity extends CodeEntity {
    private final @NotNull PsiField psiField;

    private final @NotNull ClassEntity containingClass;

    private final boolean isMovable;

    public FieldEntity(
        final @NotNull PsiField psiField,
        final @NotNull ClassEntity containingClass,
        final @NotNull RelevantProperties relevantProperties
    ) {
        super(relevantProperties);
        this.psiField = psiField;
        this.containingClass = containingClass;

        isMovable = ApplicationManager.getApplication().runReadAction(
            (Computable<Boolean>) () ->  MethodUtils.isStatic(psiField)
        );
    }

    @Override
    public @NotNull String getIdentifier() {
        return ApplicationManager.getApplication().runReadAction(
            (Computable<String>) () -> getHumanReadableName(psiField.getContainingClass()) + "." + psiField.getName()
        );
    }

    @Override
    public boolean isMovable() {
        return isMovable;
    }

    @Override
    public @NotNull String getContainingClassName() {
        final String name = getIdentifier();
        return name.substring(0, name.lastIndexOf('.'));
    }

    public @NotNull ClassEntity getContainingClass() {
        return containingClass;
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
}
