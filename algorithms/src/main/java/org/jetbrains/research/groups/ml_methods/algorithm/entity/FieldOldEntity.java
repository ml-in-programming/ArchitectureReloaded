package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import com.intellij.psi.PsiField;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.utils.MethodUtils;

public class FieldOldEntity extends OldEntity {
    public FieldOldEntity(PsiField field) {
        super(field);
        isMovable = MethodUtils.isStatic(field);
    }

    private FieldOldEntity(FieldOldEntity original) {
        super(original);
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
    public FieldOldEntity copy() {
        return new FieldOldEntity(this);
    }

    @Override
    public boolean isField() {
        return true;
    }
}
