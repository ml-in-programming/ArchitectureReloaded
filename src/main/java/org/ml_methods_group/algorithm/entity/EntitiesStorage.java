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
}
