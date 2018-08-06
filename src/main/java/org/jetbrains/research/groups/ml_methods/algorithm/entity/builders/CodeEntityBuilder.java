package org.jetbrains.research.groups.ml_methods.algorithm.entity.builders;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.CodeEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.RelevantProperties;

public abstract class CodeEntityBuilder {
    protected final @NotNull RelevantProperties relevantProperties = new RelevantProperties();

    public @NotNull RelevantProperties getRelevantProperties() {
        return relevantProperties;
    }

    public abstract @NotNull CodeEntity build();
}
