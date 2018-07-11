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
 * It's just a distance formula taken from old {@link org.ml_methods_group.algorithm.entity.Entity}
 * class.
 */
public class RelevanceBasedDistanceCalculator implements DistanceCalculator {
    private static final @NotNull RelevanceBasedDistanceCalculator INSTANCE =
            new RelevanceBasedDistanceCalculator();

    private RelevanceBasedDistanceCalculator() {}

    public @NotNull RelevanceBasedDistanceCalculator getInstance() {
        return INSTANCE;
    }

    @Override
    public double distance(
        final @NotNull ElementAttributes from,
        final @NotNull ElementAttributes to
    ) {
        double ans = 0.0;
        double w = 0.0;

        if (from.getClass().equals(to.getClass())) {
            for (int i = 0; i < to.getRawFeatures().length; i++) {
                w += square(to.getRawFeatures()[i] + from.getRawFeatures()[i]);
            }
        } else {
            for (double aVector : to.getRawFeatures()) {
                w += square(aVector);
            }

            for (double aVector : from.getRawFeatures()) {
                w += square(aVector);
            }
        }

        ans += w == 0 ? 0 : 1.0 / (w + 1);
        final int rpIntersect = to.getRelevantProperties().sizeOfIntersection(from.getRelevantProperties());
        if (rpIntersect == 0) {
            return Double.POSITIVE_INFINITY;
        }
        ans += (1 - rpIntersect /
                (1.0 * from.getRelevantProperties().sizeOfUnion(to.getRelevantProperties())));

        return Math.sqrt(ans);
    }

    private double square(double value) {
        return value * value;
    }
}
