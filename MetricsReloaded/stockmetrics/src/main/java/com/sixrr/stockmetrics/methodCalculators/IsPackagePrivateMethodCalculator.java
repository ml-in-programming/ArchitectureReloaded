package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.PsiModifier;

public class IsPackagePrivateMethodCalculator extends ExplicitModifierMethodCalculator {
    public IsPackagePrivateMethodCalculator() {
        super(PsiModifier.PACKAGE_LOCAL);
    }
}