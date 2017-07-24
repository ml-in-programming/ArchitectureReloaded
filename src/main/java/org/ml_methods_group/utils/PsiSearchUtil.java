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

import java.util.Objects;
import java.util.Optional;
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
        final SearchOptions<String, V> options = new SearchOptions<>();
        options.classKeyExtractor = PsiSearchUtil::getHumanReadableName;
        options.methodKeyExtractor = PsiSearchUtil::getHumanReadableName;
        options.fieldKeyExtractor = PsiSearchUtil::getHumanReadableName;
        options.resultExtractor = mapper;
        options.scope = scope;
        return runSafeSearch(humanReadableName, options);
    }

    public static Optional<PsiElement> findElement(String humanReadableName, AnalysisScope scope) {
        return findElement(humanReadableName, scope, Function.identity());
    }

    public static Optional<PsiMethod> findMethodByName(String name, AnalysisScope scope) {
        final SearchOptions<String, PsiMethod> options = new SearchOptions<>();
        options.methodKeyExtractor = PsiMethod::getName;
        options.resultExtractor = PsiMethod.class::cast;
        options.scope = scope;
        return runSafeSearch(name, options);
    }

    public static Optional<String> getElementText(String unit, AnalysisScope scope) {
        return findElement(unit, scope, PsiElement::getText);
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

    public static <K, V> Optional<V> runSafeSearch(K key, SearchOptions<K, V> options) {
        return ApplicationManager.getApplication()
                .runReadAction((Computable<Optional<V>>) () -> runSearch(key, options));
    }

    private static <K, V> Optional<V> runSearch(K key, SearchOptions<K, V> options) {
        final PsiElement[] resultHolder = new PsiElement[1];
        options.scope.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitClass(PsiClass aClass) {
                super.visitClass(aClass);
                if (Objects.equals(key, options.classKeyExtractor.apply(aClass))) {
                    resultHolder[0] = aClass;
                }
            }


            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);
                if (Objects.equals(key, options.methodKeyExtractor.apply(method))) {
                    resultHolder[0] = method;
                }
            }

            @Override
            public void visitField(PsiField field) {
                super.visitField(field);
                if (Objects.equals(key, options.fieldKeyExtractor.apply(field))) {
                    resultHolder[0] = field;
                }
            }
        });
        return Optional.ofNullable(resultHolder[0]).map(options.resultExtractor);
    }
}
