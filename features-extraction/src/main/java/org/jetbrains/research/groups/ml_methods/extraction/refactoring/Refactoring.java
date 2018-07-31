package org.jetbrains.research.groups.ml_methods.extraction.refactoring;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
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

    public @NotNull
    PsiMethod getMethod() {
        return method;
    }

    public @NotNull
    PsiClass getTargetClass() {
        return targetClass;
    }
}
