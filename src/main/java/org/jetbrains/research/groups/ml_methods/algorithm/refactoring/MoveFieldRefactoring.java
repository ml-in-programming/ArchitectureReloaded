package org.jetbrains.research.groups.ml_methods.algorithm.refactoring;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Representation of a refactoring which moves field to a target class.
 */
public class MoveFieldRefactoring extends MoveToClassRefactoring {
    private final @NotNull SmartPsiElementPointer<PsiField> field;

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

        this.field = ApplicationManager.getApplication().runReadAction(
                (Computable<SmartPsiElementPointer<PsiField>>) () ->
                        SmartPointerManager.getInstance(field.getProject()).createSmartPsiElementPointer(field)
        );
    }

    @Override
    public boolean isMoveFieldRefactoring() {
        return true;
    }

    /**
     * Returns field that is moved in this refactoring.
     */
    public @NotNull Optional<PsiField> getField() {
        return Optional.ofNullable(field.getElement());
    }

    /**
     * Returns field that is moved in this refactoring.
     */
    public @NotNull PsiField getFieldOrThrow() {
        return getField().orElseThrow(() -> new IllegalStateException("Cannot get field. Reference is invalid."));
    }

    @Override
    public @Nullable PsiClass getContainingClass() {
        return field.getElement() == null ? null : field.getElement().getContainingClass();
    }

    @Nullable
    @Override
    public PsiClass getContainingClassOrThrow() {
        return getFieldOrThrow().getContainingClass();
    }

    @NotNull
    public <R> R accept(final @NotNull RefactoringVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
