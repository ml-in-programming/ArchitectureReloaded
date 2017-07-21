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

import com.intellij.psi.PsiClass;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import org.ml_methods_group.algorithm.PSIUtil;
import org.ml_methods_group.algorithm.PropertiesFinder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClassEntity extends Entity {
    public ClassEntity(PsiClass psiClass, MetricsRunImpl metricsRun, PropertiesFinder propertiesFinder) {
        super(psiClass, metricsRun, propertiesFinder);
    }

    @Override
    public MetricCategory getCategory() {
        return MetricCategory.Class;
    }

    @Override
    public String getClassName() {
        return getName();
    }

    @Override
    protected double[] initializeVector(MetricsRunImpl metricsRun) {
        final double[] vector = new double[DIMENSION];
        for (int i = 0; i < DIMENSION; i++) {
            vector[i] = 0.0;
        }
        final MetricCategory category = getCategory();
        final MetricsResult results = metricsRun.getResultsForCategory(category);
        processEntity(getName(), category, results, metricsRun, vector);

        return vector;
    }

    @Override
    protected HashSet<String> findRelevantProperties() {
        return new HashSet<>(Collections.singletonList(getName()));
    }

    public Set<PsiClass> getAllSupers(Set<PsiClass> existing) {
        return PSIUtil.getAllSupers((PsiClass) getPsiElement(), existing);
    }
}
