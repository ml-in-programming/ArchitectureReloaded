package org.ml_methods_group.algorithm;

import com.intellij.analysis.AnalysisScope;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.attributes.AttributesStorage;
import org.ml_methods_group.algorithm.attributes.ElementAttributes;
import org.ml_methods_group.algorithm.entity.*;
import org.ml_methods_group.algorithm.refactoring.Refactoring;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This class is a proxy for old algorithms. It shouldn't be used to create new algorithms.
 * {@link AbstractAlgorithm} should be used directly.
 */
@Deprecated
public abstract class OldAlgorithm extends AbstractAlgorithm {
    public OldAlgorithm(String name, boolean enableParallelExecution) {
        super(name, enableParallelExecution);
    }

    protected @NotNull Executor setUpExecutor() {
        return (context, enableFieldRefactorings) ->
                calculateRefactorings(new OldExecutionContext(context), enableFieldRefactorings);
    }

    protected abstract List<Refactoring> calculateRefactorings(
        OldExecutionContext context,
        boolean enableFieldRefactorings
    ) throws Exception;

    protected final class OldExecutionContext {
        private final @NotNull ExecutionContext context;

        private final @NotNull EntitySearchResult entities;

        private final @NotNull AnalysisScope scope;

        private OldExecutionContext(final @NotNull ExecutionContext context) {
            this.context = context;

            try {
                Field field = context.getClass().getDeclaredField("scope");
                boolean accessibility = field.isAccessible();
                field.setAccessible(true);

                this.scope = (AnalysisScope) field.get(context);

                field.setAccessible(accessibility);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to access existing field. Backward compatibility code.");
            }

            AttributesStorage attributes = context.getAttributesStorage();

            List<ClassOldEntity> classes =
                attributes.getClassesAttributes()
                    .stream()
                    .map(it -> {
                        ClassOldEntity entity = new ClassOldEntity(it.getOriginalClass().getPsiClass());
                        setFields(entity, it);
                        return entity;
                    })
                    .collect(Collectors.toList());

            List<MethodOldEntity> methods =
                attributes.getMethodsAttributes()
                    .stream()
                    .map(it -> {
                        MethodOldEntity entity = new MethodOldEntity(it.getOriginalMethod().getPsiMethod());
                        setFields(entity, it);
                        return entity;
                    })
                    .collect(Collectors.toList());

            List<FieldOldEntity> fields =
                attributes.getFieldsAttributes()
                    .stream()
                    .map(it -> {
                        FieldOldEntity entity = new FieldOldEntity(it.getOriginalField().getPsiField());
                        setFields(entity, it);
                        return entity;
                    })
                    .collect(Collectors.toList());

            this.entities = new EntitySearchResult(classes, methods, fields, 0);
        }

        @NotNull
        public AnalysisScope getScope() {
            return scope;
        }

        public @NotNull EntitySearchResult getEntities() {
            return entities;
        }

        public void checkCanceled() {
            context.checkCanceled();
        }

        public void reportProgress(double progress) {
            context.reportProgress(progress);
        }

        public final <A, V> A runParallel(
            List<V> values,
            Supplier<A> accumulatorFactory,
            BiFunction<V, A, A> processor,
            BinaryOperator<A> combiner
        ) {
            return context.runParallel(values, accumulatorFactory, processor, combiner);
        }

        private void setFields(OldEntity entity, ElementAttributes attributes) {
            try {
                Field relevantPropertiesField =
                        OldEntity.class.getDeclaredField("relevantProperties");

                boolean accessible = relevantPropertiesField.isAccessible();
                relevantPropertiesField.setAccessible(true);

                relevantPropertiesField.set(entity, attributes.getRelevantProperties());
                relevantPropertiesField.setAccessible(accessible);

                Field vectorField =
                        OldEntity.class.getDeclaredField("vector");

                accessible = vectorField.isAccessible();
                vectorField.setAccessible(true);

                vectorField.set(entity, attributes.getRawFeatures());
                vectorField.setAccessible(accessible);
            } catch (NoSuchFieldException | IllegalAccessException e) {
            }
        }
    }
}
