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

package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import com.google.common.collect.Multiset;
import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.*;
import com.port.stemmer.Stemmer;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.properties.finder_strategy.RmmrStrategy;
import org.jetbrains.research.groups.ml_methods.config.Logging;
import org.jetbrains.research.groups.ml_methods.utils.IdentifierTokenizer;

import java.util.*;

import static com.google.common.math.DoubleMath.log2;

/** Implementation of {@link OldEntity} searcher for RMMR algorithm */
public class RmmrEntitySearcher {
    private static final Logger LOGGER = Logging.getLogger(EntitySearcher.class);
    private final Map<String, PsiClass> classForName = new HashMap<>();
    private final Map<PsiMethod, MethodOldEntity> entities = new HashMap<>();
    private final Map<PsiClass, ClassOldEntity> classEntities = new HashMap<>();
    /** Terms are all words for contextual distance, for example: methodWithName gives as three terms: method, with, name */
    private final Set<String> terms = new HashSet<>();
    /**
     * Uniqueness property of term in whole document system (only classes are documents here)
     * idf(term) = log_2(|D| / |{d \in D: t \in d}|)
     */
    private final Map<String, Double> idf = new HashMap<>();
    private final List<Collection<? extends OldEntity>> documents = Arrays.asList(classEntities.values(), entities.values());
    private final Stemmer stemmer = new Stemmer();
    /** Scope where entities will be searched */
    private final AnalysisScope scope;
    /** Time when started search for entities */
    private final long startTime = System.currentTimeMillis();
    /** Strategy: which classes, methods and etc. to accept. For details see {@link RmmrStrategy} */
    private final RmmrStrategy strategy = RmmrStrategy.getInstance();

    {
        strategy.setAcceptPrivateMethods(true);
        strategy.setAcceptMethodParams(true);
        strategy.setAcceptNewExpressions(true);
        strategy.setAcceptMethodReferences(true);
        strategy.setAcceptClassReferences(true);
        strategy.setAcceptInnerClasses(true);
        strategy.setApplyStemming(true);
        strategy.setMinimalTermLength(1);
        strategy.setCheckPsiVariableForBeingInScope(true);
    }

    /** UI progress indicator */
    private final ProgressIndicator indicator;

    /**
     * Constructor which initializes indicator, startTime and saves given scope.
     *
     * @param scope where to search for entities.
     */
    private RmmrEntitySearcher(AnalysisScope scope) {
        this.scope = scope;
        if (ProgressManager.getInstance().hasProgressIndicator()) {
            indicator = ProgressManager.getInstance().getProgressIndicator();
        } else {
            indicator = new EmptyProgressIndicator();
        }
    }

    /**
     * Finds and returns entities in given scope.
     * @param scope where to search.
     * @return search results described by {@link EntitySearchResult} object.
     */
    @NotNull
    public static EntitySearchResult analyze(AnalysisScope scope) {
        final RmmrEntitySearcher finder = new RmmrEntitySearcher(scope);
        return finder.runCalculations();
    }

    /**
     * Runs all calculations for searching.
     * @return search results described by {@link EntitySearchResult} object.
     */
    @NotNull
    private EntitySearchResult runCalculations() {
        indicator.pushState();
        indicator.setText("Searching entities");
        indicator.setIndeterminate(true);
        LOGGER.info("Indexing entities...");
        scope.accept(new UnitsFinder());
        indicator.setIndeterminate(false);
        LOGGER.info("Calculating properties...");
        indicator.setText("Calculating properties");
        scope.accept(new PropertiesCalculator());
        calculateContextualVectors();
        indicator.popState();
        return prepareResult();
    }

    private void calculateContextualVectors() {
        calculateTf();
        calculateIdf();
        calculateTfIdf();
    }

    private void calculateTfIdf() {
        for (Collection<? extends OldEntity> partOfDocuments : documents) {
            for (OldEntity document : partOfDocuments) {
                document.getRelevantProperties().getContextualVector().replaceAll((term, normalizedTf) -> normalizedTf * idf.get(term));
            }
        }
    }

    private void calculateIdf() {
        long N = classEntities.size();
        for (String term : terms) {
            long tfInAllClasses = classEntities.values().stream().
                    filter(classEntity -> classEntity.getRelevantProperties().getBag().contains(term)).count();
            idf.put(term, log2((double) N / tfInAllClasses));
        }
    }

    private void calculateTf() {
        for (Collection<? extends OldEntity> partOfDocuments : documents) {
            for (OldEntity document : partOfDocuments) {
                Multiset<String> bag = document.getRelevantProperties().getBag();
                for (Multiset.Entry<String> term : bag.entrySet()) {
                    document.getRelevantProperties().getContextualVector().put(term.getElement(), 1 + log2(term.getCount()));
                }
                terms.addAll(bag.elementSet());
            }
        }
    }

