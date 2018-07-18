package org.jetbrains.research.groups.ml_methods.extraction.features;

/**
 * This is a feature of a move method refactoring. It is similar to
 * {@link AnotherInstancePublicCallTargets} but it count not public methods but all the rest.
 */
public class AnotherInstanceNotPublicCallTargets extends Feature {
    public AnotherInstanceNotPublicCallTargets(int value) {
        super(value);
    }
}
