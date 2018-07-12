package org.ml_methods_group.algorithm.attributes;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.AbstractAlgorithm;
import org.ml_methods_group.algorithm.entity.CodeEntity;
import org.ml_methods_group.algorithm.entity.RelevantProperties;

/**
 * Objects of this class contain useful information (attributes) for some {@link PsiElement}.
 * Namely they store array of {@code double} values which represents extracted features and
 * {@link RelevantProperties}. Array of features contains not all available metrics but just some
 * part of them that is required for a particular {@link AbstractAlgorithm}. It is supposed that for every
 * run of any {@link AbstractAlgorithm} for each relevant {@link PsiElement} there will be instantiated a
 * new fresh {@link ElementAttributes}.
 */
public abstract class ElementAttributes {
    /**
     * Array of {@code double} values only to improve performance. It is not supposed to be changed.
     */
    private final @NotNull double[] features;

    /**
     * Initializes attributes.
     */
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
}
