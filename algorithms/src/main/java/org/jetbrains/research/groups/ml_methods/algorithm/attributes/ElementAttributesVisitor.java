package org.jetbrains.research.groups.ml_methods.attributes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.attributes.ClassAttributes;
import org.jetbrains.research.groups.ml_methods.algorithm.attributes.FieldAttributes;
import org.jetbrains.research.groups.ml_methods.algorithm.attributes.MethodAttributes;

public interface ElementAttributesVisitor<R> {
    R visit(@NotNull ClassAttributes classAttributes);

    R visit(@NotNull MethodAttributes methodAttributes);

    R visit(@NotNull FieldAttributes fieldAttributes);
}
