package org.ml_methods_group.utils;

import com.intellij.analysis.AnalysisScope;
import com.intellij.ide.util.EditorHelper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.sixrr.metrics.utils.MethodUtils.calculateSignature;

public class PsiSearchUtil {

    public static class SearchOptions<V> {
        Function<? super PsiElement, V> resultExtractor;
        AnalysisScope scope;
    }

    public static <V> Optional<V> findElement(String humanReadableName, AnalysisScope scope, Function<PsiElement, V> mapper) {
        final Map<String, V> result = findAllElements(Collections.singleton(humanReadableName), scope, mapper);
        return Optional.ofNullable(result.get(humanReadableName));
    }

    public static <V> Map<String, V> findAllElements(Set<String> names, AnalysisScope scope,
                                                     Function<PsiElement, V> mapper) {
        final SearchOptions<V> options = new SearchOptions<>();
        options.resultExtractor = mapper;
        options.scope = scope;
        return runSafeSearch(names, options);
    }

    public static Optional<PsiElement> findElement(String humanReadableName, AnalysisScope scope) {
        return findElement(humanReadableName, scope, Function.identity());
    }

    public static void openDefinition(String unit, AnalysisScope scope) {
        new Task.Backgroundable(scope.getProject(), "Search Definition"){
            private PsiElement result;

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                result = findElement(unit, scope).orElse(null);
            }

            @Override
            public void onSuccess() {
                if (result != null) {
                    EditorHelper.openInEditor(result);
                }
            }
        }.queue();
    }

    public static String getHumanReadableName(@Nullable PsiElement element) {
        if (element instanceof PsiMethod) {
            return calculateSignature((PsiMethod) element);
        } else if (element instanceof PsiClass) {
            if (element instanceof PsiAnonymousClass) {
                return getHumanReadableName(((PsiAnonymousClass) element).getBaseClassReference().resolve());
            }
            return ((PsiClass) element).getQualifiedName();
        } else if (element instanceof PsiField) {
            final PsiMember field = (PsiMember) element;
            return getHumanReadableName(field.getContainingClass()) + "." + field.getName();
        }
        return "???";
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
                final String currentKey = getHumanReadableName(aClass);
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
                final String currentKey = getHumanReadableName(method);
                if (keys.contains(currentKey)) {
                    final V value = options.resultExtractor.apply(method);
                    results.put(currentKey, value);
                }
            }

            @Override
            public void visitField(PsiField field) {
                super.visitField(field);
                final String currentKey = getHumanReadableName(field);
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
