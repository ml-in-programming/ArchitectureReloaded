package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import com.intellij.psi.PsiField;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

import static org.jetbrains.research.groups.ml_methods.utils.PsiSearchUtil.getHumanReadableName;

public class FieldEntity extends CodeEntity {
    private final @NotNull PsiField psiField;

    private final boolean isMovable;

    public FieldEntity(
        final @NotNull PsiField psiField,
        final @NotNull RelevantProperties relevantProperties
    ) {
        super(relevantProperties);
        this.psiField = psiField;

        isMovable = MethodUtils.isStatic(psiField);
    }

    @Override
    public @NotNull String getIdentifier() {
        return getHumanReadableName(psiField.getContainingClass()) + "." + psiField.getName();
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

    @Override
    public @NotNull
    MetricCategory getMetricCategory() {
        throw new UnsupportedOperationException(
            "Metrics reloaded doesn't support field metrics."
        );
    }

    public @NotNull PsiField getPsiField() {
        return psiField;
    }
}
