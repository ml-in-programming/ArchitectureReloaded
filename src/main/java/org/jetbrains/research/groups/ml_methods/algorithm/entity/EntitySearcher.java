package org.jetbrains.research.groups.ml_methods.algorithm.entity;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.properties.finder_strategy.FinderStrategy;
import org.jetbrains.research.groups.ml_methods.algorithm.properties.finder_strategy.NewStrategy;
import org.jetbrains.research.groups.ml_methods.config.Logging;
import org.jetbrains.research.groups.ml_methods.utils.PSIUtil;

import java.util.*;

import static org.jetbrains.research.groups.ml_methods.utils.PsiSearchUtil.getHumanReadableName;

/**
 * Extracts every {@link OldEntity} from {@link AnalysisScope} according to a {@link FinderStrategy} that is uses.
 * It is also responsible for relevant properties extraction.
 */
public class EntitySearcher {

    private static final Logger LOGGER = Logging.getLogger(EntitySearcher.class);

    private final Map<String, PsiClass> classForName = new HashMap<>();
    private final Map<PsiElement, CodeEntityBuilder> builders = new HashMap<>();
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
     * @return different sets of entities encapsulated in {@link EntitiesStorage}.
     */
    public static EntitiesStorage analyze(AnalysisScope scope, MetricsRun metricsRun) {
        final EntitySearcher finder = new EntitySearcher(scope);
        return finder.runCalculations(metricsRun);
    }

    private EntitiesStorage runCalculations(MetricsRun metricsRun) {
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

    private EntitiesStorage prepareResult(MetricsRun metricsRun) {
        LOGGER.info("Preparing results...");
        final List<ClassEntity> classes = new ArrayList<>();
        final List<MethodEntity> methods = new ArrayList<>();
        final List<FieldEntity> fields = new ArrayList<>();
        for (CodeEntityBuilder builder : builders.values()) {
            indicator.checkCanceled();

            CodeEntity entity = builder.build();
            entity.accept(new CodeEntityVisitor<Void>() {
                @Override
                public Void visit(final @NotNull ClassEntity classEntity) {
                    classes.add(classEntity);
                    return null;
                }

                @Override
                public Void visit(final @NotNull MethodEntity methodEntity) {
                    methods.add(methodEntity);
                    return null;
                }

                @Override
                public Void visit(final @NotNull FieldEntity fieldEntity) {
                    fields.add(fieldEntity);
                    return null;
                }
            });
        }

        LOGGER.info("Properties calculated");
        LOGGER.info("Generated " + classes.size() + " class entities");
        LOGGER.info("Generated " + methods.size() + " method entities");
        LOGGER.info("Generated " + fields.size() + " field entities");
        return new EntitiesStorage(classes, methods, fields, System.currentTimeMillis() - startTime);
    }

    private Optional<RelevantProperties> propertiesFor(PsiElement element) {
        return Optional.ofNullable(builders.get(element))
                .map(CodeEntityBuilder::getRelevantProperties);
    }

    private class UnitsFinder extends JavaRecursiveElementVisitor {
        private @Nullable ClassEntityBuilder currentClassBuilder;

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

            ClassEntityBuilder previousClassEntityBuilder = currentClassBuilder;
            currentClassBuilder = new ClassEntityBuilder(aClass);
            builders.put(aClass, currentClassBuilder);
            super.visitClass(aClass);
            currentClassBuilder = previousClassEntityBuilder;
        }

        @Override
        public void visitField(PsiField field) {
            if (!strategy.acceptField(field)) {
                return;
            }
            indicator.checkCanceled();
            builders.put(field, new FieldEntityBuilder(field, currentClassBuilder));
            super.visitField(field);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            if (!strategy.acceptMethod(method)) {
                return;
            }
            indicator.checkCanceled();
            builders.put(method, new MethodEntityBuilder(method, currentClassBuilder));
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
            final CodeEntityBuilder builder = builders.get(aClass);
            if (builder == null) {
                super.visitClass(aClass);
                return;
            }
            final RelevantProperties classProperties = builder.getRelevantProperties();
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
                    .forEach(m -> classProperties.addMethod(m, strategy.getWeight(aClass, m)));
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
            final CodeEntityBuilder builder = builders.get(method);
            if (builder == null) {
                super.visitMethod(method);
                return;

            }
            final RelevantProperties methodProperties = builder.getRelevantProperties();
            methodProperties.addMethod(method, strategy.getWeight(method, method));
            Optional.ofNullable(method.getContainingClass())
                    .ifPresent(c -> methodProperties.addClass(c, strategy.getWeight(method, c)));
            if (currentMethod == null) {
                currentMethod = method;
            }
            if (strategy.processSupers()) {
                PSIUtil.getAllSupers(method).stream()
                        .map(builders::get)
                        .filter(Objects::nonNull)
                        .forEach(superMethodBuilder -> superMethodBuilder
                                .getRelevantProperties()
                                .addOverrideMethod(method, strategy.getWeight(superMethodBuilder, method)));
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
//                        .ifPresent(p -> p.addMethod(currentMethod, strategy.getWeight(field, currentMethod)));
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
            final CodeEntityBuilder builder = builders.get(field);
            if (builder == null) {
                super.visitField(field);
                return;
            }
            RelevantProperties fieldProperties = builder.getRelevantProperties();
            fieldProperties.addField(field, strategy.getWeight(field, field));
            final PsiClass containingClass = field.getContainingClass();
            if (containingClass != null) {
                fieldProperties.addClass(containingClass, strategy.getWeight(field, containingClass));
                final PsiClass fieldClass = PsiUtil.resolveClassInType(field.getType());
                if (isClassInProject(fieldClass)) {
                    builders.get(containingClass).getRelevantProperties().addClass(fieldClass, strategy.getWeight(containingClass, fieldClass));
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
                            p.addMethod(called, strategy.getWeight(currentMethod, called));
                            p.addClass(usedClass, strategy.getWeight(currentMethod, usedClass));
                        });
            }
            super.visitMethodCallExpression(expression);
        }

        private void reportPropertiesCalculated() {
            propertiesCalculated++;
            if (indicator != null) {
                indicator.setFraction((double) propertiesCalculated / builders.size());
            }
        }
    }

