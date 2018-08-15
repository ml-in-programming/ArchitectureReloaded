package org.jetbrains.research.groups.ml_methods.refactoring;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class MoveToClassRefactoring {
    private final @NotNull SmartPsiElementPointer<PsiMember> entity;

    private final @NotNull SmartPsiElementPointer<PsiClass> targetClass;

    public MoveToClassRefactoring(
        final @NotNull PsiMember entity,
        final @NotNull PsiClass target
    ) {
        this.entity = ApplicationManager.getApplication().runReadAction(
                (Computable<SmartPsiElementPointer<PsiMember>>) () ->
                        SmartPointerManager.getInstance(entity.getProject()).createSmartPsiElementPointer(entity)
        );
        this.targetClass = ApplicationManager.getApplication().runReadAction(
                (Computable<SmartPsiElementPointer<PsiClass>>) () ->
                        SmartPointerManager.getInstance(target.getProject()).createSmartPsiElementPointer(target)
        );
    }

    /**
     * Returns class which contains moved entity.
     */
    public abstract @Nullable Optional<PsiClass> getContainingClass();

    /**
     * Returns class which contains moved entity.
     */
    public abstract @NotNull PsiClass getContainingClassOrThrow();

    /**
     * Returns class in which entity is placed in this refactoring
     */
    public @NotNull Optional<PsiClass> getTargetClass() {
        return Optional.ofNullable(targetClass.getElement());
    }

    public @NotNull Optional<PsiMember> getEntity() {
        return Optional.ofNullable(entity.getElement());
    }

    /**
     * Returns class in which entity is placed in this refactoring
     */
    public @NotNull PsiClass getTargetClassOrThrow() {
        return Optional.ofNullable(targetClass.getElement()).orElseThrow(() ->
                new IllegalStateException("Cannot get target class. Reference is invalid."));
    }

    public @NotNull PsiMember getEntityOrThrow() {
        return Optional.ofNullable(entity.getElement()).orElseThrow(() ->
                new IllegalStateException("Cannot get entity. Reference is invalid."));
    }

    public abstract boolean isMoveFieldRefactoring();

    @NotNull
    public abstract <R> R accept(final @NotNull RefactoringVisitor<R> visitor);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MoveToClassRefactoring that = (MoveToClassRefactoring) o;

        return entity.equals(that.entity) && targetClass.equals(that.targetClass);
    }

    @Override
    public int hashCode() {
        int result = entity.hashCode();
        result = 31 * result + targetClass.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MoveToClassRefactoring{" +
                "entity=" + entity +
                ", targetClass=" + targetClass +
                '}';
    }
}
