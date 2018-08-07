package org.jetbrains.research.groups.ml_methods.algorithm.attributes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.ClassInnerEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.CodeEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.MethodEntity;

public class MethodAttributes extends ClassInnerEntityAttributes {
    private final @NotNull MethodEntity methodEntity;

    public MethodAttributes(
        final @NotNull MethodEntity methodEntity,
        final @NotNull double[] features,
        final @NotNull ClassAttributes containingClassAttributes
    ) {
        super(features, containingClassAttributes);
        this.methodEntity = methodEntity;
    }

    @Override
    public @NotNull CodeEntity getOriginalEntity() {
        return methodEntity;
    }

    public @NotNull MethodEntity getOriginalMethod() {
        return methodEntity;
    }

    @Override
    public @NotNull ClassInnerEntity getClassInnerEntity() {
        return methodEntity;
    }

    public <R> R accept(final @NotNull ElementAttributesVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
