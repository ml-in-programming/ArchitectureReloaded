package org.jetbrains.research.groups.ml_methods.extraction.features;

/**
 * This is a feature of a move method refactoring. If method {@code m} is being moved to class
 * {@code c} then this feature counts number of different fields that belong to {@code c} and that
 * {@code m} accesses.
 */
public class TargetClassFieldsAccessed extends Feature {
    private static final long serialVersionUID = 4619505886035772957L;

    public TargetClassFieldsAccessed(double value) {
        super(value);
    }
}
