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


import com.intellij.psi.*;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.MethodEntity;
import org.ml_methods_group.config.Logging;

import java.util.*;

public class JMove extends Algorithm {
    private static final Logger LOGGER = Logging.getLogger(JMove.class);
    private final int MIN_NUMBER_OF_CANDIDATE_CLASSES = 3;
    private final int MIN_NUMBER_OF_DEPENDENCIES = 4;
    private final double MIN_DIFF_BETWEEN_SIMILARITY_COEFF_PERS = 0.25;

    public JMove() {
        super("JMove", false);
    }

    @Override
    protected List<Refactoring> calculateRefactorings(ExecutionContext context, boolean enableFieldRefactorings) {
        LOGGER.info("Starting calculating refactorings");
        List<MethodEntity> allMethods = context.getEntities().getMethods();
        List<ClassEntity> allClasses = context.getEntities().getClasses();
        LOGGER.info("Found " + allMethods.size() + " methods and " + allClasses.size() + " classes");
        List<Refactoring> refactorings = new ArrayList<>();

        Map<String, ClassEntity> nameToClassEntity = new HashMap<>();
        Map<String, Dependencies> nameToDependencies = new HashMap<>();

        for(ClassEntity classEntity : allClasses) {
            nameToClassEntity.put(classEntity.getName(), classEntity);
        }

        for(MethodEntity methodEntity : allMethods) {
            nameToDependencies.put(methodEntity.getName(), new Dependencies(methodEntity));
        }


        for(MethodEntity curMethod : allMethods) {
            if(!curMethod.isMovable())
                continue;
            LOGGER.info("Checking " + curMethod.getName());

            Dependencies curDependencies = nameToDependencies.get(curMethod.getName());
            if(curDependencies.cardinality() < MIN_NUMBER_OF_DEPENDENCIES)
                continue;
            ClassEntity curClass = nameToClassEntity.get(curMethod.getClassName());
            double curSimilarity = calculateSimilarity(curMethod, curClass, nameToDependencies);
            Map<ClassEntity, Double> potentialClasses = new HashMap<>();
            for(ClassEntity potentialClass : allClasses) {
                double potentialClassSimilarity = calculateSimilarity(curMethod, potentialClass, nameToDependencies);
                if (potentialClassSimilarity > curSimilarity) {
                    potentialClasses.put(potentialClass, potentialClassSimilarity);
                }
            }

            if(potentialClasses.size() < MIN_NUMBER_OF_CANDIDATE_CLASSES)
                continue;

            ClassEntity bestClass = findBestClass(potentialClasses);
            if(bestClass != null) {
                double diff = (potentialClasses.get(bestClass) - curSimilarity)/potentialClasses.get(bestClass); //may be idk
                if(diff >= MIN_DIFF_BETWEEN_SIMILARITY_COEFF_PERS)
                    refactorings.add(new Refactoring(curMethod.getName(), bestClass.getName(), diff, false)); //accuracy ~ difference between coeff ?? idk
            }
        }
        return refactorings;
    }

    private double calculateSimilarity(@NotNull MethodEntity methodEntity, @NotNull ClassEntity classEntity, Map<String, Dependencies> nameToDependencies) {
        double similarity = 0;
        Set<String> methodNames = classEntity.getRelevantProperties().getMethods();

        for(String curMethodName : methodNames) {
            if(!methodEntity.getName().equals(curMethodName))
                similarity += methodSimilarity(nameToDependencies.get(methodEntity.getName()), nameToDependencies.get(curMethodName));
        }
        if(methodEntity.getClassName().equals(classEntity.getName()))
            return similarity / methodNames.size();
        else
            return  similarity / (methodNames.size() - 1);
    }

    private double methodSimilarity (@NotNull Dependencies depFst,@NotNull Dependencies depSnd) {
        int depCardinalityFst = depFst.cardinality();
        int depCardinalitySnd = depSnd.cardinality();
        int depCardinalityIntersection = depFst.calculateIntersectionCardinality(depSnd);
        return (double)depCardinalityIntersection/(depCardinalityFst + depCardinalitySnd - depCardinalityIntersection); // Jaccard Coefficient
    }

