package org.jetbrains.research.groups.ml_methods.refactoring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;

import java.util.Objects;

/**
 * Represents a {@link MoveToClassRefactoring} that was found by an {@link Algorithm} or a combination of them. The difference is
 * in {@link #accuracy} field which represents algorithm's certainty in this particular refactoring.
 */
public class CalculatedRefactoring {
    private final @NotNull
    MoveToClassRefactoring refactoring;

    private final double accuracy;

    public CalculatedRefactoring(final @NotNull MoveToClassRefactoring refactoring, final double accuracy) {
        this.refactoring = refactoring;

        this.accuracy = accuracy;
    }

    public @NotNull
    MoveToClassRefactoring getRefactoring() {
        return refactoring;
    }

    public double getAccuracy() {
        return accuracy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalculatedRefactoring that = (CalculatedRefactoring) o;
        return Objects.equals(refactoring, that.refactoring);
    }

    @Override
    public int hashCode() {
        return Objects.hash(refactoring);
    }
}
