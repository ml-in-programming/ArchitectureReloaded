package org.jetbrains.research.groups.ml_methods.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.ClassEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.FieldEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.MethodEntity;

public interface CodeEntityVisitor<R> {
    R visit(@NotNull ClassEntity classEntity);

    R visit(@NotNull MethodEntity methodEntity);

    R visit(@NotNull FieldEntity fieldEntity);
}
