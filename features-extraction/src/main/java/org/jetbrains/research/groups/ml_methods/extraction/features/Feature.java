package org.jetbrains.research.groups.ml_methods.extraction.features;

import java.io.Serializable;

/**
 * This is just a feature. It has its one single value. All features should extend this class. This
 * will allow to distinguish different features from each other by comparing their classes.
 */
public abstract class Feature implements Serializable {
    private static final long serialVersionUID = 5338050554844387150L;

    private final double value;

    public Feature(final double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
