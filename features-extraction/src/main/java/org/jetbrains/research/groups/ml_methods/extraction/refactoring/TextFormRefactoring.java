package org.jetbrains.research.groups.ml_methods.extraction.refactoring;

import com.intellij.analysis.AnalysisScope;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TextFormRefactoring {
    private final String targetClassQualifiedName;
    private final String methodPackage;
    private final String methodName;
    private final List<String> paramsClasses;

    public TextFormRefactoring(String methodPackage, String methodName, List<String> params, String destinationClassQualifiedName) {
        this.methodPackage = methodPackage;
        this.methodName = methodName;
        this.paramsClasses = params;
        this.targetClassQualifiedName = destinationClassQualifiedName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextFormRefactoring that = (TextFormRefactoring) o;
        return Objects.equals(targetClassQualifiedName, that.targetClassQualifiedName) &&
                Objects.equals(methodPackage, that.methodPackage) &&
                Objects.equals(methodName, that.methodName) &&
                Objects.equals(paramsClasses, that.paramsClasses);
    }

    String getMethodsSignature() {
        StringBuilder methodsSignature = new StringBuilder();
        methodsSignature.append(methodPackage);
        methodsSignature.append(".");
        methodsSignature.append(methodName);
        if (paramsClasses.isEmpty()) {
            return methodsSignature.toString();
        }
        methodsSignature.append("(");
        paramsClasses.forEach(s -> methodsSignature.append(s).append(","));
        methodsSignature.deleteCharAt(methodsSignature.length() - 1);
        methodsSignature.append(")");
        return methodsSignature.toString();
    }

    @Nullable
    Refactoring toRefactoring(AnalysisScope scope) {
        return RefactoringsFinder.find(scope, Collections.singletonList(this)).get(0);
    }

    String getClassQualifiedName() {
        return targetClassQualifiedName;
    }
}
