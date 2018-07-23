package org.jetbrains.research.groups.ml_methods.generation.constraints;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.ClassUtils;
import com.sixrr.metrics.utils.MethodUtils;

public class AcceptRelevantPropertiesGenerationConstraint implements GenerationConstraint {
    @Override
    public boolean acceptTargetClass(PsiClass aClass) {
        return !(ClassUtils.isAnonymous(aClass) || aClass.getQualifiedName() == null
                || aClass.isEnum() || aClass.isInterface());
    }

    @Override
    public boolean acceptMethod(PsiMethod method) {
        if (method.isConstructor() || MethodUtils.isAbstract(method)) {
            return false;
        }
        final PsiClass containingClass = method.getContainingClass();
        return !(containingClass == null || containingClass.isInterface());
    }

    @Override
    public boolean acceptRefactoring(PsiMethod method, PsiClass aClass) {
        return method.getContainingClass() != null && !method.getContainingClass().equals(aClass);
    }

    @Override
    public boolean acceptField(PsiField field) {
        return field.getContainingClass() != null;
    }
}
