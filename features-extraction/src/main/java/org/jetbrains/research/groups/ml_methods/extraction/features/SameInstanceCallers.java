package org.jetbrains.research.groups.ml_methods.extraction.features;

/**
 * This is a feature of a move method refactoring. If method {@code m} is being moved then this
 * feature counts number of different methods of the same instance that invoke {@code m}. Same
 * instance means that these methods in order to call {@code m} use the same instance they are
 * called for, i.e. they use {@code this}.
 */
public class SameInstanceCallers extends Feature {
    private static final long serialVersionUID = -7006989490422320287L;

    public SameInstanceCallers(double value) {
        super(value);
    }
}
