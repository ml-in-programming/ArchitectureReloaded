package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.PsiModifier;

public class IsStaticMethodCalculator extends ExplicitModifierMethodCalculator {
    public IsStaticMethodCalculator() {
        super(PsiModifier.STATIC);
    }
}