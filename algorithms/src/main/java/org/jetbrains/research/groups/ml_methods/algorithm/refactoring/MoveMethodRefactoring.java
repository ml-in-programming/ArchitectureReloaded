package org.jetbrains.research.groups.ml_methods.algorithm.refactoring;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Representation of a refactoring which moves method to a target class.
 */
public class MoveMethodRefactoring extends MoveToClassRefactoring {
    private final @NotNull PsiMethod method;

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

        this.method = method;
    }

    public MoveMethodRefactoring(
            final @NotNull PsiMethod method,
            final @NotNull PsiClass targetClass
    ) {
        super(method, targetClass);

        this.method = method;
    }

    @Override
    public boolean isMoveFieldRefactoring() {
        return false;
    }

    /**
     * Returns method that is moved in this refactoring.
     */
    public @NotNull PsiMethod getMethod() {
        return method;
    }

    @Override
    public @Nullable PsiClass getContainingClass() {
        return method.getContainingClass();
    }

    @NotNull
    public <R> R accept(final @NotNull RefactoringVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
