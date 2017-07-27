/*
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
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

package org.ml_methods_group.utils;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.sixrr.metrics.utils.MethodUtils.calculateSignature;

public class PsiSearchUtil {
    private static <V> Function<Object, V> nullSupplier() {
        return x -> null;
    }

    public static class SearchOptions<K, V> {
        Function<? super PsiClass, ? extends K> classKeyExtractor = nullSupplier();
        Function<? super PsiMethod, ? extends K> methodKeyExtractor = nullSupplier();
        Function<? super PsiField, ? extends K> fieldKeyExtractor = nullSupplier();
        Function<? super PsiElement, V> resultExtractor;
        AnalysisScope scope;
    }

    public static <V> Optional<V> findElement(String humanReadableName, AnalysisScope scope, Function<PsiElement, V> mapper) {
        final Map<String, V> result = findAllElements(Collections.singleton(humanReadableName), scope, mapper);
        return Optional.ofNullable(result.get(humanReadableName));
    }

    public static <V> Map<String, V> findAllElements(Set<String> names, AnalysisScope scope,
                                                              Function<PsiElement, V> mapper) {
        final SearchOptions<String, V> options = new SearchOptions<>();
        options.classKeyExtractor = PsiSearchUtil::getHumanReadableName;
        options.methodKeyExtractor = PsiSearchUtil::getHumanReadableName;
        options.fieldKeyExtractor = PsiSearchUtil::getHumanReadableName;
        options.resultExtractor = mapper;
        options.scope = scope;
        return runSafeSearch(names, options);
    }

    public static Optional<PsiElement> findElement(String humanReadableName, AnalysisScope scope) {
        return findElement(humanReadableName, scope, Function.identity());
    }

    public static Optional<PsiMethod> findMethodByName(String name, AnalysisScope scope) {
        final SearchOptions<String, PsiMethod> options = new SearchOptions<>();
        options.methodKeyExtractor = PsiMethod::getName;
        options.resultExtractor = PsiMethod.class::cast;
        options.scope = scope;
        final Map<String, PsiMethod> result = runSafeSearch(Collections.singleton(name), options);
        return Optional.ofNullable(result.get(name));
    }

    public static String getHumanReadableName(@Nullable PsiElement element) {
        if (element instanceof PsiMethod) {
            return calculateSignature((PsiMethod) element);
        } else if (element instanceof PsiClass) {
            return ((PsiClass) element).getQualifiedName();
        } else if (element instanceof PsiField) {
            final PsiMember field = (PsiMember) element;
            return getHumanReadableName(field.getContainingClass()) + "." + field.getName();
        }
        return "???";
    }

    public static <K, V> Map<K, V> runSafeSearch(Set<K> keys, SearchOptions<K, V> options) {
        return ApplicationManager.getApplication()
                .runReadAction((Computable<Map<K, V>>) () -> runSearch(keys, options));
    }

    private static <K, V> Map<K, V> runSearch(Set<K> keys, SearchOptions<K, V> options) {
        final Map<K, V> results = new HashMap<>();
        options.scope.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitClass(PsiClass aClass) {
                super.visitClass(aClass);
                final K currentKey = options.classKeyExtractor.apply(aClass);
                if (keys.contains(currentKey)) {
                    final V value = options.resultExtractor.apply(aClass);
                    results.put(currentKey, value);
                }
            }


            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);
                final K currentKey = options.methodKeyExtractor.apply(method);
                if (keys.contains(currentKey)) {
                    final V value = options.resultExtractor.apply(method);
                    results.put(currentKey, value);
                }
            }

            @Override
            public void visitField(PsiField field) {
                super.visitField(field);
                final K currentKey = options.fieldKeyExtractor.apply(field);
                if (keys.contains(currentKey)) {
                    final V value = options.resultExtractor.apply(field);
                    results.put(currentKey, value);
                }
            }
        });
        return results;
    }
}
