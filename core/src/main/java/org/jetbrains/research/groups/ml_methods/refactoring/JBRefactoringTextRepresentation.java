package org.jetbrains.research.groups.ml_methods.refactoring;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JBRefactoringTextRepresentation extends RefactoringTextRepresentation {
    public JBRefactoringTextRepresentation(String sourceClassQualifiedName, String methodName,
                                           List<String> params, String destinationClassQualifiedName) {
        super(sourceClassQualifiedName, methodName, params, destinationClassQualifiedName);
    }

    public JBRefactoringTextRepresentation(MoveMethodRefactoring refactoring) {
        super(refactoring);
    }

    @Override
    public boolean isOfGivenMethod(PsiMethod method) {
        List<String> methodsParams = Arrays.stream(method.getParameterList().getParameters()).
                map(psiParameter -> {
                    if (!psiParameter.getType().isValid()) {
                        throw new IllegalStateException("Type of method param isn't valid. " +
                                "Could not decide if given method equals to textual representation: " + getMethodsSignature());
                    }
                    return psiParameter.getType().getCanonicalText();
                }).
                collect(Collectors.toList());
        return method.getContainingClass() != null &&
                getMethodName().equals(method.getName()) &&
                getSourceClassQualifiedName().equals(method.getContainingClass().getQualifiedName()) &&
                getParamsClasses().equals(methodsParams);
    }

    @Override
    public boolean isToGivenPsiClass(PsiClass aClass) {
        return getClassQualifiedName().equals(aClass.getQualifiedName());
    }
}
