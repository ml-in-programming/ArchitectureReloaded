package org.jetbrains.research.groups.ml_methods.extraction.refactoring;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

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

    public TextFormRefactoring toTextFormRefactoring() {
        final PsiParameter[] parameters = method.getParameterList().getParameters();
        List<String> parametersClasses = new LinkedList<>();
        for (PsiParameter psiParameter : parameters) {
            parametersClasses.add(psiParameter.getType().getPresentableText());
        }
        return new TextFormRefactoring(
                method.getContainingClass() != null ? method.getContainingClass().getQualifiedName() : null,
                method.getName(), parametersClasses, targetClass.getQualifiedName());
    }
}
