package org.jetbrains.research.groups.ml_methods.algorithm.attributes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.ClassInnerEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.CodeEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.FieldEntity;

public class FieldAttributes extends ClassInnerEntityAttributes {
    private final @NotNull FieldEntity fieldEntity;

    public FieldAttributes(
        final @NotNull FieldEntity fieldEntity,
        final @NotNull double[] features,
        final @NotNull ClassAttributes containingClassAttributes
    ) {
        super(features, containingClassAttributes);
        this.fieldEntity = fieldEntity;
    }

    @Override
    public @NotNull CodeEntity getOriginalEntity() {
        return fieldEntity;
    }

    public @NotNull FieldEntity getOriginalField() {
        return fieldEntity;
    }

    @Override
    public @NotNull ClassInnerEntity getClassInnerEntity() {
        return fieldEntity;
    }

    public <R> R accept(final @NotNull ElementAttributesVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
