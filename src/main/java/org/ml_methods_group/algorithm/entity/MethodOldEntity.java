package org.ml_methods_group.algorithm.entity;

import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.utils.MethodUtils;
import org.ml_methods_group.utils.PSIUtil;

public class MethodOldEntity extends OldEntity {

    public MethodOldEntity(PsiMethod method) {
        super(method);
        isMovable = !PSIUtil.isOverriding(method) &&
                !MethodUtils.isAbstract(method) && !method.isConstructor();
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
