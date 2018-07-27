package com.sixrr.stockmetrics.classCalculators;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.sixrr.metrics.utils.ClassUtils;

public class IsAbstractCalculator extends ClassCalculator {

    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitClass(PsiClass aClass) {
            postMetric(aClass, ClassUtils.isAbstract(aClass) ? 1 : 0);
            super.visitClass(aClass);
        }
    }
}
