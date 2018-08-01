package org.jetbrains.research.groups.ml_methods.algorithm.attributes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.CodeEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.MethodEntity;

public class MethodAttributes extends ElementAttributes {
    private final @NotNull MethodEntity methodEntity;

    public MethodAttributes(
        final @NotNull MethodEntity methodEntity,
        final @NotNull double[] features
    ) {
        super(features);
        this.methodEntity = methodEntity;
    }

    @Override
    public @NotNull CodeEntity getOriginalEntity() {
        return methodEntity;
    }

    public @NotNull MethodEntity getOriginalMethod() {
        return methodEntity;
    }
}
