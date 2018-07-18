package org.jetbrains.research.groups.ml_methods.extraction.features.vector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.Feature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Object of this class stores several features gathered together to form a feature vector.
 */
public class FeatureVector implements Serializable {
    private static final long serialVersionUID = -3766204412535951898L;

    private final @NotNull List<Feature> components;

    public FeatureVector(final @NotNull Collection<Feature> components) {
        this.components = new ArrayList<>(components);
    }
}
