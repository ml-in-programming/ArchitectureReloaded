package org.ml_methods_group.algorithm.entity;

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

import java.util.*;

public class RmmrEntitySearcher {
    private static final Logger LOGGER = Logging.getLogger(EntitySearcher.class);

    private final Map<String, PsiClass> classForName = new HashMap<>();
    private final Map<PsiMethod, MethodEntity> entities = new HashMap<>();
    private final Map<PsiClass, ClassEntity> classEntities = new HashMap<>();
    private final AnalysisScope scope;
    private final long startTime;
    private final FinderStrategy strategy;
    private final ProgressIndicator indicator;

    private RmmrEntitySearcher(AnalysisScope scope) {
        this.scope = scope;
        strategy = RmmrStrategy.getInstance();
        startTime = System.currentTimeMillis();
        if (ProgressManager.getInstance().hasProgressIndicator()) {
            indicator = ProgressManager.getInstance().getProgressIndicator();
        } else {
            indicator = new EmptyProgressIndicator();
        }
    }

    @NotNull
    public static EntitySearchResult analyze(AnalysisScope scope) {
        final RmmrEntitySearcher finder = new RmmrEntitySearcher(scope);
        return finder.runCalculations();
    }

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
        indicator.popState();
        return prepareResult();
    }

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


    private class UnitsFinder extends JavaRecursiveElementVisitor {
        @Override
        public void visitFile(PsiFile file) {
            indicator.checkCanceled();
            if (strategy.acceptFile(file)) {
                LOGGER.info("Indexing " + file.getName());
                super.visitFile(file);
            }
        }

        @Override
        public void visitClass(PsiClass aClass) {
            indicator.checkCanceled();
            //classForName.put(getHumanReadableName(aClass), aClass);
            //TODO: maybe qualified name? Otherwise name collision may occur.
            classForName.put(aClass.getName(), aClass);
            if (!strategy.acceptClass(aClass)) {
                return;
            }
            classEntities.put(aClass, new ClassEntity(aClass));
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


    // TODO: calculate properties for constructors? If yes, then we need to separate methods to check on refactoring (entities) and methods for calculating metric (to gather properties).
    private class PropertiesCalculator extends JavaRecursiveElementVisitor {
        private int propertiesCalculated = 0;

        private MethodEntity currentMethod;

        @Override
        public void visitMethod(PsiMethod method) {
            indicator.checkCanceled();
            final MethodEntity methodEntity = entities.get(method);
            if (methodEntity == null) {
                super.visitMethod(method);
                return;
            }
            final RelevantProperties methodProperties = methodEntity.getRelevantProperties();
            if (currentMethod == null) {
                currentMethod = methodEntity;
            }

            for (PsiParameter attribute : method.getParameterList().getParameters()) {
                PsiType attributeType = attribute.getType();
                if (attributeType instanceof PsiClassType) {
                    String className = ((PsiClassType) attributeType).getClassName();
                    if (isClassInScope(className)) {
                        methodProperties.addClass(classForName.get(className));
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
        public void visitNewExpression(PsiNewExpression expression) {
            indicator.checkCanceled();
            String className = null;
            PsiType type = expression.getType();
            if (type instanceof PsiClassType) {
                className = ((PsiClassType) expression.getType()).getClassName();
            }
            final PsiClass usedClass = classForName.get(className);
            if (currentMethod != null && className != null && isClassInScope(usedClass)) {
                currentMethod.getRelevantProperties().addClass(usedClass);
            }
            super.visitNewExpression(expression);
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            // Do not find constructors. It does not consider them as method calls.
            indicator.checkCanceled();
            final PsiMethod called = expression.resolveMethod();
            final PsiClass usedClass = called != null ? called.getContainingClass() : null;
            if (currentMethod != null && called != null && isClassInScope(usedClass)) {
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

        @Contract(pure = true)
        private boolean isClassInScope(String aClass) {
            return classForName.containsKey(aClass);
        }

        @Contract("null -> false")
        private boolean isClassInScope(final @Nullable PsiClass aClass) {
            return aClass != null && classForName.containsKey(aClass.getName());
        }
    }
}
