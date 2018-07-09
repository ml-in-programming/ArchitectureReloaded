package org.ml_methods_group.algorithm.entity;

import com.google.common.collect.Multiset;
import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.*;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.properties.finder_strategy.FinderStrategy;
import org.ml_methods_group.algorithm.properties.finder_strategy.RmmrStrategy;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.utils.IdentifierTokenizer;

import java.util.*;

import static com.google.common.math.DoubleMath.log2;

/**
 * Implementation of {@link Entity} searcher for RMMR algorithm.
 */
// TODO: decide if we consider or not private methods for dependency and contextual similarity.
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
    private final FinderStrategy strategy = RmmrStrategy.getInstance();
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
        int coordinate = 0;
        for (String term : terms) {
            double idfForTerm = idf.get(term);
            for (Collection<? extends Entity> partOfDocuments : documents) {
                for (Entity document : partOfDocuments) {
                    if (coordinate == 0) {
                        document.initStatisticVector(terms.size());
                    }
                    double tfForTermAndDocument = document.getNormalizedTf().getOrDefault(term, 0.0);
                    document.addStatistic(tfForTermAndDocument * idfForTerm, coordinate);
                }
            }
            coordinate++;
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
                    document.getNormalizedTf().put(term.getElement(), 1 + log2(term.getCount()));
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
                entity.getBag().addAll(IdentifierTokenizer.tokenize(identifier));
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
            if (isClassInScope(variablesClass) && variablesClass.getQualifiedName() != null) {
                addIdentifierToBag(currentClasses.peek(), variablesClass.getQualifiedName());
                addIdentifierToBag(currentMethod, variablesClass.getQualifiedName());
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
            currentClasses.push(classEntity);
            addIdentifierToBag(currentClasses.peek(), aClass.getName());
            super.visitClass(aClass);
            currentClasses.pop();
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
                /*
                // TODO: decide if this check is needed, could be guard from this: ClassNotInScope.VariableWithNoMeaningForContextInformation
                boolean isInScope = !(expressionElement instanceof PsiField)
                        || isClassInScope(((PsiField) expressionElement).getContainingClass());
                */
                addIdentifierToBag(currentClasses.peek(), ((PsiVariable) expressionElement).getName());
                addIdentifierToBag(currentMethod, ((PsiVariable) expressionElement).getName());
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
            //classForName.put(getHumanReadableName(aClass), aClass);
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

            /* Adding to Conceptual Set classes of method params.
            for (PsiParameter attribute : method.getParameterList().getParameters()) {
                PsiType attributeType = attribute.getType();
                if (attributeType instanceof PsiClassType) {
                    String className = ((PsiClassType) attributeType).getClassName();
                    if (isClassInScope(className)) {
                        methodProperties.addClass(classForName.get(className));
                    }
                }
            }
            */

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

            /* Puts classes of fields, not containing classes.
            indicator.checkCanceled();
            final PsiElement expressionElement = expression.resolve();
            if (expressionElement instanceof PsiField) {
                PsiType attributeType = ((PsiField) expressionElement).getType();
                if (attributeType instanceof PsiClassType) {
                    String attributeClass = ((PsiClassType) attributeType).getClassName();
                    if (currentMethod != null && isClassInScope(attributeClass)) {
                        currentMethod.getRelevantProperties().addClass(classForName.get(attributeClass));
                    }
                }
            }
            super.visitReferenceExpression(expression);
            */
        }

        /*
        @Override
        public void visitNewExpression(PsiNewExpression expression) {
            indicator.checkCanceled();
            PsiType type = expression.getType();
            PsiClass usedClass = type instanceof PsiClassType ? ((PsiClassType) type).resolve() : null;
            if (currentMethod != null && isClassInScope(usedClass)) {
                currentMethod.getRelevantProperties().addClass(usedClass);
            }
            super.visitNewExpression(expression);
        }
        */

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            // Does not find constructors (new expressions). It does not consider them as method calls.
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