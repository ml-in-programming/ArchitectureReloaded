package org.jetbrains.research.groups.ml_methods.utils;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;

import java.util.*;
import java.util.function.Function;

public class PsiSearchUtil {

    private static class SearchOptions<V> {
        Function<? super PsiElement, V> resultExtractor;
        AnalysisScope scope;
    }

    private static <V> Optional<V> findElement(String humanReadableName, AnalysisScope scope, Function<PsiElement, V> mapper) {
        final Map<String, V> result = findAllElements(Collections.singleton(humanReadableName), scope, mapper);
        return Optional.ofNullable(result.get(humanReadableName));
    }

    private static <V> Map<String, V> findAllElements(Set<String> names, AnalysisScope scope,
                                                      Function<PsiElement, V> mapper) {
        final SearchOptions<V> options = new SearchOptions<>();
        options.resultExtractor = mapper;
        options.scope = scope;
        return runSafeSearch(names, options);
    }

    public static Optional<PsiElement> findElement(String humanReadableName, AnalysisScope scope) {
        return findElement(humanReadableName, scope, Function.identity());
    }

    private static <V> Map<String, V> runSafeSearch(Set<String> keys, SearchOptions<V> options) {
        return ApplicationManager.getApplication()
                .runReadAction((Computable<Map<String, V>>) () -> runSearch(keys, options));
    }

    private static <V> Map<String, V> runSearch(Set<String> keys, SearchOptions<V> options) {
        final Map<String, V> results = new HashMap<>();
        final Set<String> paths = paths(keys);
        options.scope.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitPackage(PsiPackage aPackage) {
                if (paths.contains(aPackage.getQualifiedName())) {
                    super.visitPackage(aPackage);
                }
            }

            @Override
            public void visitClass(PsiClass aClass) {
                final String currentKey = PSIUtil.getHumanReadableName(aClass);
                if (keys.contains(currentKey)) {
                    final V value = options.resultExtractor.apply(aClass);
                    results.put(currentKey, value);
                }
                if (paths.contains(currentKey)) {
                    super.visitClass(aClass);
                }
            }


            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);
                final String currentKey = PSIUtil.getHumanReadableName(method);
                if (keys.contains(currentKey)) {
                    final V value = options.resultExtractor.apply(method);
                    results.put(currentKey, value);
                }
            }

            @Override
            public void visitField(PsiField field) {
                super.visitField(field);
                final String currentKey = PSIUtil.getHumanReadableName(field);
                if (keys.contains(currentKey)) {
                    final V value = options.resultExtractor.apply(field);
                    results.put(currentKey, value);
                }
            }
        });
        return results;
    }

    private static Set<String> paths(Set<String> keys) {
        final Set<String> result = new HashSet<>();
        for (String key : keys) {
            for (int i = 0; i < key.length(); i++) {
                if (key.charAt(i) == '.') {
                    result.add(key.substring(0, i));
                }
            }
        }
        return result;
    }
}