    private static abstract class CodeEntityBuilder {
        protected final @NotNull RelevantProperties relevantProperties = new RelevantProperties();

        public @NotNull RelevantProperties getRelevantProperties() {
            return relevantProperties;
        }

        public abstract @NotNull CodeEntity build();
    }

    private static class ClassEntityBuilder extends CodeEntityBuilder {
        private @Nullable ClassEntity result = null;

        private final @NotNull PsiClass psiClass;

        public ClassEntityBuilder(final @NotNull PsiClass psiClass) {
            this.psiClass = psiClass;
        }

        public @NotNull ClassEntity build() {
            if (result == null) {
                result = new ClassEntity(psiClass, relevantProperties);
            }

            return result;
        }
    }

    private static class MethodEntityBuilder extends CodeEntityBuilder {
        private @Nullable MethodEntity result = null;

        private final @NotNull PsiMethod psiMethod;

        private final @NotNull ClassEntityBuilder containingClassBuilder;

        public MethodEntityBuilder(
            final @NotNull PsiMethod psiMethod,
            final @NotNull ClassEntityBuilder containingClassBuilder
        ) {
            this.psiMethod = psiMethod;
            this.containingClassBuilder = containingClassBuilder;
        }

        public @NotNull MethodEntity build() {
            if (result == null) {
                result = new MethodEntity(psiMethod, containingClassBuilder.build(), relevantProperties);
            }

            return result;
        }
    }

    private static class FieldEntityBuilder extends CodeEntityBuilder {
        private @Nullable FieldEntity result = null;

        private final @NotNull PsiField psiField;

        private final @NotNull ClassEntityBuilder containingClassBuilder;

        public FieldEntityBuilder(
            final @NotNull PsiField psiField,
            final @NotNull ClassEntityBuilder containingClassBuilder
        ) {
            this.psiField = psiField;
            this.containingClassBuilder = containingClassBuilder;
        }

        public @NotNull FieldEntity build() {
            if (result == null) {
                if (relevantProperties == null) {
                    throw new IllegalStateException("Attempt to create entity without RelevantProperties");
                }

                result = new FieldEntity(psiField, containingClassBuilder.build(), relevantProperties);
            }

            return result;
        }
    }
}
