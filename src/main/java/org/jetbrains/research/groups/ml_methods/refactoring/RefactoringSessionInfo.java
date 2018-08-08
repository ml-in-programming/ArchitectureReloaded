package org.jetbrains.research.groups.ml_methods.refactoring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.MoveToClassRefactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Objects of this class contain information about one user interaction with the plugin.
 * Refactorings are suggested to user and he accepts some of them and rejects the other.
 * Information about what refactorings were accepted and what refactorings were rejected is
 * stored in objects of this class. Each {@link MoveToClassRefactoring} is represented by
 * {@link RefactoringFeatures} derived from it. All data from this class is intended to be written
 * to log for further analysis.
 */
public final class RefactoringSessionInfo {
    private final @NotNull List<RefactoringFeatures> acceptedRefactoringsFeatures;

    private final @NotNull List<RefactoringFeatures> rejectedRefactoringsFeatures;

    /**
     * Creates session info for given accepted and rejected refactorings.
     *
     * @param acceptedRefactorings refactorings that were accepted.
     * @param rejectedRefactorings refactorings that were rejected.
     */
    public RefactoringSessionInfo(
        final @NotNull List<MoveToClassRefactoring> acceptedRefactorings,
        final @NotNull List<MoveToClassRefactoring> rejectedRefactorings
    ) {
        acceptedRefactoringsFeatures =
            acceptedRefactorings.stream()
                                .map(RefactoringFeatures::new)
                                .collect(Collectors.toList());

        rejectedRefactoringsFeatures =
                rejectedRefactorings.stream()
                        .map(RefactoringFeatures::new)
                        .collect(Collectors.toList());
    }

    /**
     * Returns features of all accepted refactorings.
     */
    @NotNull
    public List<RefactoringFeatures> getAcceptedRefactoringsFeatures() {
        return new ArrayList<>(acceptedRefactoringsFeatures);
    }

    /**
     * Returns features of all rejected refactorings.
     */
    @NotNull
    public List<RefactoringFeatures> getRejectedRefactoringsFeatures() {
        return new ArrayList<>(rejectedRefactoringsFeatures);
    }
}
