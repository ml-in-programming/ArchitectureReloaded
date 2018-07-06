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
    private final @NotNull PsiElement unit;

    private final @NotNull PsiElement target;

    private final String unitName;

    private final String targetName;

    private final double accuracy;

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

    public Refactoring(
        final @NotNull PsiElement unit,
        final @NotNull PsiElement target,
        double accuracy
    ) {
        this.unit = unit;
        this.target = target;

        this.unitName = PsiSearchUtil.getHumanReadableName(unit);
        this.targetName = PsiSearchUtil.getHumanReadableName(target);

        this.accuracy = accuracy;
    }

    public @NotNull PsiElement getUnit() {
        return unit;
    }

    public @NotNull PsiElement getTarget() {
        return target;
    }

    /**
     * If you need to identify code entity. Then it is better to identify it directly and not
     * through its name.
     * Use {@link #getUnit()} instead.
     */
    @Deprecated
    public String getUnitName() {
        return unitName;
    }

    /**
     * If you need to identify code entity. Then it is better to identify it directly and not
     * through its name.
     * Use {@link #getTarget()} instead.
     */
    @Deprecated
    public String getTargetName() {
        return targetName;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public abstract boolean isMoveFieldRefactoring();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Refactoring that = (Refactoring) o;

        return unit.equals(that.unit) && target.equals(that.target);
    }

    @Override
    public int hashCode() {
        int result = unit.hashCode();
        result = 31 * result + target.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Refactoring{" +
                "unit=" + unit +
                ", target=" + target +
                '}';
    }
}
