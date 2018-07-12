package org.ml_methods_group.algorithm.entity;

import com.intellij.psi.PsiField;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

public class FieldEntity extends Entity {
    private final @NotNull PsiField psiField;

    FieldEntity(final @NotNull PsiField psiField) {
        super(psiField);
        this.psiField = psiField;
        isMovable = MethodUtils.isStatic(psiField);
    }

    private FieldEntity(FieldEntity original) {
        super(original);
        this.psiField = original.psiField;
    }

    @Override
    public MetricCategory getCategory() {
        return MetricCategory.Package;
    }

    @Override
    public String getClassName() {
        final String name = getName();
        return name.substring(0, name.lastIndexOf('.'));
    }

    @Override
    public FieldEntity copy() {
        return new FieldEntity(this);
    }

    @Override
    public boolean isField() {
        return true;
    }

    public @NotNull PsiField getPsiField() {
        return psiField;
    }
}
