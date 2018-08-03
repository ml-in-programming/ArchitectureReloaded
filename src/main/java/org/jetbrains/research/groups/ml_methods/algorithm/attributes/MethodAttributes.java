package org.jetbrains.research.groups.ml_methods.algorithm.attributes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.CodeEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.MethodEntity;

public class MethodAttributes extends ElementAttributes {
    private final @NotNull MethodEntity methodEntity;

    private final @NotNull ClassAttributes containingClassAttributes;

    public MethodAttributes(
        final @NotNull MethodEntity methodEntity,
        final @NotNull double[] features,
        final @NotNull ClassAttributes containingClassAttributes
    ) {
        super(features);
        this.methodEntity = methodEntity;
        this.containingClassAttributes = containingClassAttributes;
    }

    @Override
    public @NotNull CodeEntity getOriginalEntity() {
        return methodEntity;
    }

    public @NotNull MethodEntity getOriginalMethod() {
        return methodEntity;
    }

    public @NotNull ClassAttributes getContainingClassAttributes() {
        return containingClassAttributes;
    }
}
