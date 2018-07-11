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

import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.attributes.AttributesStorage;
import org.ml_methods_group.algorithm.attributes.ElementAttributes;
import org.ml_methods_group.algorithm.entity.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class OldAlgorithm extends AbstractAlgorithm {
    public OldAlgorithm(String name, boolean enableParallelExecution) {
        super(name, enableParallelExecution);
    }

    protected @NotNull
    Executor setUpExecutor() {
        return (context, enableFieldRefactorings) ->
                calculateRefactorings(new OldExecutionContext(context), enableFieldRefactorings);
    }

    protected abstract List<Refactoring> calculateRefactorings(
        OldExecutionContext context,
        boolean enableFieldRefactorings
    ) throws Exception;

    protected final class OldExecutionContext {
        private final @NotNull
        ExecutionContext context;

        private final @NotNull EntitySearchResult entities;

        private OldExecutionContext(final @NotNull ExecutionContext context) {
            this.context = context;

            AttributesStorage attributes = context.getAttributesStorage();

            List<ClassEntity> classes =
                attributes.getClassesAttributes()
                    .stream()
                    .map(it -> {
                        ClassEntity entity = new ClassEntity(it.getOriginalClass());
                        setFields(entity, it);
                        return entity;
                    })
                    .collect(Collectors.toList());

            List<MethodEntity> methods =
                attributes.getMethodsAttributes()
                    .stream()
                    .map(it -> {
                        MethodEntity entity = new MethodEntity(it.getOriginalMethod());
                        setFields(entity, it);
                        return entity;
                    })
                    .collect(Collectors.toList());

            List<FieldEntity> fields =
                attributes.getFieldsAttributes()
                    .stream()
                    .map(it -> {
                        FieldEntity entity = new FieldEntity(it.getOriginalField());
                        setFields(entity, it);
                        return entity;
                    })
                    .collect(Collectors.toList());

            this.entities = new EntitySearchResult(classes, methods, fields, 0);
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

        private void setFields(Entity entity, ElementAttributes attributes) {
            try {
                Field relevantPropertiesField =
                        Entity.class.getDeclaredField("relevantProperties");

                boolean accessible = relevantPropertiesField.isAccessible();
                relevantPropertiesField.setAccessible(true);

                relevantPropertiesField.set(entity, attributes.getRelevantProperties());
                relevantPropertiesField.setAccessible(accessible);

                Field vectorField =
                        Entity.class.getDeclaredField("vector");

                accessible = vectorField.isAccessible();
                vectorField.setAccessible(true);

                vectorField.set(entity, attributes.getRawFeatures());
                vectorField.setAccessible(accessible);
            } catch (NoSuchFieldException | IllegalAccessException e) {
            }
        }
    }
}
