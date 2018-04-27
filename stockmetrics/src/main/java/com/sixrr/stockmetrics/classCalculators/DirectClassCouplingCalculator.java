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
import com.intellij.psi.util.PsiUtil;
import com.sixrr.stockmetrics.projectCalculators.ProjectCalculator;

import java.util.HashSet;
import java.util.Set;

public class DirectClassCouplingCalculator extends ClassCalculator {
    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitClass(PsiClass aClass) {
            Set<PsiClass> classes = new HashSet<>();
            PsiField[] fields = aClass.getFields();
            for (PsiField field : fields) {
                if (!field.isPhysical()) {
                    continue;
                }
                PsiType type = field.getType().getDeepComponentType();
                PsiClass classInType = PsiUtil.resolveClassInType(type);
                if (classInType == null) {
                    continue;
                }
                classes.add(classInType);
            }
            PsiMethod[] methods = aClass.getMethods();
            for (PsiMethod method : methods){
                PsiParameter[] parameters = method.getParameterList().getParameters();
                for(PsiParameter parameter : parameters){
                    PsiTypeElement typeElement = parameter.getTypeElement();
                    if(typeElement == null){
                        continue;
                    }
                    PsiType type = typeElement.getType().getDeepComponentType();
                    PsiClass classInType = PsiUtil.resolveClassInType(type);
                    if (classInType == null) {
                        continue;
                    }
                    classes.add(classInType);
                }
            }
            postMetric(aClass, classes.size());
        }
    }

}
