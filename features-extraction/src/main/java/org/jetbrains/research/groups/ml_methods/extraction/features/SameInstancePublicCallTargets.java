package org.jetbrains.research.groups.ml_methods.extraction.features;

/**
 * This is a feature of a move method refactoring. If method {@code m} is being moved then this
 * feature counts number of different public methods of the same instance that {@code m} calls. Same
 * instance means that {@code m} in order to call another method uses the same instance it is called
 * for, i.e. it uses {@code this}.
 */
public final class SameInstancePublicCallTargets extends Feature {
    private static final long serialVersionUID = -2217592415612029756L;

    public SameInstancePublicCallTargets(double value) {
        super(value);
    }
}
