package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.PsiModifier;

public class IsPublicMethodCalculator extends ExplicitModifierMethodCalculator {
    public IsPublicMethodCalculator() {
        super(PsiModifier.PUBLIC);
    }
}