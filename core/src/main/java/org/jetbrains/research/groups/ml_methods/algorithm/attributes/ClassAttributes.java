package org.jetbrains.research.groups.ml_methods.algorithm.attributes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.ClassEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.CodeEntity;

public class ClassAttributes extends ElementAttributes {
    private final @NotNull ClassEntity classEntity;

    public ClassAttributes(
        final @NotNull ClassEntity classEntity,
        final @NotNull double[] features
    ) {
        super(features);
        this.classEntity = classEntity;
    }

    @Override
    public @NotNull CodeEntity getOriginalEntity() {
        return classEntity;
    }

    public @NotNull ClassEntity getOriginalClass() {
        return classEntity;
    }

    public <R> R accept(final @NotNull ElementAttributesVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
