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
    private final boolean isUnitField;

    public Refactoring(@NotNull String unit, @NotNull String target, double accuracy, boolean isUnitField) {
        this.unit = unit;
        this.target = target;
        this.accuracy = accuracy;
        this.isUnitField = isUnitField;
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

    public boolean isUnitField() {
        return isUnitField;
    }

    @Override
    public String toString() {
        return "unit = " + unit +
                ", target = " + target +
                ", accuracy = " + accuracy;
    }
}
