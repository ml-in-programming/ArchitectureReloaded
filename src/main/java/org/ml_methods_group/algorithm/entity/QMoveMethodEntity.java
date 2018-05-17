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

package org.ml_methods_group.algorithm.entity;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.sixrr.metrics.metricModel.MetricsRun;
import org.ml_methods_group.utils.PSIUtil;
import org.ml_methods_group.utils.QMoveUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QMoveMethodEntity extends MethodEntity {
    private final PsiMethod psiMethod;
    private boolean containsPrivateCalls;
    private boolean isContainsOnlyPublicCalls;

    private Map<PsiClass, Integer> relatedClasses = new HashMap<>();

    QMoveMethodEntity(PsiMethod method) {
        super(method);
        this.psiMethod = method;
    }

    @Override
    void calculateVector(MetricsRun metricsRun) {
        QMoveUtil.calculateRelatedClasses(psiMethod, relatedClasses);
    }

    public PsiMethod getPsiMethod() {
        return psiMethod;
    }

    public boolean isValidMoveToClass(PsiClass targetClass){
        if(!isMovable){
            return false;
        }
        PsiClass containingClass = psiMethod.getContainingClass();
        if(getAllInnerClasses(targetClass, new HashSet<>()).
                contains(containingClass)
                || getAllInnerClasses(containingClass, new HashSet<>()).
                contains(targetClass)){
            return true;
        }
        if(PSIUtil.getAllSupers(targetClass).contains(containingClass)){

        }
        else {

        }
        return true;
    }


    private Set<PsiClass> getAllInnerClasses(PsiClass aClass, Set<PsiClass> innerClasses){
        innerClasses.add(aClass);
        for(PsiClass psiClass : aClass.getInnerClasses()){
            getAllInnerClasses(psiClass, innerClasses);
        }
        return innerClasses;
    }

    Map<PsiClass, Integer> getRelatedClasses() {
        return relatedClasses;
    }
}
