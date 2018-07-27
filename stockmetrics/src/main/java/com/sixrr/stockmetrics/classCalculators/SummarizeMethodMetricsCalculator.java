/*
 * Copyright 2005, Sixth and Red River Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.sixrr.stockmetrics.classCalculators;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricsResultsHolder;
import com.sixrr.stockmetrics.methodCalculators.MethodCalculator;

public abstract class SummarizeMethodMetricsCalculator extends ClassCalculator {
    private final Metric methodMetric = getMethodMetric();
    private final MethodCalculator methodCalculator = getMethodCalculator();

    abstract protected Metric getMethodMetric();
    abstract protected MethodCalculator getMethodCalculator();

    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);
            SummarizeMetricResultsHolder holder = new SummarizeMetricResultsHolder();
            getMethodCalculator().beginMetricsRun(methodMetric, holder, null);
            for (PsiMethod method : aClass.getMethods()) {
                methodCalculator.processMethod(method);
            }
            getMethodCalculator().endMetricsRun();
            postMetric(aClass, holder.sum);
        }

        private class SummarizeMetricResultsHolder implements MetricsResultsHolder {
            private int sum = 0;

            @Override
            public void postProjectMetric(Metric metric, double value) {
                throw new UnsupportedOperationException("Method should be called in this realization!");
            }

            @Override
            public void postFileTypeMetric(Metric metric, FileType fileType, double value) {
                throw new UnsupportedOperationException("Method should be called in this realization!");
            }

            @Override
            public void postModuleMetric(Metric metric, Module module, double value) {
                throw new UnsupportedOperationException("Method should be called in this realization!");
            }

            @Override
            public void postPackageMetric(Metric metric, PsiPackage aPackage, double value) {
                throw new UnsupportedOperationException("Method should be called in this realization!");
            }

            @Override
            public void postClassMetric(Metric metric, PsiClass aClass, double value) {
                throw new UnsupportedOperationException("Method should be called in this realization!");
            }

            @Override
            public void postInterfaceMetric(Metric metric, PsiClass anInterface, double value) {
                throw new UnsupportedOperationException("Method should be called in this realization!");
            }

            @Override
            public void postMethodMetric(Metric metric, PsiMethod method, double value) {
                sum += value;
            }

            @Override
            public void postProjectMetric(Metric metric, double numerator, double denominator) {
                throw new UnsupportedOperationException("Method should be called in this realization!");
            }

            @Override
            public void postFileTypeMetric(Metric metric, FileType fileType, double numerator, double denominator) {
                throw new UnsupportedOperationException("Method should be called in this realization!");
            }

            @Override
            public void postModuleMetric(Metric metric, Module module, double numerator, double denominator) {
                throw new UnsupportedOperationException("Method should be called in this realization!");
            }

            @Override
            public void postPackageMetric(Metric metric, PsiPackage aPackage, double numerator, double denominator) {
                throw new UnsupportedOperationException("Method should be called in this realization!");
            }

            @Override
            public void postClassMetric(Metric metric, PsiClass aClass, double numerator, double denominator) {
                throw new UnsupportedOperationException("Method should be called in this realization!");
            }

            @Override
            public void postInterfaceMetric(Metric metric, PsiClass anInterface, double numerator, double denominator) {
                throw new UnsupportedOperationException("Method should be called in this realization!");
            }

            @Override
            public void postMethodMetric(Metric metric, PsiMethod method, double numerator, double denominator) {
                throw new UnsupportedOperationException("Method should be called in this realization!");
            }
        }
    }
}
