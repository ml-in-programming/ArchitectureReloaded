package org.ml_methods_group.algorithm.refactoring;

import org.jetbrains.annotations.NotNull;

interface RefactoringVisitor<R> {
    @NotNull R visit(final @NotNull MoveMethodRefactoring refactoring);

    @NotNull R visit(final @NotNull MoveFieldRefactoring refactoring);
}
