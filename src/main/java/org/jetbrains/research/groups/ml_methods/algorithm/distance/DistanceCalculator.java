package org.jetbrains.research.groups.ml_methods.algorithm.distance;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.attributes.ElementAttributes;

/**
 * An interface for a class that can calculate distance between to entities based on their
 * {@link ElementAttributes}.
 */
public interface DistanceCalculator {
    /**
     * Calculates distance between two set of attributes.
     *
     * @param from left-hand side argument or initial point.
     * @param to right-hand side argument or destination point.
     * @return real-valued distance.
     */
    double distance(@NotNull ElementAttributes from, @NotNull ElementAttributes to);
}
