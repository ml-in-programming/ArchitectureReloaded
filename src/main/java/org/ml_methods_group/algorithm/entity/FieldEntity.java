package org.ml_methods_group.algorithm.entity;

import com.intellij.psi.PsiField;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.utils.MethodUtils;

public class FieldEntity extends Entity {
    FieldEntity(PsiField field) {
        super(field);
        isMovable = MethodUtils.isStatic(field);
    }

    private FieldEntity(FieldEntity original) {
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
    public FieldEntity copy() {
        return new FieldEntity(this);
    }

    @Override
    public boolean isField() {
        return true;
    }
}
