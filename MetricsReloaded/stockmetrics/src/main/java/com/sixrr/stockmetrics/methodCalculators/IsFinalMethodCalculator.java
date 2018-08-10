package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.PsiModifier;

public class IsFinalMethodCalculator extends ExplicitModifierMethodCalculator {
    public IsFinalMethodCalculator() {
        super(PsiModifier.FINAL);
    }
}