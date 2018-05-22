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
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.MethodEntity;
import org.ml_methods_group.config.Logging;

import java.util.*;

public class JMove extends Algorithm {
    private static final Logger LOGGER = Logging.getLogger(JMove.class);
    private final int MIN_NUMBER_OF_CANDIDATE_CLASSES = 3; //experimentally found thresholds
    private final int MIN_NUMBER_OF_DEPENDENCIES = 4;
    private final double MIN_DIFF_BETWEEN_SIMILARITY_COEFF_PERS = 0.25;

//    Set<String> methodNamesWithNoDependencies;

    public JMove() {
        super("JMove", false);
//        methodNamesWithNoDependencies = new HashSet<>();
    }

    @Override
    protected List<Refactoring> calculateRefactorings(ExecutionContext context, boolean enableFieldRefactorings) {
        List<MethodEntity> allMethods = context.getEntities().getMethods();
        List<ClassEntity> allClasses = context.getEntities().getClasses();
        List<Refactoring> refactorings = new ArrayList<>();

        Map<String, ClassEntity> nameToClassEntity = new HashMap<>();
        Map<String, Dependencies> nameToDependencies = new HashMap<>();

//        int numberOfMethodInClasses = 0; // for debug

        for(ClassEntity classEntity : allClasses) {
            nameToClassEntity.put(classEntity.getName(), classEntity);
//            numberOfMethodInClasses += classEntity.getRelevantProperties().getMethods().size();
        }
//        LOGGER.info("The size of allMethods list: " + allMethods.size() + "\n The number of methods from allClasses: " + numberOfMethodInClasses );
        LOGGER.info("Calculating Dependencies for Method Entities...");

        for(MethodEntity methodEntity : allMethods) {
            if(methodEntity == null) {
                LOGGER.warn("There is a null Method Entity");
                continue;
            }
            nameToDependencies.put(methodEntity.getName(), new Dependencies(methodEntity, nameToClassEntity));
        }

        LOGGER.info("Finding refactorings...");
        for(MethodEntity curMethod : allMethods) {
            if(curMethod == null || !curMethod.isMovable())
                continue;
            LOGGER.info("Checking " + curMethod.getName());

            Dependencies curDependencies = nameToDependencies.get(curMethod.getName());
            ClassEntity curClass = nameToClassEntity.get(curMethod.getClassName());
            if(curDependencies.cardinality() < MIN_NUMBER_OF_DEPENDENCIES
                    || curClass.getRelevantProperties().getMethods().size() == 1 //because we calculate similarity between this method and all remaining in curClass
                    || isGetter(curMethod) //this methods are rarely implemented in the wrong classes
                    || isSetter(curMethod)) //todo: check if we need this check here and not in other place
                continue;
            double curSimilarity = calculateSimilarity(curMethod, curClass, nameToDependencies);
            ClassEntity bestClass = null;
            double bestClassSimilarity = curSimilarity;
            int numberOfPotentialClasses = 0;
            for(ClassEntity potentialClass : allClasses) {
                double potentialClassSimilarity = calculateSimilarity(curMethod, potentialClass, nameToDependencies);
                if (potentialClassSimilarity > curSimilarity) {
                    numberOfPotentialClasses++;
                    if(potentialClassSimilarity > bestClassSimilarity) {
                        bestClassSimilarity = potentialClassSimilarity;
                        bestClass = potentialClass;
                    }
                }
            }

            if(numberOfPotentialClasses < MIN_NUMBER_OF_CANDIDATE_CLASSES)
                continue;

            if(bestClass != null) {
                double diff = (bestClassSimilarity - curSimilarity)/bestClassSimilarity; //may be idk
                if(diff >= MIN_DIFF_BETWEEN_SIMILARITY_COEFF_PERS)
                    refactorings.add(new Refactoring(curMethod.getName(), bestClass.getName(), bestClassSimilarity, false)); //accuracy idk
            }
            //todo may be i should check weither it is possible to make this refactoring oops
        }
//        LOGGER.info("The size of allMethods list: " + allMethods.size() + "\n The number of methods from allClasses: " + numberOfMethodInClasses );
//        LOGGER.info("Dependencies not found for " + methodNamesWithNoDependencies.size() + " methods");
//        LOGGER.info("Here they are:");
//        for(String methodName : methodNamesWithNoDependencies) {
//            LOGGER.info(methodName);
//        }
//        methodNamesWithNoDependencies.clear();
        return refactorings;
    }

