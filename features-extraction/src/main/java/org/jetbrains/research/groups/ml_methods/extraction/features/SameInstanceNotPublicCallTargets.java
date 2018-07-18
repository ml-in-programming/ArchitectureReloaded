package org.jetbrains.research.groups.ml_methods.extraction.features;

/**
 * This is a feature of a move method refactoring. It is similar to
 * {@link SameInstancePublicCallTargets} but it count not public methods but all the rest.
 */
public final class SameInstanceNotPublicCallTargets extends Feature {
    private static final long serialVersionUID = 2547853590858010128L;

    public SameInstanceNotPublicCallTargets(double value) {
        super(value);
    }
}
