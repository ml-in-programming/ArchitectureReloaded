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

package com.sixrr.stockmetrics.classCalculators;

import com.intellij.psi.*;

public class MeasureOfFunctionalAbstractionCalculator extends ClassCalculator {

    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);
            if (isConcreteClass(aClass)) {
                final PsiMethod[] allMethods = aClass.getAllMethods();
                double numInheritedMethods = 0;
                for (final PsiMethod method : allMethods) {
                    final PsiClass containingClass = method.getContainingClass();
                    if (containingClass != null && !containingClass.equals(aClass) &&
                            !method.hasModifierProperty(PsiModifier.STATIC)) {
                        numInheritedMethods++;
                    }
                }

                postMetric(aClass, numInheritedMethods / allMethods.length);
            }
        }
    }
}