    /**
     * Creates {@link EntitySearchResult} instance based on found entities (sorts by classes, methods and etc.).
     * @return search results described by {@link EntitySearchResult} object.
     */
    @NotNull
    private EntitySearchResult prepareResult() {
        LOGGER.info("Preparing results...");
        final List<ClassOldEntity> classes = new ArrayList<>();
        final List<MethodOldEntity> methods = new ArrayList<>();
        for (MethodOldEntity methodEntity : entities.values()) {
            indicator.checkCanceled();
            methods.add(methodEntity);
        }
        for (ClassOldEntity classEntity : classEntities.values()) {
            indicator.checkCanceled();
            classes.add(classEntity);
        }
        LOGGER.info("Properties calculated");
        LOGGER.info("Generated " + classes.size() + " class entities");
        LOGGER.info("Generated " + methods.size() + " method entities");
        LOGGER.info("Generated " + 0 + " field entities. Fields are not supported.");
        return new EntitySearchResult(classes, methods, Collections.emptyList(), System.currentTimeMillis() - startTime);
    }

    /** Finds all units (classes, methods and etc.) in the scope based on {@link RmmrStrategy} that will be considered in searching process */
    private class UnitsFinder extends JavaRecursiveElementVisitor {
        @Override
        public void visitFile(PsiFile file) {
            indicator.checkCanceled();
            if (!strategy.acceptFile(file)) {
                return;
            }
            LOGGER.info("Indexing " + file.getName());
            super.visitFile(file);
        }

        @Override
        public void visitClass(PsiClass aClass) {
            indicator.checkCanceled();
            classForName.put(aClass.getQualifiedName(), aClass); // Classes for ConceptualSet.
            if (!strategy.acceptClass(aClass)) {
                return;
            }
            classEntities.put(aClass, new ClassOldEntity(aClass)); // Classes where method can be moved.
            super.visitClass(aClass);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            indicator.checkCanceled();
            if (!strategy.acceptMethod(method)) {
                return;
            }
            entities.put(method, new MethodOldEntity(method));
            super.visitMethod(method);
        }
    }


    /** Calculates conceptual sets and term bags for all methods and classes found by {@link UnitsFinder} */
    // TODO: calculate properties for constructors? If yes, then we need to separate methods to check on refactoring (entities) and methods for calculating metric (to gather properties).
    private class PropertiesCalculator extends JavaRecursiveElementVisitor {
        private int propertiesCalculated = 0;
        /** Stack of current classes (it has size more than 1 if we have nested classes), updates only the last bag */
        // TODO: maybe update all bags on stack?
        final private Deque<ClassOldEntity> currentClasses = new ArrayDeque<>();
        /** Current method: if not null then we are parsing this method now and we need to update conceptual set and term bag of this method */
        private MethodOldEntity currentMethod;

        private void addIdentifierToBag(@Nullable OldEntity entity, String identifier) {
            if (entity != null) {
                List<String> terms = IdentifierTokenizer.tokenize(identifier);
                terms.removeIf(s -> s.length() < strategy.getMinimalTermLength());
                if (strategy.isApplyStemming()) {
                    terms.replaceAll(s -> {
                        stemmer.add(s.toCharArray(), s.length());
                        stemmer.stem();
                        return stemmer.toString();
                    });
                }
                entity.getRelevantProperties().getBag().addAll(terms);
            }
        }

        @Override
        public void visitMethodReferenceExpression(PsiMethodReferenceExpression expression) {
            indicator.checkCanceled();
            if (strategy.isAcceptMethodReferences()) {
                PsiElement expressionElement = expression.resolve();
                if (expressionElement instanceof PsiMethod) {
                    processMethod((PsiMethod) expressionElement);
                }
            }
            super.visitMethodReferenceExpression(expression);
        }

        private void processMethod(@NotNull PsiMethod calledMethod) {
            final PsiClass usedClass = calledMethod.getContainingClass();
            if (isClassInScope(usedClass)) {
                addIdentifierToBag(currentClasses.peek(), calledMethod.getName());
                addIdentifierToBag(currentMethod, calledMethod.getName());
                /* Conceptual set part */
                if (currentMethod != null) {
                    currentMethod.getRelevantProperties().addClass(usedClass);
                }
            }
        }

