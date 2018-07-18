package org.jetbrains.research.groups.ml_methods.extraction.features;

/**
 * This is a feature of a move method refactoring. It is similar to
 * {@link SameInstancePublicCallTargets} but it count not public methods but all the rest.
 */
public final class SameInstanceNotPublicCallTargets extends Feature {
    public SameInstanceNotPublicCallTargets(double value) {
        super(value);
    }
}
