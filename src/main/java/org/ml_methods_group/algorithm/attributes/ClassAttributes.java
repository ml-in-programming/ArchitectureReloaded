package org.ml_methods_group.algorithm.attributes;

import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.CodeEntity;

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
}
