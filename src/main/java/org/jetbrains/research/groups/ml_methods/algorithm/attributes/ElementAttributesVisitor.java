package org.jetbrains.research.groups.ml_methods.algorithm.attributes;

import org.jetbrains.annotations.NotNull;

public interface ElementAttributesVisitor<R> {
    R visit(@NotNull ClassAttributes classAttributes);

    R visit(@NotNull MethodAttributes methodAttributes);

    R visit(@NotNull FieldAttributes fieldAttributes);
}
