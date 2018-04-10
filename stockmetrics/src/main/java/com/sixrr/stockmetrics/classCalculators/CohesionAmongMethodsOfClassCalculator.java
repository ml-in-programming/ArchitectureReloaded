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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CohesionAmongMethodsOfClassCalculator extends ClassCalculator {

    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }
    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitClass(PsiClass aClass) {
            int sumIntersection = 0;
            int numMethods = 0;
            Set<PsiType> parameters = new HashSet<>();
            for(PsiMethod method : aClass.getMethods()){
                if(method.isConstructor() || method.hasModifierProperty(PsiModifier.STATIC)){
                    continue;
                }
                numMethods++;
                Set<PsiType> parametersInMethod = Stream.of(method.getParameterList()).
                        flatMap(x -> Stream.of(x.getParameters())).
                        map(PsiVariable::getType).collect(Collectors.toSet());
                sumIntersection += parametersInMethod.size();
                parameters.addAll(parametersInMethod);
            }
            if(parameters.size() == 0){
                return;
            }
            postMetric(aClass, sumIntersection, numMethods * parameters.size());
        }
    }
}
