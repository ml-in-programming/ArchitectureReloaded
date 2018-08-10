package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.PsiModifier;

public class IsSynchronizedMethodCalculator extends ExplicitModifierMethodCalculator {
    public IsSynchronizedMethodCalculator() {
        super(PsiModifier.SYNCHRONIZED);
    }
}