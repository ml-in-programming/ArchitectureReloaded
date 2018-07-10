/*
 * Copyright 2018 Machine Learning Methods in Software Engineering Group of JetBrains Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ml_methods_group.algorithm.attributes;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.AbstractAlgorithm;
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

    private final @NotNull RelevantProperties relevantProperties;

    /**
     * Initializes attributes.
     */
    public ElementAttributes(
        final @NotNull double[] features,
        final @NotNull RelevantProperties relevantProperties
    ) {
        this.features = features;
        this.relevantProperties = relevantProperties;
    }

    /**
     * Returns element this attributes derived from.
     */
    public abstract @NotNull PsiElement getOriginalElement();

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
        return relevantProperties;
    }
}
