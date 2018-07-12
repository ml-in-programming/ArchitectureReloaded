package org.ml_methods_group.algorithm.entity;

import com.intellij.psi.PsiClass;
import com.sixrr.metrics.MetricCategory;

public class ClassEntity extends Entity {
    ClassEntity(PsiClass psiClass) {
        super(psiClass);
    }

    private ClassEntity(ClassEntity original) {
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


    public void removeFromClass(String method) {
        getRelevantProperties().removeMethod(method);
    }

    public void addToClass(String method) {
        getRelevantProperties().addMethod(method);
    }

    @Override
    public ClassEntity copy() {
        return new ClassEntity(this);
    }

    @Override
    public boolean isField() {
        return false;
    }
}
