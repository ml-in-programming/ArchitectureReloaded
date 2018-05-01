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

package org.ml_methods_group.algorithm;


import com.intellij.psi.PsiType;
import com.intellij.psi.PsiReferenceList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.MethodEntity;

import java.util.*;

public class JMove extends Algorithm {
    private final int MIN_NUMBER_OF_CANDIDATE_CLASSES = 3;
    private final int MIN_NUMBER_OF_DEPENDENCIES = 4;
    private final double MIN_DIFF_BETWEEN_SIMILARITY_COEFF_PERS = 0.25;

    JMove() {
        super("JMove", false);
    }

    @Override
    protected List<Refactoring> calculateRefactorings(ExecutionContext context, boolean enableFieldRefactorings) {
        List<MethodEntity> allMethods = context.getEntities().getMethods(); // get all methods ??
        List<ClassEntity> allClasses = context.getEntities().getClasses(); // get all classes ??
        List<Refactoring> refactorings = new ArrayList<>();

        Map<String, ClassEntity> nameToClassEntity = new HashMap<>();
        Map<String, MethodEntity> nameToMethodEntity = new HashMap<>();

        for(ClassEntity classEntity : allClasses) {
            nameToClassEntity.put(classEntity.getName(), classEntity); //I see no other way to find containing ClassEntity for MethodEntity
        }

        for(MethodEntity methodEntity : allMethods) {
            nameToMethodEntity.put(methodEntity.getName(), methodEntity); //same
        }


        for(MethodEntity curMethod : allMethods) {
            Dependencies curDependencies = new Dependencies(curMethod);
            if(curDependencies.cardinality() < MIN_NUMBER_OF_DEPENDENCIES)
                continue;
            ClassEntity curClass = nameToClassEntity.get(curMethod.getClassName()); //ClassEntity of curMethod (potentially working)
            double curSimilarity = calculateSimilarity(curMethod, curClass, nameToMethodEntity);
            Map<ClassEntity, Double> potentialClasses = new HashMap<>();
            for(ClassEntity potentialClass : allClasses) {
                double potentialClassSimilarity = calculateSimilarity(curMethod, potentialClass, nameToMethodEntity);
                if(potentialClassSimilarity > curSimilarity) {
                    potentialClasses.put(potentialClass, potentialClassSimilarity);
                }
            }

            if(potentialClasses.size() < MIN_NUMBER_OF_CANDIDATE_CLASSES)
                continue;

            ClassEntity bestClass = findBestClass(potentialClasses);
            if(bestClass != null) {
                double diff = (potentialClasses.get(bestClass) - curSimilarity)/potentialClasses.get(bestClass); //may be
                if(diff >= MIN_DIFF_BETWEEN_SIMILARITY_COEFF_PERS)
                    refactorings.add(new Refactoring(curMethod.getName(), bestClass.getName(), diff, false)); //accuracy ~ difference between coeff ??
            }
        }
        return refactorings;
    }

    private double calculateSimilarity(@NotNull MethodEntity methodEntity, @NotNull ClassEntity classEntity, Map<String, MethodEntity> nameToMethodEntity) {
        double similarity = 0;
        Set<String> methodNames = classEntity.getRelevantProperties().getMethods();

        for(String curMethodName : methodNames) {
            MethodEntity curMethod = nameToMethodEntity.get(curMethodName);
            if(!methodEntity.getName().equals(curMethodName))
                similarity += methodSimilarity(methodEntity, curMethod);
        }
        if(methodEntity.getClassName().equals(classEntity.getName()))
            return similarity / methodNames.size();
        else
            return  similarity / (methodNames.size() - 1);
    }

    private double methodSimilarity (MethodEntity methodFst, MethodEntity methodSnd) {
        Dependencies depFst = new Dependencies(methodFst);
        Dependencies depSnd = new Dependencies(methodSnd);

        int depCardinalityFst = depFst.cardinality();
        int depCardinalitySnd = depSnd.cardinality();
        int depCardinalityIntersection = depFst.calculateIntersectionCardinality(depSnd);
        return (double)depCardinalityIntersection/(depCardinalityFst + depCardinalitySnd - depCardinalityIntersection); // Jaccard Coefficient
    }

    private class Dependencies {
        // here are lists that need to be made:

        //method calls
        private Set<String> methodCalls;

        //field accesses
        //object instantiations
        //local declarations
        private Set<String> instances;

        //return types
        private PsiType returnType; //use PsiMethod PsiType getReturnType()

        //exceptions
        private PsiReferenceList exceptions; //use PsiMethod PsiReferenceList getThrowsList()

        //annotations
        private List<String> annotations;

        //todo ignore primitive types and types and annotations from java.lang and java.util

        private Dependencies(@NotNull MethodEntity m) {
            methodCalls = m.getRelevantProperties().getMethods();
            instances = m.getRelevantProperties().getMethods();
            returnType = m.getPsiMethod().getReturnType();
            exceptions = m.getPsiMethod().getThrowsList();
            //todo initialize annotations
        }


        private int cardinality() {

            return    methodCalls.size()
                    + instances.size()
                    + 1 //returnType
                    + exceptions.getReferencedTypes().length
                    + annotations.size();
        }

        private int calculateIntersectionCardinality(@NotNull Dependencies depSnd) {
            int result = 0;

            Set<String> methodCallIntersection = new HashSet<>(methodCalls);
            methodCallIntersection.retainAll(depSnd.methodCalls);
            result += methodCallIntersection.size();

            Set<String> instancesIntersection = new HashSet<>(instances);
            instancesIntersection.retainAll(depSnd.instances);
            result += instancesIntersection.size();

            if(returnType.toString().equals(depSnd.returnType.toString()))
                result++;

            //todo find intersection of exceptions and annotations

            return result;
        }

    }

    @Nullable
    private ClassEntity findBestClass(@NotNull Map<ClassEntity, Double> potentialClasses) { //choose movable class with the biggest coefficient
        if(potentialClasses.size() < MIN_NUMBER_OF_CANDIDATE_CLASSES)
            return null;
        ClassEntity bestClass = null;
        Double bestCoefficient = 0.0;
        for(Map.Entry<ClassEntity, Double> entry : potentialClasses.entrySet()) {
            if(entry.getValue() > bestCoefficient) {
                bestCoefficient = entry.getValue();
                bestClass = entry.getKey();
            }
        }
        return bestClass;
    }
}
