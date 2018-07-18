package org.jetbrains.research.groups.ml_methods.extraction.features;

/**
 * This is a feature of a move method refactoring. If method {@code m} is being moved then this
 * feature counts number of different methods of the another instance (but same class) that invoke
 * {@code m}. Another instance means that these methods in order to call {@code m} use not the same
 * instance they are called for, i.e. they don't use {@code this}, but obtains object from
 * somewhere else.
 */
public class AnotherInstanceCallers extends Feature {
    public AnotherInstanceCallers(int value) {
        super(value);
    }
}
