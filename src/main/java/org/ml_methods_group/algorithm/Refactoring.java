/*
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
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

package org.ml_methods_group.algorithm;

import org.jetbrains.annotations.NotNull;

public class Refactoring {
    private final String unit;
    private final String target;
    private final double accuracy;

    public Refactoring(@NotNull String unit, @NotNull String target, double accuracy) {
        this.unit = unit;
        this.target = target;
        this.accuracy = accuracy;
    }

    public String getUnit() {
        return unit;
    }

    public String getTarget() {
        return target;
    }

    public double getAccuracy() {
        return accuracy;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Refactoring) {
            Refactoring other = ((Refactoring) obj);
            return unit.equals(other.unit) && target.equals(other.target);
        }
        return false;
    }
}
