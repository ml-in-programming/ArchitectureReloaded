package org.jetbrains.research.groups.ml_methods.algorithm.refactoring;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.utils.PsiSearchUtil;

import java.util.Optional;

public abstract class Refactoring {
    private final @NotNull SmartPsiElementPointer<PsiElement> entity;

    private final @NotNull SmartPsiElementPointer<PsiElement> target;

    private final String entityName;

    private final String targetName;

    private final double accuracy;

    /**
     * This factory method is a replacement for old ctor. Previously {@link Refactoring} class
     * stored only names of entities that were involved in refactoring. Now {@link Refactoring}
     * class has subclasses for different type of refactorings.
     * Use constructors of {@link MoveMethodRefactoring} and {@link MoveFieldRefactoring} instead.
     */
    @Deprecated
    public static @NotNull Refactoring createRefactoring(
        final @NotNull String entity,
        final @NotNull String target,
        final double accuracy,
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
                (PsiClass) targetElement,
                accuracy
            );
        } else {
            return new MoveFieldRefactoring(
                (PsiField) entityElement,
                (PsiClass) targetElement,
                accuracy
            );
        }
    }

    public Refactoring(
        final @NotNull PsiElement entity,
        final @NotNull PsiElement target,
        double accuracy
    ) {
        this.entity = SmartPointerManager.getInstance(entity.getProject()).createSmartPsiElementPointer(entity);
        this.target = SmartPointerManager.getInstance(entity.getProject()).createSmartPsiElementPointer(target);

        this.entityName = ApplicationManager.getApplication().runReadAction(
            (Computable<String>) () -> PsiSearchUtil.getHumanReadableName(entity)
        );

        this.targetName = ApplicationManager.getApplication().runReadAction(
            (Computable<String>) () -> PsiSearchUtil.getHumanReadableName(target)
        );

        this.accuracy = accuracy;
    }

    public @NotNull Optional<PsiElement> getEntity() {
        return Optional.ofNullable(entity.getElement());
    }

    public @NotNull Optional<PsiElement> getTarget() {
        return Optional.ofNullable(target.getElement());
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
     * Use {@link #getTarget()} instead.
     */
    @Deprecated
    public String getTargetName() {
        return targetName;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public abstract boolean isMoveFieldRefactoring();

    @NotNull
    public abstract <R> R accept(final @NotNull RefactoringVisitor<R> visitor);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Refactoring that = (Refactoring) o;

        return entity.equals(that.entity) && target.equals(that.target);
    }

    @Override
    public int hashCode() {
        int result = entity.hashCode();
        result = 31 * result + target.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Refactoring{" +
                "entity=" + entity +
                ", target=" + target +
                '}';
    }
}