    private class Dependencies {

        //method calls
        private Set<String> methodCalls;

        //field accesses
        private Set<String> fieldAccesses;

        //object instantiations
        private Set<String> objectInstantiations; //idk

        //local declarations
        private  Set<String> localDeclarations; //idk

        //return types
        private String returnType;

        //exceptions
        private Set<String> exceptions;

        //annotations
        private Set<String> annotations;

        //todo ignore primitive types and types and annotations from java.lang and java.util

        private Dependencies(@NotNull MethodEntity methodForDependencies) {
            methodCalls = methodForDependencies.getRelevantProperties().getMethods();
            fieldAccesses = methodForDependencies.getRelevantProperties().getFields();
            returnType = methodForDependencies.getPsiMethod().getReturnType().toString();

            exceptions = new HashSet<>();
            PsiClassType[] referencedTypes = methodForDependencies.getPsiMethod().getThrowsList().getReferencedTypes();
            for(PsiClassType classType : referencedTypes) {
                exceptions.add(classType.getClassName()); //may be not that name todo: check
            }

            annotations = new HashSet<>();
            PsiAnnotation[] psiAnnotations = methodForDependencies.getPsiMethod().getModifierList().getAnnotations();
            for(PsiAnnotation psiAnnotation : psiAnnotations) {
                annotations.add(psiAnnotation.getQualifiedName()); //may be not that name todo: check
            }

            localDeclarations = new HashSet<>();
            objectInstantiations = new HashSet<>();
            methodForDependencies.getPsiMethod().accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitLocalVariable(PsiLocalVariable variable) {
                    super.visitLocalVariable(variable);
                    localDeclarations.add(variable.getType().getCanonicalText()); //idk
                    //System.out.println("Found a variable at offset " + variable.getTextRange().getStartOffset());
                }
                @Override
                public void visitNewExpression(PsiNewExpression expression) {
                    super.visitNewExpression(expression);
                    objectInstantiations.add(expression.getClassOrAnonymousClassReference().getQualifiedName());
                }
            });

//            newStatements = new HashSet<>();
//            methodForDependencies.getPsiMethod().accept(new JavaRecursiveElementVisitor() {
//                @Override
//                public void visitNewExpression(PsiNewExpression expression) {
//                    super.visitNewExpression(expression);
//                    newStatements.add(expression.getText());
//                }
//            });
        }


        private int cardinality() { //idk may be I just need to do one whole set

            return    methodCalls.size()
                    + fieldAccesses.size()
                    + objectInstantiations.size()
                    + localDeclarations.size()
                    + 1 //returnType //fixme: may be null if I learn to ignore
                    + exceptions.size()
                    + annotations.size();
        }

        private int calculateIntersectionCardinality(@NotNull Dependencies depSnd) {
            int intersectionCardinality = 0;

            Set<String> methodCallIntersection = new HashSet<>(methodCalls);
            methodCallIntersection.retainAll(depSnd.methodCalls);
            intersectionCardinality += methodCallIntersection.size();

            Set<String> instancesIntersection = new HashSet<>(fieldAccesses);
            instancesIntersection.retainAll(depSnd.fieldAccesses);
            intersectionCardinality += instancesIntersection.size();

            if(returnType.equals(depSnd.returnType))
                intersectionCardinality++;

            Set<String> exceptionsIntersection = new HashSet<>(exceptions);
            exceptionsIntersection.retainAll(depSnd.exceptions);
            intersectionCardinality += exceptionsIntersection.size();

            Set<String> annotationsIntersection = new HashSet<>(exceptions);
            annotationsIntersection.retainAll(depSnd.exceptions);
            intersectionCardinality += annotationsIntersection.size();

            Set<String> localVariablesIntersection = new HashSet<>(localDeclarations);
            localVariablesIntersection.retainAll(depSnd.localDeclarations);
            intersectionCardinality += localVariablesIntersection.size();

            return intersectionCardinality;
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
