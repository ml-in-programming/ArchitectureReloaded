package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.PsiModifier;

public class IsProtectedMethodCalculator extends ExplicitModifierMethodCalculator {
    public IsProtectedMethodCalculator() {
        super(PsiModifier.PROTECTED);
    }
}
