/*
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ml_methods_group.algorithm.entity;

import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsRun;
import org.ml_methods_group.algorithm.PSIUtil;
import org.ml_methods_group.algorithm.PropertiesFinder;

public class MethodEntity extends Entity {
    private final boolean isOverriding;

    public MethodEntity(PsiMethod method, MetricsRun metricsRun, PropertiesFinder propertiesFinder) {
        super(method, metricsRun, propertiesFinder);
        isOverriding = PSIUtil.isOverriding((PsiMethod) getPsiElement());
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

    public boolean isOverriding() {
        return isOverriding;
    }
}
