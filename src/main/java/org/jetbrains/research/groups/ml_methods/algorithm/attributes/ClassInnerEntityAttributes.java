package org.jetbrains.research.groups.ml_methods.algorithm.attributes;

import org.jetbrains.annotations.NotNull;

public abstract class ClassInnerEntityAttributes extends ElementAttributes {
    private final @NotNull ClassAttributes containingClassAttributes;

    public ClassInnerEntityAttributes(
        final @NotNull double[] features,
        final @NotNull ClassAttributes containingClassAttributes
    ) {
        super(features);
        this.containingClassAttributes = containingClassAttributes;
    }

    public @NotNull ClassAttributes getContainingClassAttributes() {
        return containingClassAttributes;
    }
}
