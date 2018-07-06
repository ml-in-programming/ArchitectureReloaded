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

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.utils.PsiSearchUtil;

public abstract class Refactoring {
    private final String unit;
    private final String target;
    private final double accuracy;
    private final boolean isUnitField;

    /**
     * This factory method is a replacement for old ctor. Previously {@link Refactoring} class
     * stored only names of entities that were involved in refactoring. Now {@link Refactoring}
     * class has subclasses for different type of refactorings.
     * Use constructors of {@link MoveMethodRefactoring} and {@link MoveFieldRefactoring} instead.
     */
    @Deprecated
    public static @NotNull Refactoring createRefactoring(
        final @NotNull String unit,
        final @NotNull String target,
        final double accuracy,
        final boolean isUnitField,
        final @NotNull AnalysisScope scope
    ) {
        String exceptionMessage =
            "Unable to find PsiElement with given name during Refactoring creation";

        PsiElement unitElement =
            PsiSearchUtil.findElement(unit, scope)
                         .orElseThrow(() -> new IllegalArgumentException(exceptionMessage));

        PsiElement targetElement =
            PsiSearchUtil.findElement(target, scope)
                         .orElseThrow(() -> new IllegalArgumentException(exceptionMessage));

        if (!isUnitField) {
            return new MoveMethodRefactoring(
                (PsiMethod) unitElement,
                (PsiClass) targetElement,
                accuracy
            );
        } else {
            return new MoveFieldRefactoring(
                (PsiField) unitElement,
                (PsiClass) targetElement,
                accuracy
            );
        }
    }

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
    public int hashCode() {
        return unit.hashCode() + target.hashCode();
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

    @Override
    public String toString() {
        return "unit = " + unit +
                ", target = " + target +
                ", accuracy = " + accuracy;
    }
}
