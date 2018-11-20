package org.jetbrains.research.groups.ml_methods.refactoring;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.MethodUtils;

import java.util.List;

public class JMoveRefactoringTextRepresentation extends RefactoringTextRepresentation {

    public JMoveRefactoringTextRepresentation(String methodPackage, String methodName, List<String> params, String destinationClassQualifiedName) {
        super(methodPackage, methodName, params, destinationClassQualifiedName);
    }

    public JMoveRefactoringTextRepresentation(MoveMethodRefactoring refactoring) {
        super(refactoring);
    }

    @Override
    public boolean isOfGivenMethod(PsiMethod method) {
        String methodsSignature = MethodUtils.calculateSignature(method);
        String methodsSignatureWithoutParams = methodsSignature.split("\\(")[0];
        String refactoringSignature = getMethodsSignature();
        return refactoringSignature.equals(methodsSignature) ||
                refactoringSignature.equals(methodsSignatureWithoutParams);
    }

    @Override
    public boolean isToGivenPsiClass(PsiClass aClass) {
        return getClassQualifiedName().equals(aClass.getQualifiedName());
    }
}