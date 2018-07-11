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

package org.ml_methods_group.algorithm.entity;

import com.sixrr.metrics.MetricCategory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class CodeEntity {
    private final @NotNull
    RelevantProperties relevantProperties;

    public CodeEntity(final @NotNull RelevantProperties relevantProperties) {
        this.relevantProperties = relevantProperties;
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

    /**
     * Returns a name of containing class (for a method for example). If this entity is already a
     * class then name of this entity is returned.
     *
     * It appears that some algorithms use name of containing class to identify this class.
     * Obviously it's better to use
     * {@link org.ml_methods_group.algorithm.attributes.ClassAttributes} for this purpose. But in
     * order to do so we need guarantee that enclosing class is in {@link EntitiesStorage} that
     * {@link EntitySearcher} produced. If there is such a guarantee then this method should be
     * deprecated and replaced with equivalent in
     * {@link org.ml_methods_group.algorithm.attributes.ElementAttributes} that returns
     * {@link org.ml_methods_group.algorithm.attributes.ClassAttributes}.
     */
    public abstract @NotNull String getContainingClassName();

    public abstract @NotNull MetricCategory getMetricCategory();

    public @NotNull RelevantProperties getRelevantProperties() {
        return relevantProperties;
    }
}
