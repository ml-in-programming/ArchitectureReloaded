/*
 * Copyright 2005-2017 Sixth and Red River Software, Bas Leijdekkers
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

package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FanInMethodCalculator extends MethodCalculator {
    private PsiMethod currentMethod;
    private int methodNestingDepth = 0;
    private final HashMap<PsiMethod, Integer> references = new HashMap<>();
    private final Set<PsiMethod> primaryMethods = new HashSet<>();

    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitAnonymousClass(PsiAnonymousClass aClass) {
            // ignore
        }

        @Override
        public void visitMethod(PsiMethod method) {
            if (methodNestingDepth == 0) {
                currentMethod = method;
                primaryMethods.add(method);
            }

            methodNestingDepth++;
            super.visitMethod(method);
            methodNestingDepth--;
        }

        @Override
        public void visitCallExpression(PsiCallExpression expression) {
            final PsiMethod method = expression.resolveMethod();
            if (method != null && !method.equals(currentMethod)) {
                references.put(method, references.getOrDefault(method, 0) + 1);
            }
            super.visitCallExpression(expression);
        }
    }

    @Override
    public void endMetricsRun() {
        ApplicationManager.getApplication()
                .runReadAction(this::postResult);
    }

    private void postResult() {
        for (PsiMethod method : primaryMethods) {
            postMetric(method, references.getOrDefault(method, 0));
        }
    }
}
