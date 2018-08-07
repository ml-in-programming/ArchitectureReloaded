package org.jetbrains.research.groups.ml_methods.algorithm.entity.builders;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.CodeEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.RelevantProperties;

public abstract class CodeEntityBuilder {
    public abstract @NotNull CodeEntity build();
}
