/*
 * Copyright 2018 Machine Learning Methods in Software Engineering Group of JetBrains Research
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

package com.sixrr.stockmetrics.projectCalculators;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.MethodUtils;
import com.sixrr.stockmetrics.utils.CyclomaticComplexityUtil;

public class AverageNumOfAncestorsProjectCalculator extends ProjectCalculator {
    private int totalNumOfAncestors = 0;
    private int numOfClasses = 0;

    @Override
    public void endMetricsRun() {
        postMetric(totalNumOfAncestors, numOfClasses);
    }

    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitClass(PsiClass aClass) {
            totalNumOfAncestors += aClass.getExtendsListTypes().length;
            numOfClasses++;
        }
    }
}
