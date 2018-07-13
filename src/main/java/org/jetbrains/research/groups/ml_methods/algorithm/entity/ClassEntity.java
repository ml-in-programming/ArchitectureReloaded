package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import com.intellij.psi.PsiClass;
import com.sixrr.metrics.MetricCategory;
import org.jetbrains.annotations.NotNull;

public class ClassEntity extends Entity {
    private final @NotNull PsiClass psiClass;

    ClassEntity(final @NotNull PsiClass psiClass) {
        super(psiClass);
        this.psiClass = psiClass;
    }

    private ClassEntity(ClassEntity original) {
        super(original);
        this.psiClass = original.psiClass;
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
        getRelevantProperties().addNotOverrideMethod(method);
    }

    @Override
    public ClassEntity copy() {
        return new ClassEntity(this);
    }

    @Override
    public boolean isField() {
        return false;
    }

    public @NotNull PsiClass getPsiClass() {
        return psiClass;
    }
}
