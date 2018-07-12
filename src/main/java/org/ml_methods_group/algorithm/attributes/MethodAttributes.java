package org.ml_methods_group.algorithm.attributes;

import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.entity.CodeEntity;
import org.ml_methods_group.algorithm.entity.MethodEntity;

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
