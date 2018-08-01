package org.jetbrains.research.groups.ml_methods.extraction.refactoring;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Refactoring {
    private final @NotNull
    PsiMethod method;

    private final @NotNull
    PsiClass targetClass;

    public Refactoring(final @NotNull PsiMethod method, final @NotNull PsiClass targetClass) {
        this.method = method;
        this.targetClass = targetClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Refactoring that = (Refactoring) o;
        return Objects.equals(method, that.method) &&
                Objects.equals(targetClass, that.targetClass);
    }

    @Override
    public int hashCode() {

        return Objects.hash(method, targetClass);
    }

    public @NotNull

    PsiMethod getMethod() {
        return method;
    }

    public @NotNull
    PsiClass getTargetClass() {
        return targetClass;
    }
}
