package org.jetbrains.research.groups.ml_methods.algorithm.attributes;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.CodeEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.RelevantProperties;

import java.util.Objects;

/**
 * Objects of this class contain useful information (attributes) for some {@link PsiElement}.
 * Namely they store array of {@code double} values which represents extracted features and
 * {@link RelevantProperties}. Array of features contains not all available metrics but just some
 * part of them that is required for a particular {@link Algorithm}. It is supposed that for every
 * run of any {@link Algorithm} for each relevant {@link PsiElement} there will be instantiated a
 * new fresh {@link ElementAttributes}.
 */
public abstract class ElementAttributes {
    /**
     * Array of {@code double} values only to improve performance. It is not supposed to be changed.
     */
    private final @NotNull double[] features;

    /** Initializes attributes. */
    public ElementAttributes(final @NotNull double[] features) {
        this.features = features;
    }

    /**
     * Returns entity this attributes derived from.
     */
    public abstract @NotNull CodeEntity getOriginalEntity();

    /**
     * Returns array of features. ATTENTION: this array is not supposed to be changed directly.
     */
    public @NotNull double[] getRawFeatures() {
        return features;
    }

    /**
     * Returns {@link RelevantProperties} for element.
     */
    public @NotNull RelevantProperties getRelevantProperties() {
        return getOriginalEntity().getRelevantProperties();
    }

    public abstract <R> R accept(final @NotNull ElementAttributesVisitor<R> visitor);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementAttributes that = (ElementAttributes) o;
        return Objects.equals(getOriginalEntity(), that.getOriginalEntity());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getOriginalEntity());
    }
}