    private double calculateSimilarity(@NotNull MethodEntity methodEntity, @NotNull ClassEntity classEntity, Map<String, Dependencies> nameToDependencies) {
        double similarity = 0;
        Set<String> methodNames = classEntity.getRelevantProperties().getMethods();

        for(String curMethodName : methodNames) {
            if(!methodEntity.getPsiMethod().getModifierList().hasExplicitModifier("abstract")
                    && !methodEntity.getName().equals(curMethodName) )
                try {
                    similarity += methodSimilarity(nameToDependencies.get(methodEntity.getName()), nameToDependencies.get(curMethodName));
                }
                catch(IllegalArgumentException e) {
//                    if(nameToDependencies.get(methodEntity.getName()) == null) {
//                        methodNamesWithNoDependencies.add(methodEntity.getName());
//                    }
//                    if(nameToDependencies.get(curMethodName) == null) {
//                        methodNamesWithNoDependencies.add(curMethodName);
//                    }
                    LOGGER.warn(e.getMessage()
                            + "\n Error happened when trying to calculate similarity between "
                            + methodEntity.getName() + " and " + curMethodName);
                }
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
            all = new HashSet<>();

            //classes of methods that are called inside this method
            for(String methodName : methodForDependencies.getRelevantProperties().getMethods()) {
                if(!methodName.equals(methodForDependencies.getName())) //because it's somehow always there
                    all.add(methodName.substring(0, methodName.lastIndexOf('.')));
            }

            // classes of fields that the method accesses
            for(String fieldName : methodForDependencies.getRelevantProperties().getFields()) {
                try {
                    PsiType psiType = nameToClassEntity.get(fieldName.substring(0, fieldName.lastIndexOf('.'))).getPsiClass().findFieldByName(fieldName.substring(fieldName.lastIndexOf('.') + 1), false).getType();
                    if (!(psiType instanceof PsiPrimitiveType) || fromUtilOrLang(psiType.getCanonicalText()))
                        all.add(psiType.getCanonicalText());
                }
                catch(NullPointerException e) {
                    LOGGER.warn("Trouble with field " + fieldName + " in method " + methodForDependencies.getName());
                }
            }

            //return type
            PsiType returnType = methodForDependencies.getPsiMethod().getReturnType();
            try {
                if(!(returnType instanceof PsiPrimitiveType ||fromUtilOrLang(returnType.getCanonicalText())))
                    all.add(returnType.getCanonicalText());
            }
            catch (NullPointerException e) {
                LOGGER.warn("Cannot get name of return type of method " + methodForDependencies.getName());
            }



            //exceptions
            //idk check for internal exception handling may be
            PsiClassType[] referencedTypes = methodForDependencies.getPsiMethod().getThrowsList().getReferencedTypes();
            for(PsiClassType classType : referencedTypes) {
                if(!(fromUtilOrLang(classType.getCanonicalText())))
                    all.add(classType.getCanonicalText());
            }

            //annotations
            PsiAnnotation[] psiAnnotations = methodForDependencies.getPsiMethod().getModifierList().getAnnotations();
            for(PsiAnnotation psiAnnotation : psiAnnotations) {
                try {
                    if (!fromUtilOrLang(psiAnnotation.getQualifiedName()))
                        all.add(psiAnnotation.getQualifiedName());
                }
                catch(NullPointerException e) {
                    LOGGER.warn("Cannot get annotation name for method " + methodForDependencies.getName());
                }
            }

            //formal parameters
            for(PsiParameter  parameter: methodForDependencies.getPsiMethod().getParameterList().getParameters()) {
                if(!((parameter.getType() instanceof PsiPrimitiveType) || fromUtilOrLang(parameter.getType().getCanonicalText())))
                    all.add(parameter.getType().getCanonicalText());
            }


            methodForDependencies.getPsiMethod().accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitLocalVariable(PsiLocalVariable variable) {
                    super.visitLocalVariable(variable);
                    try {
                        if (!(variable.getType() instanceof PsiPrimitiveType || fromUtilOrLang(variable.getType().getCanonicalText())))
                            all.add(variable.getType().getCanonicalText());
                    }
                    catch(NullPointerException e) {
                        LOGGER.warn("Cannot get name for local variable in method " + methodForDependencies.getName());
                    }
                }
                @Override
                public void visitNewExpression(PsiNewExpression expression) {
                    super.visitNewExpression(expression);
                    try {
                        if (!(fromUtilOrLang(expression.getClassOrAnonymousClassReference().getQualifiedName())))
                            all.add(expression.getClassOrAnonymousClassReference().getQualifiedName());
                    }
                    catch(NullPointerException e) {
                        LOGGER.warn("Cannot get name for class in new expression for method " + methodForDependencies.getName());
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

        public int cardinality() {

            return all.size();
        }

        public int calculateIntersectionCardinality(@NotNull Dependencies depSnd) {

            Set<String> intersection = new HashSet<>(all);
            intersection.retainAll(depSnd.all);
            return intersection.size();
        }

    }

    private boolean isGetter (@NotNull MethodEntity methodEntity) {
        PsiMethod psiMethod = methodEntity.getPsiMethod();
        try {
            int numSt = psiMethod.getBody().getStatements().length;
            return psiMethod.getParameterList().getParametersCount() == 0 && numSt == 1;
        }
        catch(NullPointerException e) {
            LOGGER.warn("Cannot find out if method " + methodEntity.getName() + " is getter method. Assuming that it is not.");
            return false;
        }
    }

    private boolean isSetter(@NotNull MethodEntity methodEntity) {
        PsiMethod psiMethod = methodEntity.getPsiMethod();
        try {
            int numSt = psiMethod.getBody().getStatements().length;
            return psiMethod.getParameterList().getParametersCount() == 1 && numSt == 1;
        }
        catch (NullPointerException e) {
            LOGGER.warn("Cannot find out if method " + methodEntity.getName() + " is setter method. Assuming that it is not.");
            return false;
        }
    }

    private boolean fromUtilOrLang (String fullName) {
        return fullName.startsWith("java.lang.") || fullName.startsWith("java.util.");
    }
}
