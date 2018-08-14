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
import org.jetbrains.research.groups.ml_methods.logging.Logging;
import org.jetbrains.research.groups.ml_methods.utils.PSIUtil;

import java.util.*;

import static org.jetbrains.research.groups.ml_methods.utils.PsiSearchUtil.getHumanReadableName;

/**
 * Extracts every {@link CodeEntity} from {@link AnalysisScope} according to a {@link FinderStrategy} that is uses.
 * It is also responsible for relevant properties extraction.
 */
public class EntitySearcher {

    private static final Logger LOGGER = Logging.getLogger(EntitySearcher.class);

    private final Map<String, PsiClass> classForName = new HashMap<>();

    final List<ClassEntity> classes = new ArrayList<>();
    final List<MethodEntity> methods = new ArrayList<>();
    final List<FieldEntity> fields = new ArrayList<>();

    final Map<PsiClass, ClassEntity> classEntities = new HashMap<>();
    final Map<PsiMethod, MethodEntity> methodEntities = new HashMap<>();
    final Map<PsiField, FieldEntity> fieldEntities = new HashMap<>();

    private final AnalysisScope scope;
    private final long startTime;
    private final FinderStrategy strategy;
    private final ProgressIndicator indicator;

    private ClassEntity getCodeEntity(final @NotNull PsiClass psiClass) {
        return classEntities.computeIfAbsent(psiClass, ClassEntity::new);
    }

    private MethodEntity getCodeEntity(final @NotNull PsiMethod psiMethod) {
        return methodEntities.computeIfAbsent(psiMethod, psi -> new MethodEntity(psi, getCodeEntity(psi.getContainingClass())));
    }

    private FieldEntity getCodeEntity(final @NotNull PsiField psiField) {
        return fieldEntities.computeIfAbsent(psiField, psi -> new FieldEntity(psi, getCodeEntity(psi.getContainingClass())));
    }

    private void addCodeEntityFor(final @NotNull PsiClass psiClass) {
        classes.add(getCodeEntity(psiClass));
    }

    private void addCodeEntityFor(final @NotNull PsiMethod psiMethod) {
        methods.add(getCodeEntity(psiMethod));
    }

    private void addCodeEntityFor(final @NotNull PsiField psiField) {
        fields.add(getCodeEntity(psiField));
    }

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
     * @return different sets of entities encapsulated in {@link EntitiesStorage}.
     */
    public static EntitiesStorage analyze(AnalysisScope scope) {
        final EntitySearcher finder = new EntitySearcher(scope);
        return finder.runCalculations();
    }

