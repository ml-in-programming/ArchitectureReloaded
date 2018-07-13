package org.jetbrains.research.groups.ml_methods.algorithm;

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
