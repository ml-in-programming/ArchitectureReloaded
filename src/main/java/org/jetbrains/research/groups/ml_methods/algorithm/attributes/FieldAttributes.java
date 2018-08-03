package org.jetbrains.research.groups.ml_methods.algorithm.attributes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.CodeEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.FieldEntity;

public class FieldAttributes extends ElementAttributes {
    private final @NotNull FieldEntity fieldEntity;

    private final @NotNull ClassAttributes containingClassAttributes;

    public FieldAttributes(
        final @NotNull FieldEntity fieldEntity,
        final @NotNull double[] features,
        final @NotNull ClassAttributes containingClassAttributes
    ) {
        super(features);
        this.fieldEntity = fieldEntity;
        this.containingClassAttributes = containingClassAttributes;
    }

    @Override
    public @NotNull CodeEntity getOriginalEntity() {
        return fieldEntity;
    }

    public @NotNull FieldEntity getOriginalField() {
        return fieldEntity;
    }

    public @NotNull ClassAttributes getContainingClassAttributes() {
        return containingClassAttributes;
    }

    public <R> R accept(final @NotNull ElementAttributesVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