    private EntitiesStorage runCalculations() {
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

    private EntitiesStorage prepareResult() {
        LOGGER.info("Preparing results...");

        LOGGER.info("Generated " + classes.size() + " class entities");
        LOGGER.info("Generated " + methods.size() + " method entities");
        LOGGER.info("Generated " + fields.size() + " field entities");
        return new EntitiesStorage(classes, methods, fields, System.currentTimeMillis() - startTime);
    }

    private Optional<RelevantProperties> propertiesFor(PsiClass element) {
        CodeEntity entity = classEntities.get(element);

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(entity.getRelevantProperties());
    }

    private Optional<RelevantProperties> propertiesFor(PsiMethod element) {
        CodeEntity entity = methodEntities.get(element);

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(entity.getRelevantProperties());
    }

    private Optional<RelevantProperties> propertiesFor(PsiField element) {
        CodeEntity entity = fieldEntities.get(element);

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(entity.getRelevantProperties());
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

            addCodeEntityFor(aClass);
            super.visitClass(aClass);
        }

        @Override
        public void visitField(PsiField field) {
            if (!strategy.acceptField(field)) {
                return;
            }
            indicator.checkCanceled();

            addCodeEntityFor(field);
            super.visitField(field);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            if (!strategy.acceptMethod(method)) {
                return;
            }
            indicator.checkCanceled();

            addCodeEntityFor(method);
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
            final CodeEntity entity = getCodeEntity(aClass);
            if (entity == null) {
                super.visitClass(aClass);
                return;
            }
            final RelevantProperties classProperties = entity.getRelevantProperties();
            classProperties.addClass(getCodeEntity(aClass), strategy.getWeight(aClass, aClass));
            if (strategy.processSupers()) {
                for (PsiClass superClass : PSIUtil.getAllSupers(aClass)) {
                    if (superClass.isInterface()) {
                        classProperties.addClass(getCodeEntity(superClass), strategy.getWeight(aClass, superClass));
                    } else {
                        propertiesFor(superClass).ifPresent(p -> p.addClass(getCodeEntity(aClass), strategy.getWeight(superClass, aClass)));
                    }
                }
            }
            Arrays.stream(aClass.getMethods())
                    .filter(m -> isProperty(aClass, m))
                    .forEach(m -> classProperties.addNotOverrideMethod(getCodeEntity(m), strategy.getWeight(aClass, m)));
            Arrays.stream(aClass.getFields())
                    .filter(f -> isProperty(aClass, f))
                    .forEach(f -> classProperties.addField(getCodeEntity(f), strategy.getWeight(aClass, f)));
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
            final CodeEntity entity = getCodeEntity(method);
            if (entity == null) {
                super.visitMethod(method);
                return;

            }
            final RelevantProperties methodProperties = entity.getRelevantProperties();
            methodProperties.addNotOverrideMethod(getCodeEntity(method), strategy.getWeight(method, method));
            Optional.ofNullable(method.getContainingClass())
                    .ifPresent(c -> methodProperties.addClass(getCodeEntity(c), strategy.getWeight(method, c)));
            if (currentMethod == null) {
                currentMethod = method;
            }
            if (strategy.processSupers()) {
                PSIUtil.getAllSupers(method).stream()
                        .map(EntitySearcher.this::getCodeEntity)
                        .filter(Objects::nonNull)
                        .forEach(superMethodBuilder -> superMethodBuilder
                                .getRelevantProperties()
                                .addOverrideMethod(getCodeEntity(method), strategy.getWeight(superMethodBuilder, method)));
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
                        .ifPresent(p -> p.addField(getCodeEntity(field), strategy.getWeight(currentMethod, field)));
//                propertiesFor(field)
//                        .ifPresent(p -> p.addNotOverrideMethod(currentMethod, strategy.getWeight(field, currentMethod)));
                final PsiClass fieldClass = PsiUtil.resolveClassInType(field.getType());
                if (isClassInProject(fieldClass)) {
                    propertiesFor(currentMethod)
                            .ifPresent(p -> p.addClass(getCodeEntity(fieldClass), strategy.getWeight(currentMethod, fieldClass)));
                }
            }
            super.visitReferenceExpression(expression);
        }

        @Override
        public void visitField(PsiField field) {
            indicator.checkCanceled();
            final CodeEntity entity = getCodeEntity(field);
            if (entity == null) {
                super.visitField(field);
                return;
            }
            RelevantProperties fieldProperties = entity.getRelevantProperties();
            fieldProperties.addField(getCodeEntity(field), strategy.getWeight(field, field));
            final PsiClass containingClass = field.getContainingClass();
            if (containingClass != null) {
                fieldProperties.addClass(getCodeEntity(containingClass), strategy.getWeight(field, containingClass));
                final PsiClass fieldClass = PsiUtil.resolveClassInType(field.getType());
                if (isClassInProject(fieldClass)) {
                    getCodeEntity(containingClass).getRelevantProperties().addClass(getCodeEntity(fieldClass), strategy.getWeight(containingClass, fieldClass));
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
                            p.addNotOverrideMethod(getCodeEntity(called), strategy.getWeight(currentMethod, called));
                            p.addClass(getCodeEntity(usedClass), strategy.getWeight(currentMethod, usedClass));
                        });
            }
            super.visitMethodCallExpression(expression);
        }

        private void reportPropertiesCalculated() {
            propertiesCalculated++;
            if (indicator != null) {
                indicator.setFraction((double) propertiesCalculated / (classes.size() + methods.size() + fields.size()));
            }
        }
    }
}
