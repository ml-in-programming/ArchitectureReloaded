package org.jetbrains.research.groups.ml_methods.refactoring;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveToClassRefactoring;

/**
 * Representation of a refactoring which moves field to a target class.
 */
public class MoveFieldRefactoring extends MoveToClassRefactoring {
    private final @NotNull PsiField field;

    /**
     * Creates refactoring.
     *
     * @param field a field that is moved in this refactoring.
     * @param targetClass destination class in which given field is placed in this refactoring.
     */
    public MoveFieldRefactoring(
        final @NotNull PsiField field,
        final @NotNull PsiClass targetClass
    ) {
        super(field, targetClass);

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
