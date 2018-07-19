package org.jetbrains.research.groups.ml_methods.extraction.features;

/**
 * This is just a feature. It has its one single value. All features should extend this class. This
 * will allow to distinguish different features from each other by comparing their classes.
 */
public abstract class Feature {
    private final int value;

    public Feature(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
