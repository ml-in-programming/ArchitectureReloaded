package org.ml_methods_group.algorithm.attributes;

import org.jetbrains.annotations.NotNull;

public class NoRequestedMetricException extends Exception {
    public NoRequestedMetricException(final @NotNull String message) {
        super(message);
    }
}
