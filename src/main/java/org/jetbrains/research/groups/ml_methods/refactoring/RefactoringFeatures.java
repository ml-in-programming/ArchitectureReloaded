package org.jetbrains.research.groups.ml_methods.refactoring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.MoveToClassRefactoring;

/**
 * This class contains features extracted from some {@link MoveToClassRefactoring}. This features should
 * characterize {@link MoveToClassRefactoring} in a way that helps to extract user refactorings preferences
 * from features of accepted and rejected refactorings.
 */
public class RefactoringFeatures {
    private final boolean isFieldMove;

    /**
     * Extracts features from a given {@link MoveToClassRefactoring}.
     *
     * @param refactoring a {@link MoveToClassRefactoring} to extract features from.
     */
    public RefactoringFeatures(final @NotNull MoveToClassRefactoring refactoring) {
        isFieldMove = refactoring.isMoveFieldRefactoring();
    }

    public boolean isFieldMove() {
        return isFieldMove;
    }
}
