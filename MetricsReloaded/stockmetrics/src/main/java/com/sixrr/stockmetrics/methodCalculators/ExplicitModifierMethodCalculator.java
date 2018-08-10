
package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import org.jetbrains.annotations.NotNull;

public abstract class ExplicitModifierMethodCalculator extends MethodCalculator {
    private final @NotNull String modifier;

    public ExplicitModifierMethodCalculator(final @PsiModifier.ModifierConstant @NotNull String modifier) {
        this.modifier = modifier;
    }

    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitMethod(PsiMethod method) {
            super.visitMethod(method);
            postMetric(
                method,
                method.getModifierList().hasExplicitModifier(modifier) ? 1. : 0.
            );
        }
    }
}