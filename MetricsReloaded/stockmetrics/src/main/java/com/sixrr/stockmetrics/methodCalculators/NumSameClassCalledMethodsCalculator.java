package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.PsiClass;
import org.jetbrains.research.groups.ml_methods.utils.PSIUtil;

public class NumSameClassCalledMethodsCalculator extends AbstractNumCalledMethodsCalculator {
    public NumSameClassCalledMethodsCalculator() {
        super((calledMethod, currentMethod) -> {
            PsiClass calledMethodClass = calledMethod.getContainingClass();
            PsiClass currentMethodClass = currentMethod.getContainingClass();

            return currentMethodClass != null && calledMethodClass != null &&
                (currentMethodClass.equals(calledMethodClass) ||
                 PSIUtil.getAllSupers(currentMethodClass).contains(calledMethodClass));
        });
    }
}
