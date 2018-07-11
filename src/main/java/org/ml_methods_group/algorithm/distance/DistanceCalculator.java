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

package org.ml_methods_group.algorithm.distance;

import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.attributes.ElementAttributes;

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
