
package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.PsiModifier;

public class IsPrivateMethodCalculator extends ExplicitModifierMethodCalculator {
    public IsPrivateMethodCalculator() {
        super(PsiModifier.PRIVATE);
    }
}