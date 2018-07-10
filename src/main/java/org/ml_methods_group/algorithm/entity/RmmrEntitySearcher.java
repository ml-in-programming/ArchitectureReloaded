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
import org.ml_methods_group.algorithm.properties.finder_strategy.RmmrStrategy;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.utils.IdentifierTokenizer;

import java.util.*;

import static com.google.common.math.DoubleMath.log2;

/**
 * Implementation of {@link Entity} searcher for RMMR algorithm.
 */
public class RmmrEntitySearcher {
    private static final Logger LOGGER = Logging.getLogger(EntitySearcher.class);

    /**
     * Map: name of class -> {@link PsiClass} instance.
     */
    private final Map<String, PsiClass> classForName = new HashMap<>();
    /**
     * Map: {@link PsiMethod} instance -> corresponding {@link MethodEntity}.
     */
    private final Map<PsiMethod, MethodEntity> entities = new HashMap<>();
    /**
     * Map: {@link PsiClass} instance -> corresponding {@link ClassEntity}.
     */
    private final Map<PsiClass, ClassEntity> classEntities = new HashMap<>();
    private final Set<String> terms = new HashSet<>();
    private final Map<String, Double> idf = new HashMap<>();
    private final List<Collection<? extends Entity>> documents = Arrays.asList(classEntities.values(), entities.values());
    private final Stemmer stemmer = new Stemmer();
    /**
     * Scope where entities will be searched.
     */
    private final AnalysisScope scope;
    /**
     * Time when started search for entities.
     */
    private final long startTime = System.currentTimeMillis();
    /**
     * Strategy: which classes, methods and etc. to accept. For details see {@link RmmrStrategy}.
     */
    private final RmmrStrategy strategy = RmmrStrategy.getInstance();
    {
        strategy.setAcceptPrivateMethods(true);
        strategy.setAcceptMethodParams(false);
        strategy.setAcceptNewExpressions(false);
        strategy.setAcceptInnerClasses(true);
        strategy.setApplyStemming(true);
        strategy.setMinimalTermLength(3);
        strategy.setCheckPsiVariableForBeingInScope(true);
    }
    /**
     * UI progress indicator.
     */
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
        scope.accept(new BagsFinder());
        calculateStatistic();
        indicator.setIndeterminate(false);
        LOGGER.info("Calculating properties...");
        indicator.setText("Calculating properties");
        scope.accept(new PropertiesCalculator());
        indicator.popState();
        return prepareResult();
    }

    private void calculateStatistic() {
        calculateTf();
        calculateIdf();
        calculateTfIdf();
    }

    private void calculateTfIdf() {
        for (Collection<? extends Entity> partOfDocuments : documents) {
            for (Entity document : partOfDocuments) {
                document.getContextualVector().replaceAll((term, normalizedTf) -> normalizedTf * idf.get(term));
            }
        }
    }

    private void calculateIdf() {
        long N = classEntities.size();
        for (String term : terms) {
            long tfInAllClasses = classEntities.values().stream().
                    filter(classEntity -> classEntity.getBag().contains(term)).count();
            idf.put(term, log2((double) N / tfInAllClasses));
        }
    }

    private void calculateTf() {
        for (Collection<? extends Entity> partOfDocuments : documents) {
            for (Entity document : partOfDocuments) {
                Multiset<String> bag = document.getBag();
                for (Multiset.Entry<String> term : bag.entrySet()) {
                    document.getContextualVector().put(term.getElement(), 1 + log2(term.getCount()));
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
        final List<ClassEntity> classes = new ArrayList<>();
        final List<MethodEntity> methods = new ArrayList<>();
        for (MethodEntity methodEntity : entities.values()) {
            indicator.checkCanceled();
            methods.add(methodEntity);
        }
        for (ClassEntity classEntity : classEntities.values()) {
            indicator.checkCanceled();
            classes.add(classEntity);
        }
        LOGGER.info("Properties calculated");
        LOGGER.info("Generated " + classes.size() + " class entities");
        LOGGER.info("Generated " + methods.size() + " method entities");
        LOGGER.info("Generated " + 0 + " field entities. Fields are not supported.");
        return new EntitySearchResult(classes, methods, Collections.emptyList(), System.currentTimeMillis() - startTime);
    }

    private class BagsFinder extends JavaRecursiveElementVisitor {
        /**
         * Current method: if not null then we are parsing this method now and we need to update conceptual set of this method.
         */
        private MethodEntity currentMethod;
        final private Deque<ClassEntity> currentClasses = new ArrayDeque<>();

        private void addIdentifierToBag(@Nullable Entity entity, String identifier) {
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
                entity.getBag().addAll(terms);
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
            final ClassEntity classEntity = classEntities.get(aClass);
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
            final MethodEntity methodEntity = entities.get(method);
            if (methodEntity == null) {
                super.visitMethod(method);
                return;
            }
            if (currentMethod == null) {
                currentMethod = methodEntity;
            }
            addIdentifierToBag(currentMethod, method.getName());
            super.visitMethod(method);
            if (currentMethod == methodEntity) {
                currentMethod = null;
            }
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
            }
            super.visitReferenceExpression(expression);
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            indicator.checkCanceled();
            final PsiMethod called = expression.resolveMethod();
            final PsiClass usedClass = called != null ? called.getContainingClass() : null;
            if (isClassInScope(usedClass)) {
                addIdentifierToBag(currentClasses.peek(), called.getName());
                addIdentifierToBag(currentMethod, called.getName());
            }
            super.visitMethodCallExpression(expression);
        }
    }

    /**
     * Finds all units (classes, methods and etc.) in the scope based on {@link RmmrStrategy} that will be considered in searching process.
     */
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
            classEntities.put(aClass, new ClassEntity(aClass)); // Classes where method can be moved.
            super.visitClass(aClass);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            indicator.checkCanceled();
            if (!strategy.acceptMethod(method)) {
                return;
            }
            entities.put(method, new MethodEntity(method));
            super.visitMethod(method);
        }
    }


    /**
     * Calculates conceptual sets for all methods found by {@link UnitsFinder}.
     */
    // TODO: calculate properties for constructors? If yes, then we need to separate methods to check on refactoring (entities) and methods for calculating metric (to gather properties).
    private class PropertiesCalculator extends JavaRecursiveElementVisitor {
        private int propertiesCalculated = 0;
        /**
         * Current method: if not null then we are parsing this method now and we need to update conceptual set of this method.
         */
        private MethodEntity currentMethod;

        @Override
        public void visitMethod(PsiMethod method) {
            indicator.checkCanceled();
            final MethodEntity methodEntity = entities.get(method);
            if (methodEntity == null) {
                super.visitMethod(method);
                return;
            }
            if (currentMethod == null) {
                currentMethod = methodEntity;
            }

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
            if (expressionElement instanceof PsiField) {
                PsiField attribute = (PsiField) expressionElement;
                final PsiClass attributeClass = attribute.getContainingClass();
                if (currentMethod != null && isClassInScope(attributeClass)) {
                    currentMethod.getRelevantProperties().addClass(attributeClass);
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
            indicator.checkCanceled();
            final PsiMethod called = expression.resolveMethod();
            final PsiClass usedClass = called != null ? called.getContainingClass() : null;
            if (currentMethod != null && isClassInScope(usedClass)) {
                currentMethod.getRelevantProperties().addClass(usedClass);
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