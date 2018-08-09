package org.jetbrains.research.groups.ml_methods.refactoring.logging;

import org.jetbrains.annotations.NotNull;

public interface RefactoringFeaturesVisitor<R> {
    @NotNull R visit(final @NotNull MoveMethodRefactoringFeatures features);

    @NotNull R visit(final @NotNull MoveFieldRefactoringFeatures features);
}
