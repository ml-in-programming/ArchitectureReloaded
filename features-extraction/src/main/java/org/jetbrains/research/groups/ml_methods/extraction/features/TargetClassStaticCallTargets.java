package org.jetbrains.research.groups.ml_methods.extraction.features;

/**
 * This is a feature of a move method refactoring. It is similar to
 * {@link TargetClassInstanceCallTargets} but it counts static methods.
 */
public class TargetClassStaticCallTargets extends Feature {
    public TargetClassStaticCallTargets(double value) {
        super(value);
    }
}
