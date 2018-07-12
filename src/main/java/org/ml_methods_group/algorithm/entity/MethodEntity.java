package org.ml_methods_group.algorithm.entity;

import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.utils.PSIUtil;

public class MethodEntity extends Entity {
    private final @NotNull PsiMethod psiMethod;

    MethodEntity(final @NotNull PsiMethod psiMethod) {
        super(psiMethod);
        this.psiMethod = psiMethod;
        isMovable = !PSIUtil.isOverriding(psiMethod) &&
                !MethodUtils.isAbstract(psiMethod) && !psiMethod.isConstructor();
    }

    private MethodEntity(MethodEntity original) {
        super(original);
        this.psiMethod = original.psiMethod;
    }

    @Override
    public MetricCategory getCategory() {
        return MetricCategory.Method;
    }

    @Override
    public String getClassName() {
        final String signature = getName();
        final String name = signature.substring(0, signature.indexOf('('));
        return name.substring(0, name.lastIndexOf('.'));
    }

    @Override
    public MethodEntity copy() {
        return new MethodEntity(this);
    }

    @Override
    public boolean isField() {
        return false;
    }

    public @NotNull PsiMethod getPsiMethod() {
        return psiMethod;
    }
}
