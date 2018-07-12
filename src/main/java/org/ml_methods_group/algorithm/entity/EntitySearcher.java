package org.ml_methods_group.algorithm.entity;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.metrics.utils.MethodUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.properties.finder_strategy.FinderStrategy;
import org.ml_methods_group.algorithm.properties.finder_strategy.NewStrategy;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.utils.PSIUtil;

import java.util.*;

import static org.ml_methods_group.utils.PsiSearchUtil.getHumanReadableName;

/**
 * Extracts every {@link Entity} from {@link AnalysisScope} according to a {@link FinderStrategy} that is uses.
 * It is also responsible for relevant properties extraction.
 */
public class EntitySearcher {

    private static final Logger LOGGER = Logging.getLogger(EntitySearcher.class);

    private final Map<String, PsiClass> classForName = new HashMap<>();
    private final Map<PsiElement, Entity> entities = new HashMap<>();
    private final AnalysisScope scope;
    private final long startTime;
    private final FinderStrategy strategy;
    private final ProgressIndicator indicator;

    private EntitySearcher(AnalysisScope scope) {
        this.scope = scope;
        strategy = NewStrategy.getInstance();
        startTime = System.currentTimeMillis();
        if (ProgressManager.getInstance().hasProgressIndicator()) {
            indicator = ProgressManager.getInstance().getProgressIndicator();
        } else {
            indicator = new EmptyProgressIndicator();
        }
    }

    /**
     * Searches for entities within a given {@link AnalysisScope}. Each found entity will have a vector
     * of features derived from a given {@link MetricsRun}.
     *
     * @param scope a scope to search for entities in.
     * @param metricsRun this allows to obtain feature vector for each found entity.
     * @return different sets of entities encapsulated in {@link EntitySearchResult}.
     */
    public static EntitySearchResult analyze(AnalysisScope scope, MetricsRun metricsRun) {
        final EntitySearcher finder = new EntitySearcher(scope);
        return finder.runCalculations(metricsRun);
    }

    private EntitySearchResult runCalculations(MetricsRun metricsRun) {
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
        return prepareResult(metricsRun);
    }

    private EntitySearchResult prepareResult(MetricsRun metricsRun) {
        LOGGER.info("Preparing results...");
        final List<ClassEntity> classes = new ArrayList<>();
        final List<MethodEntity> methods = new ArrayList<>();
        final List<FieldEntity> fields = new ArrayList<>();
        final List<Entity> validEntities = new ArrayList<>();
        for (Entity entity : entities.values()) {
            indicator.checkCanceled();
            try {
                entity.calculateVector(metricsRun);
            } catch (Exception e) {
                LOGGER.warn("Failed to calculate vector for " + entity.getName());
                continue;
            }
            validEntities.add(entity);
            switch (entity.getCategory()) {
                case Class:
                    classes.add((ClassEntity) entity);
                    break;
                case Method:
                    methods.add((MethodEntity) entity);
                    break;
                default:
                    fields.add((FieldEntity) entity);
                    break;
            }
        }
        LOGGER.info("Properties calculated");
        LOGGER.info("Generated " + classes.size() + " class entities");
        LOGGER.info("Generated " + methods.size() + " method entities");
        LOGGER.info("Generated " + fields.size() + " field entities");
        return new EntitySearchResult(classes, methods, fields, System.currentTimeMillis() - startTime);
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
            classForName.put(getHumanReadableName(aClass), aClass);
            if (!strategy.acceptClass(aClass)) {
                return;
            }
            entities.put(aClass, new ClassEntity(aClass));
            super.visitClass(aClass);
        }

