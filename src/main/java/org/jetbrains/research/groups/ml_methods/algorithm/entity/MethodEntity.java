package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.utils.PSIUtil;

import java.util.Objects;

public class MethodEntity extends CodeEntity {
    private final @NotNull PsiMethod psiMethod;

    private final @NotNull ClassEntity containingClass;

    private final boolean isMovable;

    public MethodEntity(
        final @NotNull PsiMethod psiMethod,
        final @NotNull ClassEntity containingClass,
        final @NotNull RelevantProperties relevantProperties
    ) {
        super(relevantProperties);
        this.psiMethod = psiMethod;
        this.containingClass = containingClass;

        isMovable = ApplicationManager.getApplication().runReadAction((Computable<Boolean>)
                () ->  !PSIUtil.isOverriding(psiMethod) &&
                !MethodUtils.isAbstract(psiMethod) && !psiMethod.isConstructor());
    }

    @Override
    public @NotNull String getIdentifier() {
        return ApplicationManager.getApplication().runReadAction(
            (Computable<String>) () -> MethodUtils.calculateSignature(psiMethod)
        );
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

    public @NotNull ClassEntity getContainingClass() {
        return containingClass;
    }

    @Override
    public <R> R accept(@NotNull CodeEntityVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public @NotNull MetricCategory getMetricCategory() {
        return MetricCategory.Method;
    }

    public @NotNull PsiMethod getPsiMethod() {
        return psiMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodEntity that = (MethodEntity) o;
        return Objects.equals(psiMethod, that.psiMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(psiMethod);
    }
}
