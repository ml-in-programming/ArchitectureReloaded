package org.jetbrains.research.groups.ml_methods.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.ClassEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.CodeEntity;

public abstract class ClassInnerEntity extends CodeEntity {
    private final @NotNull
    ClassEntity containingClass;

    public ClassInnerEntity(
        final @NotNull ClassEntity containingClass
    ) {
        this.containingClass = containingClass;
    }

    public @NotNull ClassEntity getContainingClass() {
        return containingClass;
    }
}
