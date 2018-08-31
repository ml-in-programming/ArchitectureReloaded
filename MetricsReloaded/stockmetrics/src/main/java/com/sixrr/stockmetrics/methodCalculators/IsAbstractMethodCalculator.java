
package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.PsiModifier;

public class IsAbstractMethodCalculator extends ExplicitModifierMethodCalculator {
    public IsAbstractMethodCalculator() {
        super(PsiModifier.ABSTRACT);
    }
}