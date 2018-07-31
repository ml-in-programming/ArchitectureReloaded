package org.jetbrains.research.groups.ml_methods.generation.constraints;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.*;
import com.sixrr.metrics.utils.ClassUtils;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AcceptMethodParamsGenerationConstraint extends BasicGenerationConstraint {
    @Override
    public boolean acceptTargetClass(@NotNull PsiClass aClass) {
        return super.acceptTargetClass(aClass) && !(ClassUtils.isAnonymous(aClass) || aClass.isEnum() || aClass.isInterface());
    }

    @Override
    public boolean acceptMethod(@NotNull PsiMethod method, AnalysisScope scope) {
        final PsiClass containingClass = method.getContainingClass();
        return super.acceptMethod(method, scope) &&
                !method.isConstructor() &&
                !MethodUtils.isAbstract(method) &&
                !Objects.requireNonNull(containingClass).isInterface();
    }

    @Override
    public boolean acceptRefactoring(@NotNull PsiMethod method, @NotNull PsiClass aClass) {
        if (!super.acceptRefactoring(method, aClass)) {
            return false;
        }
        for (PsiParameter attribute : method.getParameterList().getParameters()) {
            PsiType attributeType = attribute.getType();
            if (attributeType instanceof PsiClassType) {
                PsiClass parameterClass = ((PsiClassType) attributeType).resolve();
                if (aClass.equals(parameterClass)) {
                    return true;
                }
            }
        }
        return false;
    }
}
