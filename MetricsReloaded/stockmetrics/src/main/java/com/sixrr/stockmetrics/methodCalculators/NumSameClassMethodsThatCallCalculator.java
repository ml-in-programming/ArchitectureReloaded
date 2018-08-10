package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.PsiClass;
import org.jetbrains.research.groups.ml_methods.utils.PSIUtil;

public class NumSameClassMethodsThatCallCalculator extends AbstractNumMethodsThatCallCalculator {
    public NumSameClassMethodsThatCallCalculator() {
        super((callingMethod, currentMethod) -> {
            PsiClass callingMethodClass = callingMethod.getContainingClass();
            PsiClass currentMethodClass = currentMethod.getContainingClass();

            return currentMethodClass != null && callingMethodClass != null &&
                    (currentMethodClass.equals(callingMethodClass) ||
                            PSIUtil.getAllSupers(currentMethodClass).contains(callingMethodClass));
        });
    }
}
