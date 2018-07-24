package org.jetbrains.research.groups.ml_methods.extraction.refactoring;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.MethodUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TextFormRefactoring {
    public String getTargetClassQualifiedName() {
        return targetClassQualifiedName;
    }

    public String getMethodPackageWithClass() {
        return methodPackageWithClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<String> getParamsClasses() {
        return paramsClasses;
    }

    private final String targetClassQualifiedName;
    private final String methodPackageWithClass;
    private final String methodName;
    private final List<String> paramsClasses;

    public TextFormRefactoring(String methodPackage, String methodName,
                               List<String> params, String destinationClassQualifiedName) {
        this.methodPackageWithClass = methodPackage;
        this.methodName = methodName;
        this.paramsClasses = params;
        this.targetClassQualifiedName = destinationClassQualifiedName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetClassQualifiedName, methodPackageWithClass, methodName, paramsClasses);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextFormRefactoring that = (TextFormRefactoring) o;
        return Objects.equals(targetClassQualifiedName, that.targetClassQualifiedName) &&
                Objects.equals(methodPackageWithClass, that.methodPackageWithClass) &&
                Objects.equals(methodName, that.methodName) &&
                Objects.equals(paramsClasses, that.paramsClasses);
    }

    String getMethodsSignature() {
        StringBuilder methodsSignature = new StringBuilder();
        methodsSignature.append(methodPackageWithClass);
        methodsSignature.append(".");
        methodsSignature.append(methodName);
        if (paramsClasses.isEmpty()) {
            return methodsSignature.toString();
        }
        if (paramsClasses.size() == 1 && paramsClasses.get(0).equals("void")) {
            methodsSignature.append("()");
        } else {
            methodsSignature.append("(");
            paramsClasses.forEach(s -> methodsSignature.append(s).append(","));
            methodsSignature.deleteCharAt(methodsSignature.length() - 1);
            methodsSignature.append(")");
        }
        return methodsSignature.toString();
    }

    String getClassQualifiedName() {
        return targetClassQualifiedName;
    }

    private boolean isOfGivenMethod(PsiMethod method) {
        String methodsSignature = MethodUtils.calculateSignature(method);
        String methodsSignatureWithoutParams = methodsSignature.split("\\(")[0];
        String refactoringSignature = getMethodsSignature();
        return refactoringSignature.equals(methodsSignature) ||
                refactoringSignature.equals(methodsSignatureWithoutParams);
    }

    private boolean isToGivenPsiClass(PsiClass aClass) {
        return getClassQualifiedName().equals(aClass.getQualifiedName());
    }

    static Set<TextFormRefactoring> getRefactoringOfGivenMethod(Set<TextFormRefactoring> textualRefactorings,
                                                                     PsiMethod method) {
        return textualRefactorings.stream().
                filter(textualRefactoring -> textualRefactoring.isOfGivenMethod(method)).collect(Collectors.toSet());
    }

    static Set<TextFormRefactoring> getRefactoringsToGivenClass(Set<TextFormRefactoring> textualRefactorings,
                                                                PsiClass aClass) {
        return textualRefactorings.stream().
                filter(textualRefactoring -> textualRefactoring.isToGivenPsiClass(aClass)).
                collect(Collectors.toSet());
    }
}
