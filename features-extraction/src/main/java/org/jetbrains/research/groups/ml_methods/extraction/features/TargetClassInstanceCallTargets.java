package org.jetbrains.research.groups.ml_methods.extraction.features;

/**
 * This is a feature of a move method refactoring. If method {@code m} is being moved to class
 * {@code c} then this feature counts number of different non-static methods of {@code c} that
 * {@code m} calls.
 */
public class TargetClassInstanceCallTargets extends Feature {
    private static final long serialVersionUID = -6942041699726595570L;

    public TargetClassInstanceCallTargets(double value) {
        super(value);
    }
}
