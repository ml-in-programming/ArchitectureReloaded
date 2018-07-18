package org.jetbrains.research.groups.ml_methods.extraction.features;

/**
 * This is a feature of a move method refactoring. If method {@code m} is being moved then this
 * feature counts number of different static public methods that belong to the same class as
 * {@code m} and that {@code m} calls.
 */
public class SameClassStaticPublicCallTargets extends Feature {
    public SameClassStaticPublicCallTargets(double value) {
        super(value);
    }
}
