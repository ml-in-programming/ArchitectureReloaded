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

package org.ml_methods_group.refactoring;

import com.intellij.analysis.AnalysisScope;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import com.sixrr.stockmetrics.i18n.StockMetricsBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.plugin.AutomaticRefactoringAction;
import org.ml_methods_group.utils.RefactoringUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RefactoringAnnotator implements Annotator {
    private static Set<Integer> hashes = new HashSet<>();
    public static final AutomaticRefactoringAction action = new AutomaticRefactoringAction();

    @Override
    public synchronized void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        final Project project = psiElement.getProject();
        MetricsProfileRepository.getInstance().
                setSelectedProfile(StockMetricsBundle.message("refactoring.metrics.profile.name"));

        final AnalysisScope scope = new AnalysisScope(project);

        if ((psiElement instanceof PsiFile)) {
            final Set<PsiFile> currentFiles = new HashSet<>();
            scope.accept(new JavaElementVisitor() {
                @Override
                public void visitJavaFile(PsiJavaFile file) {
                    super.visitJavaFile(file);
                    currentFiles.add(file);
                }
            });
            final Set<Integer> currentFilesText = currentFiles.stream()
                    .map(f -> f.getText().hashCode()).collect(Collectors.toSet());
            if (!hashes.equals(currentFilesText)) {
                action.analyzeSynchronously(project, scope);
                hashes = currentFilesText;
            }
        }
        setAnnotations(psiElement, annotationHolder, scope);
    }

    private static void setAnnotations(@NotNull PsiElement element,
                                       @NotNull AnnotationHolder annotationHolder,
                                       @NotNull AnalysisScope scope) {
        doRefactoringAnnotations(element, "CCDA", action.getRefactoringsCCDA(), annotationHolder, scope);
        doRefactoringAnnotations(element, "MRI", action.getRefactoringsMRI(), annotationHolder, scope);
        doRefactoringAnnotations(element, "AKMeans", action.getRefactoringsAKMeans(), annotationHolder, scope);
        doRefactoringAnnotations(element, "HAC", action.getRefactoringsHAC(), annotationHolder, scope);
        doRefactoringAnnotations(element, "ARI", action.getRefactoringsARI(), annotationHolder, scope);
    }

    private static void doRefactoringAnnotations(@NotNull PsiElement element,
                                                 @NotNull String algorithmName,
                                                 final Map<String, String> refactorings,
                                                 @NotNull AnnotationHolder holder,
                                                 @NotNull AnalysisScope scope) {
        if (refactorings == null || refactorings.isEmpty()) {
            return;
        }

        final String name = RefactoringUtil.getHumanReadableName(element);
        if (refactorings.containsKey(name)) {
            final Annotation annotation = holder.createWarningAnnotation(
                    getAnnotationPart(element),
                    String.format("Can be moved to %s (%s)",
                            refactorings.get(name), algorithmName));

            annotation.registerFix(new RefactorIntentionAction(algorithmName,
                    name, refactorings.get(name), scope));
        }
    }

    @NotNull
    private static TextRange getAnnotationPart(@Nullable PsiElement element) {
        if (element instanceof PsiMember) {
            final PsiIdentifier identifier = PsiTreeUtil.getChildOfType(element, PsiIdentifier.class);
            return identifier != null ? identifier.getTextRange() : TextRange.EMPTY_RANGE;
        }
        return TextRange.EMPTY_RANGE;
    }

}
