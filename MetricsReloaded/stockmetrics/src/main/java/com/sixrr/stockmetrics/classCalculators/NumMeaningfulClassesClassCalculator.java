package com.sixrr.stockmetrics.classCalculators;

import com.intellij.psi.*;
import org.jetbrains.research.groups.ml_methods.utils.ClassUtils;

public class NumMeaningfulClassesClassCalculator extends ClassCalculator {
    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);
            postMetric(aClass, ClassUtils.getMeaningfulClasses(aClass).size());
        }
    }
}
