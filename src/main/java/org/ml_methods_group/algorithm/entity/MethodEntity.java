package org.ml_methods_group.algorithm.entity;

import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.utils.PSIUtil;

public class MethodEntity extends CodeEntity {
    private final @NotNull PsiMethod psiMethod;

    private final boolean isMovable;

    public MethodEntity(
        final @NotNull PsiMethod psiMethod,
        final @NotNull RelevantProperties relevantProperties
    ) {
        super(relevantProperties);
        this.psiMethod = psiMethod;

        isMovable = !PSIUtil.isOverriding(psiMethod) &&
                !MethodUtils.isAbstract(psiMethod) && !psiMethod.isConstructor();
    }

    @Override
    public @NotNull String getIdentifier() {
        return MethodUtils.calculateSignature(psiMethod);
    }

    @Override
    public boolean isMovable() {
        return isMovable;
    }

    @Override
    public @NotNull String getContainingClassName() {
        final String signature = getIdentifier();
        final String name = signature.substring(0, signature.indexOf('('));
        return name.substring(0, name.lastIndexOf('.'));
    }

    @Override
    public @NotNull
    MetricCategory getMetricCategory() {
        return MetricCategory.Method;
    }

    public @NotNull PsiMethod getPsiMethod() {
        return psiMethod;
    }
}
