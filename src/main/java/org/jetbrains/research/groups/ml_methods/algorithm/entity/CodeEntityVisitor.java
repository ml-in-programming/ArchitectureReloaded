package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import org.jetbrains.annotations.NotNull;

public interface CodeEntityVisitor<R> {
    R visit(@NotNull ClassEntity classEntity);

    R visit(@NotNull MethodEntity methodEntity);

    R visit(@NotNull FieldEntity fieldEntity);
}
