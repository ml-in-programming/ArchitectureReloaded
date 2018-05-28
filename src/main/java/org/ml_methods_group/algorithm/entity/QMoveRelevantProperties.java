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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;

import java.util.*;

import static org.ml_methods_group.utils.QMoveUtil.incrementMapValue;

class QMoveRelevantProperties  {
    //cohesion
    private double sumIntersection;
    private Map<String, Integer> parametersOfMethods = new HashMap<>();
    //coupling
    private Map<String, Integer> relatedClasses = new HashMap<>();

    private Set<QMoveClassEntity> outerClasses = new HashSet<>();
    private Set<QMoveClassEntity> innerClasses = new HashSet<>();
    private Set<QMoveClassEntity> supers = new HashSet<>();
    private Set<QMoveClassEntity> inheritors = new HashSet<>();
    private int numUserDefinedClasses;
    private boolean containsPrivateCalls;
    private boolean containsProtectedCalls;
    private int numOfMethods;

    void incrementNumOfMethods(){
        numOfMethods++;
    }

    void addToParameters(PsiType psiType){
        incrementMapValue(psiType.getPresentableText(),
                parametersOfMethods);
    }

    void addToRelatedClasses(PsiClass psiClass){
        incrementMapValue(psiClass.getQualifiedName(),
                relatedClasses);
    }

    void increaseSumIntersection(int val){
        sumIntersection += val;
    }

    int setMethodSumIntersection(){
        sumIntersection = parametersOfMethods.size();
        return parametersOfMethods.size();
    }
    Map<String, Integer> getParametersOfMethods() {
        return parametersOfMethods;
    }

    Map<String, Integer> getRelatedClasses() {
        return relatedClasses;
    }

    Set<QMoveClassEntity> getInnerClasses() {
        return innerClasses;
    }

    void setInnerClasses(Set<QMoveClassEntity> innerClasses) {
        this.innerClasses = innerClasses;
    }


    void setSupers(Set<QMoveClassEntity> supers) {
        this.supers = supers;
    }

    Set<QMoveClassEntity> getInheritors() {
        return inheritors;
    }

    void incrementUserDefinedClasses(){
        numUserDefinedClasses++;
    }
    int getNumUserDefinedClasses() {
        return numUserDefinedClasses;
    }

    boolean isContainsPrivateCalls() {
        return containsPrivateCalls;
    }

    void setContainsPrivateCalls(boolean containsPrivateCalls) {
        this.containsPrivateCalls = containsPrivateCalls;
    }

    boolean isContainsProtectedCalls() {
        return containsProtectedCalls;
    }

    void setContainsProtectedCalls(boolean containsProtectedCalls) {
        this.containsProtectedCalls = containsProtectedCalls;
    }

    void setInheritors(Set<QMoveClassEntity> inheritors) {
        this.inheritors = inheritors;
    }

    Set<QMoveClassEntity> getSupers() {
        return supers;
    }


     double getSumIntersection() {
        return sumIntersection;
    }

    int getNumOfMethods() {
        return numOfMethods;
    }


    Set<QMoveClassEntity> getOuterClasses() {
        return outerClasses;
    }

    void setOuterClasses(Set<QMoveClassEntity> outerClasses) {
        this.outerClasses = outerClasses;
    }
}
