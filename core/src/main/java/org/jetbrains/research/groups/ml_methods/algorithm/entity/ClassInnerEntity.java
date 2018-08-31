package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import org.jetbrains.annotations.NotNull;

public abstract class ClassInnerEntity extends CodeEntity {
    private final @NotNull ClassEntity containingClass;

    public ClassInnerEntity(
        final @NotNull ClassEntity containingClass
    ) {
        this.containingClass = containingClass;
    }

    public @NotNull ClassEntity getContainingClass() {
        return containingClass;
    }
}