        @Override
        public void visitField(PsiField field) {
            if (!strategy.acceptField(field)) {
                return;
            }
            indicator.checkCanceled();
            entities.put(field, new FieldEntity(field));
            super.visitField(field);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            if (!strategy.acceptMethod(method)) {
                return;
            }
            indicator.checkCanceled();
            entities.put(method, new MethodEntity(method));
            super.visitMethod(method);
        }
    }

    private class PropertiesCalculator extends JavaRecursiveElementVisitor {
        private int propertiesCalculated = 0;

        private PsiMethod currentMethod;

        @Override
        public void visitFile(PsiFile file) {
            indicator.checkCanceled();
            if (strategy.acceptFile(file)) {
                super.visitFile(file);
            }
        }

        @Override
        public void visitClass(PsiClass aClass) {
            indicator.checkCanceled();
            final Entity entity = entities.get(aClass);
            if (entity == null) {
                super.visitClass(aClass);
                return;
            }
            final RelevantProperties classProperties = entity.getRelevantProperties();
            classProperties.addClass(aClass, strategy.getWeight(aClass, aClass));
            if (strategy.processSupers()) {
                for (PsiClass superClass : PSIUtil.getAllSupers(aClass)) {
                    if (superClass.isInterface()) {
                        classProperties.addClass(superClass, strategy.getWeight(aClass, superClass));
                    } else {
                        propertiesFor(superClass).ifPresent(p -> p.addClass(aClass, strategy.getWeight(superClass, aClass)));
                    }
                }
            }
            Arrays.stream(aClass.getMethods())
                    .filter(m -> isProperty(aClass, m))
                    .forEach(m -> classProperties.addNotOverrideMethod(m, strategy.getWeight(aClass, m)));
            Arrays.stream(aClass.getFields())
                    .filter(f -> isProperty(aClass, f))
                    .forEach(f -> classProperties.addField(f, strategy.getWeight(aClass, f)));
            reportPropertiesCalculated();
            super.visitClass(aClass);
        }

        private boolean isProperty(PsiClass aClass, PsiMember member) {
            return !(member instanceof PsiMethod && ((PsiMethod) member).isConstructor()) && (aClass.equals(member.getContainingClass()) || !MethodUtils.isPrivate(member));
        }

        @Contract("null -> false")
        private boolean isClassInProject(final @Nullable PsiClass aClass) {
            return aClass != null && classForName.containsKey(getHumanReadableName(aClass));
        }

        @Override
        public void visitMethod(PsiMethod method) {
            indicator.checkCanceled();
            final Entity entity = entities.get(method);
            if (entity == null) {
                super.visitMethod(method);
                return;

            }
            final RelevantProperties methodProperties = entity.getRelevantProperties();
            methodProperties.addNotOverrideMethod(method, strategy.getWeight(method, method));
            Optional.ofNullable(method.getContainingClass())
                    .ifPresent(c -> methodProperties.addClass(c, strategy.getWeight(method, c)));
            if (currentMethod == null) {
                currentMethod = method;
            }
            if (strategy.processSupers()) {
                PSIUtil.getAllSupers(method).stream()
                        .map(entities::get)
                        .filter(Objects::nonNull)
                        .forEach(superMethod -> superMethod
                                .getRelevantProperties()
                                .addOverrideMethod(method, strategy.getWeight(superMethod, method)));
            }
            reportPropertiesCalculated();
            super.visitMethod(method);
            if (currentMethod == method) {
                currentMethod = null;
            }
        }

        @Override
        public void visitReferenceExpression(PsiReferenceExpression expression) {
            indicator.checkCanceled();
            PsiElement element = expression.resolve();
            if (currentMethod != null && element instanceof PsiField
                    && isClassInProject(((PsiField) element).getContainingClass()) && strategy.isRelation(expression)) {
                final PsiField field = (PsiField) element;
                propertiesFor(currentMethod)
                        .ifPresent(p -> p.addField(field, strategy.getWeight(currentMethod, field)));
//                propertiesFor(field)
//                        .ifPresent(p -> p.addNotOverrideMethod(currentMethod, strategy.getWeight(field, currentMethod)));
                final PsiClass fieldClass = PsiUtil.resolveClassInType(field.getType());
                if (isClassInProject(fieldClass)) {
                    propertiesFor(currentMethod)
                            .ifPresent(p -> p.addClass(fieldClass, strategy.getWeight(currentMethod, fieldClass)));
                }
            }
            super.visitReferenceExpression(expression);
        }

        @Override
        public void visitField(PsiField field) {
            indicator.checkCanceled();
            final Entity entity = entities.get(field);
            if (entity == null) {
                super.visitField(field);
                return;
            }
            RelevantProperties fieldProperties = entity.getRelevantProperties();
            fieldProperties.addField(field, strategy.getWeight(field, field));
            final PsiClass containingClass = field.getContainingClass();
            if (containingClass != null) {
                fieldProperties.addClass(containingClass, strategy.getWeight(field, containingClass));
                final PsiClass fieldClass = PsiUtil.resolveClassInType(field.getType());
                if (isClassInProject(fieldClass)) {
                    entities.get(containingClass).getRelevantProperties().addClass(fieldClass, strategy.getWeight(containingClass, fieldClass));
                }
            }
            reportPropertiesCalculated();
            super.visitField(field);
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            indicator.checkCanceled();
            final PsiMethod called = expression.resolveMethod();
            final PsiClass usedClass = called != null ? called.getContainingClass() : null;
            if (currentMethod != null && called != null && isClassInProject(usedClass)
                    && strategy.isRelation(expression)) {
                propertiesFor(currentMethod)
                        .ifPresent(p -> {
                            p.addNotOverrideMethod(called, strategy.getWeight(currentMethod, called));
                            p.addClass(usedClass, strategy.getWeight(currentMethod, usedClass));
                        });
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

    private Optional<RelevantProperties> propertiesFor(PsiElement element) {
        return Optional.ofNullable(entities.get(element))
                .map(Entity::getRelevantProperties);
    }
}
