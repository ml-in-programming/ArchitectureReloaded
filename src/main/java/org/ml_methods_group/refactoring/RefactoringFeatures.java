package org.ml_methods_group.refactoring;

import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.refactoring.Refactoring;

/**
 * This class contains features extracted from some {@link Refactoring}. This features should
 * characterize {@link Refactoring} in a way that helps to extract user refactorings preferences
 * from features of accepted and rejected refactorings.
 */
public class RefactoringFeatures {
    private final boolean isFieldMove;

    /**
     * Extracts features from a given {@link Refactoring}.
     *
     * @param refactoring a {@link Refactoring} to extract features from.
     */
    public RefactoringFeatures(final @NotNull Refactoring refactoring) {
        isFieldMove = refactoring.isMoveFieldRefactoring();
    }

    public boolean isFieldMove() {
        return isFieldMove;
    }
}
