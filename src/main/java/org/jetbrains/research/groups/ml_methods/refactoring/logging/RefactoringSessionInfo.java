package org.jetbrains.research.groups.ml_methods.refactoring.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveToClassRefactoring;

import java.util.ArrayList;
import java.util.List;

/**
 * Objects of this class contain information about one user interaction with the plugin.
 * Refactorings are suggested to user and he accepts some of them and rejects the other.
 * Information about what refactorings were accepted and what refactorings were rejected is
 * stored in objects of this class. Each {@link MoveToClassRefactoring} is represented by
 * {@link RefactoringFeatures} derived from it. All data from this class is intended to be written
 * to log for further analysis.
 */
public final class RefactoringSessionInfo {
    private final @NotNull List<RefactoringFeatures> uncheckedRefactoringsFeatures;

    private final @NotNull List<RefactoringFeatures> rejectedRefactoringsFeatures;

    private final @NotNull List<RefactoringFeatures> appliedRefactoringsFeatures;

    /**
     * Creates session info for given accepted and rejected refactorings.
     *
     * @param uncheckedRefactoringsFeatures refactorings that were accepted.
     * @param rejectedRefactoringsFeatures refactorings that were rejected.
     * @param appliedRefactoringsFeatures refactorings that were applied.
     */
    public RefactoringSessionInfo(
        final @NotNull List<RefactoringFeatures> uncheckedRefactoringsFeatures,
        final @NotNull List<RefactoringFeatures> rejectedRefactoringsFeatures,
        final @NotNull List<RefactoringFeatures> appliedRefactoringsFeatures
    ) {
        this.uncheckedRefactoringsFeatures = uncheckedRefactoringsFeatures;
        this.rejectedRefactoringsFeatures = rejectedRefactoringsFeatures;
        this.appliedRefactoringsFeatures = appliedRefactoringsFeatures;
    }

    /**
     * Returns features of all unchecked refactorings.
     */
    public @NotNull List<RefactoringFeatures> getUncheckedRefactoringsFeatures() {
        return new ArrayList<>(uncheckedRefactoringsFeatures);
    }

    /**
     * Returns features of all rejected refactorings.
     */
    public @NotNull List<RefactoringFeatures> getRejectedRefactoringsFeatures() {
        return new ArrayList<>(rejectedRefactoringsFeatures);
    }

    /**
     * Returns features of all applied refactorings.
     */
    public @NotNull List<RefactoringFeatures> getAppliedRefactoringsFeatures() {
        return new ArrayList<>(appliedRefactoringsFeatures);
    }
}
