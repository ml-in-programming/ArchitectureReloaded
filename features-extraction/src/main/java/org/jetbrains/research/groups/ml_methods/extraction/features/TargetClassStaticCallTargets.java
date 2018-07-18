package org.jetbrains.research.groups.ml_methods.extraction.features;

/**
 * This is a feature of a move method refactoring. It is similar to
 * {@link TargetClassInstanceCallTargets} but it counts static methods.
 */
public class TargetClassStaticCallTargets extends Feature {
    private static final long serialVersionUID = -2102601113399775811L;

    public TargetClassStaticCallTargets(double value) {
        super(value);
    }
}
