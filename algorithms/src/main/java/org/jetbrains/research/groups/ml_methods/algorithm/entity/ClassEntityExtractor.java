package org.jetbrains.research.groups.ml_methods.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.ClassEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.CodeEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.FieldEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.MethodEntity;

/**
 * Returns {@link ClassEntity} for given {@link CodeEntity}. If given argument is a {@link ClassEntity}
 * then it is returned. Otherwise containing class of given element is returned.
 */
public class ClassEntityExtractor implements CodeEntityVisitor<ClassEntity> {
    private static final @NotNull ClassEntityExtractor INSTANCE = new ClassEntityExtractor();

    private ClassEntityExtractor() {}

    public static @NotNull ClassEntityExtractor getInstance() {
        return INSTANCE;
    }

    @Override
    public ClassEntity visit(@NotNull ClassEntity classEntity) {
        return classEntity;
    }

    @Override
    public ClassEntity visit(@NotNull MethodEntity methodEntity) {
        return methodEntity.getContainingClass();
    }

    @Override
    public ClassEntity visit(@NotNull FieldEntity fieldEntity) {
        return fieldEntity.getContainingClass();
    }
}
