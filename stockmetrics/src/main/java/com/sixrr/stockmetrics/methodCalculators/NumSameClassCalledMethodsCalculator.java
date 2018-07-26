package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.PsiClass;

public class NumSameClassCalledMethodsCalculator extends AbstractNumCalledMethodsCalculator {
    public NumSameClassCalledMethodsCalculator() {
        super((calledMethod, currentMethod) -> {
            PsiClass calledMethodClass = calledMethod.getContainingClass();

            return calledMethodClass != null &&
                   calledMethodClass.equals(currentMethod.getContainingClass());
        });
    }
}
