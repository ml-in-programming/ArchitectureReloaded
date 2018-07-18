package org.jetbrains.research.groups.ml_methods.extraction.features;

/**
 * This is a feature of a move method refactoring. It is similar to
 * {@link SameClassStaticPublicCallTargets} but it count not public methods but all the rest.
 */
public class SameClassStaticNotPublicCallTargets extends Feature {
    public SameClassStaticNotPublicCallTargets(double value) {
        super(value);
    }
}
