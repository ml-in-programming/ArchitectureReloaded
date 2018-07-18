package org.jetbrains.research.groups.ml_methods.extraction.features;

/**
 * This is a feature of a move method refactoring. If method {@code m} is being moved then this
 * feature counts number of different public methods of the another instance (but same class) that
 * {@code m} calls. Another instance means that {@code m} in order to call another method uses not
 * the same instance it is called for, i.e. it doesn't use {@code this}, but obtains object from
 * somewhere else.
 */
public class AnotherInstancePublicCallTargets extends Feature {
    public AnotherInstancePublicCallTargets(double value) {
        super(value);
    }
}