        @Override
        public void visitVariable(PsiVariable variable) {
            indicator.checkCanceled();
            PsiClass variablesClass = null;
            String variableName = variable.getName();
            PsiType variableType = variable.getType();
            if (variableType instanceof PsiClassType) {
                variablesClass = ((PsiClassType) variableType).resolve();
            }
            // TODO: add support for arrays int[][][][][].
            if (isClassInScope(variablesClass) && variablesClass.getName() != null) {
                addIdentifierToBag(currentClasses.peek(), variablesClass.getName());
                addIdentifierToBag(currentMethod, variablesClass.getName());
            }
            addIdentifierToBag(currentClasses.peek(), variableName);
            addIdentifierToBag(currentMethod, variableName);
            super.visitVariable(variable);
        }

        @Override
        public void visitClass(PsiClass aClass) {
            indicator.checkCanceled();
            final ClassOldEntity classEntity = classEntities.get(aClass);
            if (classEntity == null) {
                super.visitClass(aClass);
                return;
            }
            if (currentClasses.size() == 0 || strategy.isAcceptInnerClasses()) {
                currentClasses.push(classEntity);
            }
            addIdentifierToBag(currentClasses.peek(), aClass.getName());
            super.visitClass(aClass);
            if (currentClasses.peek() == classEntity) {
                currentClasses.pop();
            }
        }

        @Override
        public void visitMethod(PsiMethod method) {
            indicator.checkCanceled();
            addIdentifierToBag(currentClasses.peek(), method.getName());
            final MethodOldEntity methodEntity = entities.get(method);
            if (methodEntity == null) {
                super.visitMethod(method);
                return;
            }
            if (currentMethod == null) {
                currentMethod = methodEntity;
            }
            addIdentifierToBag(currentMethod, method.getName());
            if (strategy.isAcceptMethodParams()) {
                for (PsiParameter attribute : method.getParameterList().getParameters()) {
                    PsiType attributeType = attribute.getType();
                    if (attributeType instanceof PsiClassType) {
                        PsiClass aClass = ((PsiClassType) attributeType).resolve();
                        if (isClassInScope(aClass)) {
                            currentMethod.getRelevantProperties().addClass(aClass);
                        }
                    }
                }
            }

            super.visitMethod(method);
            if (currentMethod == methodEntity) {
                currentMethod = null;
            }
            reportPropertiesCalculated();
        }

        @Override
        public void visitReferenceExpression(PsiReferenceExpression expression) {
            indicator.checkCanceled();
            final PsiElement expressionElement = expression.resolve();
            if (expressionElement instanceof PsiVariable) {
                boolean isInScope = !strategy.getCheckPsiVariableForBeingInScope() ||
                        (!(expressionElement instanceof PsiField) ||
                                isClassInScope(((PsiField) expressionElement).getContainingClass()));
                if (isInScope) {
                    addIdentifierToBag(currentClasses.peek(), ((PsiVariable) expressionElement).getName());
                    addIdentifierToBag(currentMethod, ((PsiVariable) expressionElement).getName());
                }
                /* Conceptual Set part */
                if (expressionElement instanceof PsiField) {
                    PsiField attribute = (PsiField) expressionElement;
                    final PsiClass attributeClass = attribute.getContainingClass();
                    if (currentMethod != null && isClassInScope(attributeClass)) {
                        currentMethod.getRelevantProperties().addClass(attributeClass);
                    }
                }
            }
            if (strategy.isAcceptClassReferences() && expressionElement instanceof PsiClass) {
                PsiClass aClass = (PsiClass) expressionElement;
                if (isClassInScope(aClass)) {
                    addIdentifierToBag(currentClasses.peek(), aClass.getName());
                    addIdentifierToBag(currentMethod, aClass.getName());
                }
            }
            super.visitReferenceExpression(expression);
        }

        @Override
        public void visitNewExpression(PsiNewExpression expression) {
            if (strategy.isAcceptNewExpressions()) {
                indicator.checkCanceled();
                PsiType type = expression.getType();
                PsiClass usedClass = type instanceof PsiClassType ? ((PsiClassType) type).resolve() : null;
                if (currentMethod != null && isClassInScope(usedClass)) {
                    currentMethod.getRelevantProperties().addClass(usedClass);
                }
            }
            super.visitNewExpression(expression);
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            final PsiMethod called = expression.resolveMethod();
            if (called != null) {
                processMethod(called);
            }
            super.visitMethodCallExpression(expression);
        }

        private void reportPropertiesCalculated() {
            propertiesCalculated++;
            if (indicator != null) {
                indicator.setFraction((double) propertiesCalculated / entities.size());
            }
        }
    }

    @Contract("null -> false")
    private boolean isClassInScope(final @Nullable PsiClass aClass) {
        return aClass != null && classForName.containsKey(aClass.getQualifiedName());
    }
}