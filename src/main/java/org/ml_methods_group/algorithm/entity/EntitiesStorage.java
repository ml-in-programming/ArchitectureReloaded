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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This storage contains all code entities (classes, method, field...) that have passed
 * {@link EntitySearcher} filtering, i.e. considered to be relevant and useful for algorithms. Each
 * entity also stores its {@link RelevantProperties}.
 */
public class EntitiesStorage {
    private final @NotNull List<ClassEntity> classes;

    private final @NotNull List<MethodEntity> methods;

    private final @NotNull List<FieldEntity> fields;

    /**
     * Restores information from {@link EntitySearchResult}. {@link EntitySearchResult} is
     * considered to be old, something like {@link EntitySearchResult} should create
     * {@link EntitiesStorage} directly.
     *
     * @param entitySearchResult an object to retrieve information from.
     */
    public EntitiesStorage(final @NotNull EntitySearchResult entitySearchResult) {
        classes = entitySearchResult.getClasses()
            .stream()
            .map((it) -> new ClassEntity(
                (PsiClass) it.getPsiElement(),
                it.getRelevantProperties())
            ).collect(Collectors.toList());

        methods = entitySearchResult.getMethods()
            .stream()
            .map((it) -> new MethodEntity(
                (PsiMethod) it.getPsiElement(),
                it.getRelevantProperties())
            ).collect(Collectors.toList());

        fields = entitySearchResult.getFields()
            .stream()
            .map((it) -> new FieldEntity(
                (PsiField) it.getPsiElement(),
                it.getRelevantProperties())
            ).collect(Collectors.toList());
    }

    @NotNull
    public List<ClassEntity> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    @NotNull
    public List<MethodEntity> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    @NotNull
    public List<FieldEntity> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public static abstract class Entity {
        private final @NotNull RelevantProperties relevantProperties;

        public Entity(final @NotNull RelevantProperties relevantProperties) {
            this.relevantProperties = relevantProperties;
        }

        /**
         * Returns identifier of this entity, usually its qualified name. The main reason entity has
         * this method is that in MetricsReloaded plugin metrics computation result is stored in
         * {@link Map} with {@link String} keys not {@link com.intellij.psi.PsiElement}. Therefore
         * one need to have an ability to obtain identifier for entity if he wants to get metrics
         * for this entity.
         */
        public abstract @NotNull String getIdentifier();

        public abstract @NotNull MetricCategory getMetricCategory();

        public @NotNull RelevantProperties getRelevantProperties() {
            return relevantProperties;
        }
    }

    public static class ClassEntity extends Entity {
        private final @NotNull PsiClass psiClass;

        public ClassEntity(
            final @NotNull PsiClass psiClass,
            final @NotNull RelevantProperties relevantProperties
        ) {
            super(relevantProperties);
            this.psiClass = psiClass;
        }

        @Override
        public @NotNull String getIdentifier() {
            return psiClass.getQualifiedName();
        }

        @Override
        public @NotNull MetricCategory getMetricCategory() {
            return MetricCategory.Class;
        }

        public @NotNull PsiClass getPsiClass() {
            return psiClass;
        }
    }

    public static class MethodEntity extends Entity {
        private final @NotNull PsiMethod psiMethod;

        public MethodEntity(
            final @NotNull PsiMethod psiMethod,
            final @NotNull RelevantProperties relevantProperties
        ) {
            super(relevantProperties);
            this.psiMethod = psiMethod;
        }

        @Override
        public @NotNull String getIdentifier() {
            return MethodUtils.calculateSignature(psiMethod);
        }

        @Override
        public @NotNull MetricCategory getMetricCategory() {
            return MetricCategory.Method;
        }

        public @NotNull PsiMethod getPsiMethod() {
            return psiMethod;
        }
    }

    public static class FieldEntity extends Entity {
        private final @NotNull PsiField psiField;

        public FieldEntity(
            final @NotNull PsiField psiField,
            final @NotNull RelevantProperties relevantProperties
        ) {
            super(relevantProperties);
            this.psiField = psiField;
        }

        @Override
        public @NotNull String getIdentifier() {
            throw new UnsupportedOperationException(
                "Metrics reloaded doesn't support field metrics."
            );
        }

        @Override
        public @NotNull MetricCategory getMetricCategory() {
            throw new UnsupportedOperationException(
                "Metrics reloaded doesn't support field metrics."
            );
        }

        public @NotNull PsiField getPsiField() {
            return psiField;
        }
    }
}
