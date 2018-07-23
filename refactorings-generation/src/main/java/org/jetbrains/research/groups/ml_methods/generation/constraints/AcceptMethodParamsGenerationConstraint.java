package org.jetbrains.research.groups.ml_methods.generation.constraints;

import com.intellij.psi.*;
import com.sixrr.metrics.utils.ClassUtils;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

public class AcceptMethodParamsGenerationConstraint implements GenerationConstraint {
    @Override
    public boolean acceptTargetClass(@NotNull PsiClass aClass) {
        return !(ClassUtils.isAnonymous(aClass) || aClass.getQualifiedName() == null
                || aClass.isEnum() || aClass.isInterface());
    }

    @Override
    public boolean acceptMethod(@NotNull PsiMethod method) {
        if (method.isConstructor() || MethodUtils.isAbstract(method)) {
            return false;
        }
        final PsiClass containingClass = method.getContainingClass();
        return !(containingClass == null || containingClass.isInterface());
    }

    @Override
    public boolean acceptRefactoring(@NotNull PsiMethod method, @NotNull PsiClass aClass) {
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
