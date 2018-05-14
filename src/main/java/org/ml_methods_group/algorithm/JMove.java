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
            nameToDependencies.put(methodEntity.getName(), new Dependencies(methodEntity, nameToClassEntity));
        }


        for(MethodEntity curMethod : allMethods) {
            if(!curMethod.isMovable())
                continue;
            LOGGER.info("Checking " + curMethod.getName()); // todo some logging

            Dependencies curDependencies = nameToDependencies.get(curMethod.getName());
            ClassEntity curClass = nameToClassEntity.get(curMethod.getClassName());
            if(curDependencies.cardinality() < MIN_NUMBER_OF_DEPENDENCIES //experimental
                    || curClass.getRelevantProperties().getMethods().size() == 1 //because we calculate similarity between this method and all remaining in curClass
                    || isGetter(curMethod) //this methods are rarely implemented in the wrong classes
                    || isSetter(curMethod)) //todo: check if we need this check here and not in other place
                continue;
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
            //todo may be i should check weither it is possible to make this refactoring oops
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
        //field accesses
        //object instantiations
        //local declarations
        // formal parameters
        //return type
        //exceptions
        //annotations

        private Set<String> all;

        //ignoring primitive types and types and annotations from java.lang and java.util

        protected Dependencies(@NotNull MethodEntity methodForDependencies, @NotNull Map<String, ClassEntity> nameToClassEntity  ) {
            all = new HashSet<>(); //todo use

            //classes of methods that are called inside this method
            for(String methodName : methodForDependencies.getRelevantProperties().getMethods()) {
                if(!methodName.equals(methodForDependencies.getName())) //because it's somehow always there
                    all.add(methodName.substring(0, methodName.lastIndexOf('.')));
            }

            // classes of fields that the method accesses
            for(String fieldName : methodForDependencies.getRelevantProperties().getFields()) {
                PsiType pt = nameToClassEntity.get(fieldName.substring(0, fieldName.lastIndexOf('.'))).getPsiClass().findFieldByName(fieldName.substring(fieldName.lastIndexOf('.') + 1), false).getType();
                if(!(pt instanceof PsiPrimitiveType)
                        || pt.getCanonicalText().startsWith("java.lang.")
                        || pt.getCanonicalText().startsWith("java.util."))
                    all.add(pt.getCanonicalText());
            }

            //return type
            if(methodForDependencies.getPsiMethod().getReturnType() instanceof PsiPrimitiveType //return type
                    || methodForDependencies.getPsiMethod().getReturnType().getCanonicalText().startsWith("java.lang.")
                    ||methodForDependencies.getPsiMethod().getReturnType().getCanonicalText().startsWith("java.util.") )
                all.add(methodForDependencies.getPsiMethod().getReturnType().getCanonicalText()); //idk what is going on here fixme


            //exceptions
            //idk check for internal exception handling may be
            PsiClassType[] referencedTypes = methodForDependencies.getPsiMethod().getThrowsList().getReferencedTypes();
            for(PsiClassType classType : referencedTypes) {
                if(!(classType.getCanonicalText().startsWith("java.lang.")
                    || classType.getCanonicalText().startsWith("java.util.")))
                    all.add(classType.getCanonicalText());

            }

            //annotations
            PsiAnnotation[] psiAnnotations = methodForDependencies.getPsiMethod().getModifierList().getAnnotations();
            for(PsiAnnotation psiAnnotation : psiAnnotations) {
                if(!psiAnnotation.getQualifiedName().startsWith("java.lang.") ||
                        psiAnnotation.getQualifiedName().startsWith("java.util."))
                all.add(psiAnnotation.getQualifiedName());
            }

            //formal parameters
            for(PsiParameter  parameter: methodForDependencies.getPsiMethod().getParameterList().getParameters()) {
                if(!((parameter.getType() instanceof PsiPrimitiveType)
                || parameter.getType().getCanonicalText().startsWith("java.lang")
                || parameter.getType().getCanonicalText().startsWith("java.util")))
                    all.add(parameter.getType().getCanonicalText());

            }


            methodForDependencies.getPsiMethod().accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitLocalVariable(PsiLocalVariable variable) {
                    super.visitLocalVariable(variable);
                    if(!(variable.getType() instanceof PsiPrimitiveType ||
                            variable.getType().getCanonicalText().startsWith("java.lang.") ||
                            variable.getType().getCanonicalText().startsWith("java.util."))) {
                        all.add(variable.getType().getCanonicalText());
                    }
                }
                @Override
                public void visitNewExpression(PsiNewExpression expression) {
                    super.visitNewExpression(expression);
                    if (!(expression.getClassOrAnonymousClassReference().getQualifiedName().startsWith("java.lang.") ||
                            expression.getClassOrAnonymousClassReference().getQualifiedName().startsWith("java.util."))) {
                        all.add(expression.getClassOrAnonymousClassReference().getQualifiedName());
                    }
                }

                //                @Override idk this way I will find all annotations may be I shouldn't
//                public void visitAnnotation(PsiAnnotation annotation) {
//                    super.visitAnnotation(annotation);
//                    if(!annotation.getQualifiedName().startsWith("java.lang.") ||
//                            annotation.getQualifiedName().startsWith("java.util."))
//                        annotations.add(annotation.getQualifiedName());
//                }


            });
        }

        private int cardinality() {

            return all.size();
        }

        private int calculateIntersectionCardinality(@NotNull Dependencies depSnd) {

            Set<String> intersection = new HashSet<>(all);
            intersection.retainAll(depSnd.all);

//            Set<String> methodCallIntersection = new HashSet<>(methodCalls);
//            methodCallIntersection.retainAll(depSnd.methodCalls);
//            intersectionCardinality += methodCallIntersection.size();
//
//            Set<String> instancesIntersection = new HashSet<>(fieldAccesses);
//            instancesIntersection.retainAll(depSnd.fieldAccesses);
//            intersectionCardinality += instancesIntersection.size();
//
//            if(returnType.equals(depSnd.returnType))
//                intersectionCardinality++;
//
//            Set<String> exceptionsIntersection = new HashSet<>(exceptions);
//            exceptionsIntersection.retainAll(depSnd.exceptions);
//            intersectionCardinality += exceptionsIntersection.size();
//
//            Set<String> annotationsIntersection = new HashSet<>(exceptions);
//            annotationsIntersection.retainAll(depSnd.exceptions);
//            intersectionCardinality += annotationsIntersection.size();
//
//            Set<String> localVariablesIntersection = new HashSet<>(localDeclarations);
//            localVariablesIntersection.retainAll(depSnd.localDeclarations);
//            intersectionCardinality += localVariablesIntersection.size();

            return intersection.size();
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

    private boolean isGetter (@NotNull MethodEntity methodEntity) {
        PsiMethod psiMethod = methodEntity.getPsiMethod();
        int numSt = psiMethod.getBody().getStatements().length;
        if(psiMethod.getParameterList().getParametersCount() == 0 && numSt == 1)
            return true;
        return false;
    }

    private boolean isSetter(@NotNull MethodEntity methodEntity) {
        PsiMethod psiMethod = methodEntity.getPsiMethod();
        int numSt = psiMethod.getBody().getStatements().length;
        if(psiMethod.getParameterList().getParametersCount() == 1 && numSt == 1)
            return true;
        return false;
    }
}
