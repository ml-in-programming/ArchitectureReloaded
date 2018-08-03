package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.research.groups.ml_methods.utils.PSIUtil;

public class MethodOldEntity extends OldEntity {

    public MethodOldEntity(PsiMethod method) {
        super(method);
        isMovable = ApplicationManager.getApplication().runReadAction((Computable<Boolean>)
                () ->  !PSIUtil.isOverriding(method) &&
                        !MethodUtils.isAbstract(method) && !method.isConstructor());
    }

    private MethodOldEntity(MethodOldEntity original) {
        super(original);
    }

    @Override
    public MetricCategory getCategory() {
        return MetricCategory.Method;
    }

    @Override
    public String getClassName() {
        final String signature = getName();
        final String name = signature.substring(0, signature.indexOf('('));
        return name.substring(0, name.lastIndexOf('.'));
    }

    @Override
    public MethodOldEntity copy() {
        return new MethodOldEntity(this);
    }

    @Override
    public boolean isField() {
        return false;
    }
}
