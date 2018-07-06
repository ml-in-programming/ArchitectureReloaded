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

import java.util.*;

public class QMoveMethodEntity extends MethodEntity {
    private QMoveClassEntity containingClass;
    private QMoveRelevantProperties properties = new QMoveRelevantProperties();
    private MoveAbility moveAbility;

    QMoveMethodEntity(PsiMethod method) {
        super(method);
    }

    @Override
    void calculateVector(MetricsRun metricsRun) {
        moveAbility = MoveAbility.ANY_CLASS;
        if(properties.isContainsProtectedCalls()){
            moveAbility = MoveAbility.INHERITOR;
        }
        if(properties.isContainsPrivateCalls()){
            moveAbility = MoveAbility.INNER_OR_OUTER;
        }
    }


    public List<Set<QMoveClassEntity>> getTargets(){
        List<Set<QMoveClassEntity>> list = new ArrayList<>();
        list.add(properties.getInnerClasses());
        list.add(properties.getOuterClasses());
        if(moveAbility != MoveAbility.INNER_OR_OUTER){
            list.add(properties.getInheritors());
        }
        return list;
    }


    public boolean isValidMoveToClass(QMoveClassEntity targetClass) {
        if (!isMovable) {
            return false;
        }
        if (properties.getInnerClasses().contains(targetClass)
                || targetClass.getProperties()
                .getInnerClasses().contains(containingClass)) {
            return true;
        }
        if (properties.isContainsPrivateCalls()) {
            return false;
        }
        return !properties.isContainsProtectedCalls()
                || targetClass.getProperties().getSupers().contains(containingClass);
    }

    public QMoveRelevantProperties getProperties() {
        return properties;
    }

    void setContainingClass(QMoveClassEntity containingClass) {
        this.containingClass = containingClass;
    }

    public MoveAbility getMoveAbility() {
        return moveAbility;
    }

    public enum MoveAbility {
        ANY_CLASS,
        INHERITOR,
        INNER_OR_OUTER
    }
}
