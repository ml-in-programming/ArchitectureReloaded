package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import com.intellij.psi.PsiClass;
import com.sixrr.metrics.MetricCategory;

public class ClassOldEntity extends OldEntity {
    public ClassOldEntity(PsiClass psiClass) {
        super(psiClass);
    }

    private ClassOldEntity(ClassOldEntity original) {
        super(original);
    }

    @Override
    public MetricCategory getCategory() {
        return MetricCategory.Class;
    }

    @Override
    public String getClassName() {
        return getName();
    }

    @Override
    public ClassOldEntity copy() {
        return new ClassOldEntity(this);
    }

    @Override
    public boolean isField() {
        return false;
    }
}
