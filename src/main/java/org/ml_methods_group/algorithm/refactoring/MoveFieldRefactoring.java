package org.ml_methods_group.algorithm.refactoring;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Representation of a refactoring which moves field to a target class.
 */
class MoveFieldRefactoring extends MoveToClassRefactoring {
    private final @NotNull PsiField field;

    /**
     * Creates refactoring.
     *
     * @param field a field that is moved in this refactoring.
     * @param targetClass destination class in which given field is placed in this refactoring.
     * @param accuracy
     */
    public MoveFieldRefactoring(
        final @NotNull PsiField field,
        final @NotNull PsiClass targetClass,
        final double accuracy
    ) {
        super(field, targetClass, accuracy);

        this.field = field;
    }

    @Override
    public boolean isMoveFieldRefactoring() {
        return true;
    }

    /**
     * Returns field that is moved in this refactoring.
     */
    public @NotNull PsiField getField() {
        return field;
    }

    @Override
    public @Nullable PsiClass getContainingClass() {
        return field.getContainingClass();
    }

    @NotNull
    public <R> R accept(final @NotNull RefactoringVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
