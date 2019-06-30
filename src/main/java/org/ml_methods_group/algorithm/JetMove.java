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

import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.MethodEntity;

import java.util.ArrayList;
import java.util.List;

public class JetMove extends Algorithm {
    JetMove() {
        super("JetMove", false);
    }

    @Override
    protected List<Refactoring> calculateRefactorings(ExecutionContext context, boolean enableFieldRefactorings) {
        List<MethodEntity> allMethods = context.getEntities().getMethods(); // get all methods ??
        List<ClassEntity> allClasses = context.getEntities().getClasses(); // get all classes ??
        List<Refactoring> refactorings = new ArrayList<>();

        for(MethodEntity curMethod : allMethods) {
            ClassEntity curClass = curMethod.getClassEntity(); // the class of currMethod
            double curSimilarity = calculateSimilarity(curMethod, curClass);
            List<ClassEntity> potentialClasses = new ArrayList<>();
            for(ClassEntity potentialClass : allClasses) {
                if(calculateSimilarity(curMethod, potentialClass) > curSimilarity)
                    potentialClasses.add(potentialClass);
            }
            ClassEntity bestClass = findBestClass(potentialClasses);
            if(bestClass != null) refactorings.add(new Refactoring(curMethod.getName(), bestClass.getName(), 0, false)); //accuracy ~ difference between coeff ??
        }
        return refactorings;
    }

    private double calculateSimilarity(MethodEntity methodEntity, ClassEntity classEntity) {
        double similarity = 0;
        List<MethodEntity> classMethods = classEntity.getMethodEntities(); //all methods of classEntity
        for(MethodEntity curMethod : classMethods) {
            if(methodEntity != curMethod)
                similarity += methodSimilarity(methodEntity, curMethod);
        }
        if(methodEntity.getClassName() != classEntity.getName())
            return similarity / classMethods.size();
        else
            return  similarity / (classMethods.size() - 1);
    }

    private double methodSimilarity (MethodEntity methodFst, MethodEntity methodSnd) {
        Dependencies depFst = new Dependencies(methodFst);
        Dependencies depSnd = new Dependencies(methodSnd);

        int depCardinalityFst = depFst.cardinality();
        int depCardinalitySnd = depSnd.cardinality();
        int depCardinalityIntersection = depFst.calculateIntersectionCardinality(depSnd);
        return (double)depCardinalityIntersection/(depCardinalityFst + depCardinalitySnd - depCardinalityIntersection); // Jaccard Coefficient
    }

    protected class Dependencies {
        // here are lists that need to be made:

        //method calls
        //field accesses
        //object instantiations
        //local declarations
        //return types
        //exceptions
        //annotations

        //ignoring primitive types and types and annotations from java.lang and java.util

        public Dependencies(MethodEntity m) {
            //find all these things
        }


        public int cardinality() {

            return 0; //todo
        }

        public int calculateIntersectionCardinality(Dependencies depSnd) {
            return 0; //todo
        }

    }

    private ClassEntity findBestClass(List <ClassEntity> potentialClasses) {
        //todo choose movable class with the biggest coefficient
        return null;
    }
}
