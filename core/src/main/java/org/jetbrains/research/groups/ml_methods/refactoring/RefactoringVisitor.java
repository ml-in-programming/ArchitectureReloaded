package org.jetbrains.research.groups.ml_methods.refactoring;

import org.jetbrains.annotations.NotNull;

public interface RefactoringVisitor<R> {
    @NotNull R visit(final @NotNull MoveMethodRefactoring refactoring);

    @NotNull R visit(final @NotNull MoveFieldRefactoring refactoring);
}
