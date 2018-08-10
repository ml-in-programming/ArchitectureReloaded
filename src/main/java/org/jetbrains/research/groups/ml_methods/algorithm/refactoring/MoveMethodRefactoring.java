package org.jetbrains.research.groups.ml_methods.algorithm.refactoring;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Representation of a refactoring which moves method to a target class.
 */
public class MoveMethodRefactoring extends MoveToClassRefactoring {
    private final @NotNull SmartPsiElementPointer<PsiMethod> method;

    /**
     * Creates refactoring.
     *
     * @param method a method that is moved in this refactoring.
     * @param targetClass destination class in which given method is placed in this refactoring.
     */
    public MoveMethodRefactoring(
        final @NotNull PsiMethod method,
        final @NotNull PsiClass targetClass
    ) {
        super(method, targetClass);

        this.method = ApplicationManager.getApplication().runReadAction(
                (Computable<SmartPsiElementPointer<PsiMethod>>) () ->
                        SmartPointerManager.getInstance(method.getProject()).createSmartPsiElementPointer(method)
        );
    }

    @Override
    public boolean isMoveFieldRefactoring() {
        return false;
    }

    /**
     * Returns method that is moved in this refactoring.
     */
    public @NotNull Optional<PsiMethod> getMethod() {
        return Optional.ofNullable(method.getElement());
    }

    /**
     * Returns method that is moved in this refactoring.
     */
    public @NotNull PsiMethod getMethodOrThrow() {
        return getMethod().orElseThrow(() ->
                new IllegalStateException("Cannot get method. Reference is invalid."));
    }

    @Override
    public @Nullable Optional<PsiClass> getContainingClass() {
        return method.getElement() == null ?
                Optional.empty() : Optional.ofNullable(method.getElement().getContainingClass());
    }

    @NotNull
    @Override
    public PsiClass getContainingClassOrThrow() {
        return Optional.ofNullable(getMethodOrThrow().getContainingClass())
                .orElseThrow(() -> new IllegalStateException("No containing class."));
    }

    @NotNull
    public <R> R accept(final @NotNull RefactoringVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
