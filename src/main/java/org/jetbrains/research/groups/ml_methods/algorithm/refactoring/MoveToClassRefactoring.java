package org.jetbrains.research.groups.ml_methods.algorithm.refactoring;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.utils.PsiSearchUtil;

import java.util.Optional;

public abstract class MoveToClassRefactoring {
    private final @NotNull SmartPsiElementPointer<PsiMember> entity;

    private final @NotNull SmartPsiElementPointer<PsiClass> targetClass;

    private final String entityName;

    private final String targetName;

    /**
     * This factory method is a replacement for old ctor. Previously {@link MoveToClassRefactoring} class
     * stored only names of entities that were involved in refactoring. Now {@link MoveToClassRefactoring}
     * class has subclasses for different type of refactorings.
     * Use constructors of {@link MoveMethodRefactoring} and {@link MoveFieldRefactoring} instead.
     */
    @Deprecated
    public static @NotNull
    MoveToClassRefactoring createRefactoring(
        final @NotNull String entity,
        final @NotNull String target,
        final boolean isEntityField,
        final @NotNull AnalysisScope scope
    ) {
        String exceptionMessage =
            "Unable to find PsiElement with given name during Refactoring creation";

        PsiElement entityElement =
            PsiSearchUtil.findElement(entity, scope)
                         .orElseThrow(() -> new IllegalArgumentException(exceptionMessage));

        PsiElement targetElement =
            PsiSearchUtil.findElement(target, scope)
                         .orElseThrow(() -> new IllegalArgumentException(exceptionMessage));

        if (!isEntityField) {
            return new MoveMethodRefactoring(
                (PsiMethod) entityElement,
                (PsiClass) targetElement
            );
        } else {
            return new MoveFieldRefactoring(
                (PsiField) entityElement,
                (PsiClass) targetElement
            );
        }
    }

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

        this.entityName = ApplicationManager.getApplication().runReadAction(
            (Computable<String>) () -> PsiSearchUtil.getHumanReadableName(entity)
        );

        this.targetName = ApplicationManager.getApplication().runReadAction(
            (Computable<String>) () -> PsiSearchUtil.getHumanReadableName(target)
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
        return getTargetClass().orElseThrow(() ->
                new IllegalStateException("Cannot get target class. Reference is invalid."));
    }

    public @NotNull PsiMember getEntityOrThrow() {
        return getEntity().orElseThrow(() ->
                new IllegalStateException("Cannot get entity. Reference is invalid."));
    }

    /**
     * If you need to identify code entity. Then it is better to identify it directly and not
     * through its name.
     * Use {@link #getEntity()} instead.
     */
    @Deprecated
    public String getEntityName() {
        return entityName;
    }

    /**
     * If you need to identify code entity. Then it is better to identify it directly and not
     * through its name.
     * Use {@link #getTargetClass()} instead.
     */
    @Deprecated
    public String getTargetName() {
        return targetName;
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
