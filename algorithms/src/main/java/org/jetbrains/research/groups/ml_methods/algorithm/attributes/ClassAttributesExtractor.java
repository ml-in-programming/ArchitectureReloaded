package org.jetbrains.research.groups.ml_methods.algorithm.attributes;

import org.jetbrains.annotations.NotNull;

/**
 * Returns {@link ClassAttributes} for given {@link ElementAttributes}. If given argument is a {@link ClassAttributes}
 * then it is returned. Otherwise containing class of given element is returned.
 */
public class ClassAttributesExtractor implements ElementAttributesVisitor<ClassAttributes> {
    private static final @NotNull ClassAttributesExtractor INSTANCE = new ClassAttributesExtractor();

    private ClassAttributesExtractor() {}

    public static @NotNull ClassAttributesExtractor getInstance() {
        return INSTANCE;
    }

    @Override
    public ClassAttributes visit(final @NotNull ClassAttributes classAttributes) {
        return classAttributes;
    }

    @Override
    public ClassAttributes visit(final @NotNull MethodAttributes methodAttributes) {
        return methodAttributes.getContainingClassAttributes();
    }

    @Override
    public ClassAttributes visit(final @NotNull FieldAttributes fieldAttributes) {
        return fieldAttributes.getContainingClassAttributes();
    }
}
