package org.ml_methods_group.algorithm.attributes;

import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.entity.CodeEntity;
import org.ml_methods_group.algorithm.entity.FieldEntity;

public class FieldAttributes extends ElementAttributes {
    private final @NotNull FieldEntity fieldEntity;

    public FieldAttributes(
        final @NotNull FieldEntity fieldEntity,
        final @NotNull double[] features
    ) {
        super(features);
        this.fieldEntity = fieldEntity;
    }

    @Override
    public @NotNull CodeEntity getOriginalEntity() {
        return fieldEntity;
    }

    public @NotNull FieldEntity getOriginalField() {
        return fieldEntity;
    }
}