package org.jetbrains.research.groups.ml_methods.extraction.features;

/**
 * This is a feature of a move method refactoring. If method {@code m} is being moved to class
 * {@code c} then this feature counts number of different methods of {@code c} that invoke
 * {@code m}.
 */
public class TargetClassCallers extends Feature {
    private static final long serialVersionUID = -999232955576934448L;

    public TargetClassCallers(double value) {
        super(value);
    }
}
