package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import com.sixrr.metrics.MetricCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.attributes.ClassAttributes;
import org.jetbrains.research.groups.ml_methods.algorithm.attributes.ElementAttributes;

import java.util.Map;
import java.util.Objects;

public abstract class CodeEntity {
    private final @NotNull
    RelevantProperties relevantProperties;

    public CodeEntity() {
        this.relevantProperties = new RelevantProperties();
    }

    /**
     * Returns identifier of this entity, usually its qualified name. The main reason entity has
     * this method is that in MetricsReloaded plugin metrics computation result is stored in
     * {@link Map} with {@link String} keys not {@link com.intellij.psi.PsiElement}. Therefore
     * one need to have an ability to obtain identifier for entity if he wants to get metrics
     * for this entity.
     */
    public abstract @NotNull String getIdentifier();

    /**
     * Returns {@code true} if move refactoring can be applied to this entity
     * (e.g. it's not a ctor which can't be moved to another class).
     */
    public abstract boolean isMovable();

    public abstract <R> R accept(@NotNull CodeEntityVisitor<R> visitor);

    public abstract @NotNull MetricCategory getMetricCategory();

    public @NotNull RelevantProperties getRelevantProperties() {
        return relevantProperties;
    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();
}
