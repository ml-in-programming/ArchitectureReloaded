/*
 *  Copyright 2017 Machine Learning Methods in Software Engineering Research Group
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.sixrr.metrics.plugin;

import com.intellij.analysis.AnalysisScope;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import com.sixrr.metrics.utils.RefactoringUtil;
import com.sixrr.stockmetrics.i18n.StockMetricsBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RefactoringAnnotator implements Annotator {
    private final AutomaticRefactoringAction action = new AutomaticRefactoringAction();

    @Override
    public synchronized void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (!(psiElement instanceof PsiFile)) {
            return;
        }
        final Project project = psiElement.getProject();
        MetricsProfileRepository.getInstance().
                setSelectedProfile(StockMetricsBundle.message("refactoring.metrics.profile.name"));

        final AnalysisScope scope = new AnalysisScope(project);
        action.analyzeSynchronously(project, scope);

        doRefactoringAnnotations("CCDA", action.getRefactoringsCCDA(), annotationHolder, scope);
        doRefactoringAnnotations("MRI", action.getRefactoringsMRI(), annotationHolder, scope);
        doRefactoringAnnotations("AKMeans", action.getRefactoringsAKMeans(), annotationHolder, scope);
        doRefactoringAnnotations("HAC", action.getRefactoringsHAC(), annotationHolder, scope);
        doRefactoringAnnotations("ARI", action.getRefactoringsARI(), annotationHolder, scope);
    }

    private static void doRefactoringAnnotations(@NotNull String algorithmName,
                                                 final Map<String, String> refactorings,
                                                 @NotNull AnnotationHolder holder,
                                                 @NotNull AnalysisScope scope) {
        if (refactorings == null || refactorings.isEmpty()) {
            return;
        }
        holder.getCurrentAnnotationSession().getFile().accept(new JavaRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                final String name = RefactoringUtil.getName(element);
                if (refactorings.containsKey(name)) {
                    holder.createWarningAnnotation(getAnnotationPart(element),
                            String.format("Can be moved to %s (%s)", refactorings.get(name), algorithmName))
                            .registerFix(new RefactorIntentionAction(algorithmName, name, refactorings.get(name), scope));
                }
            }
        });
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
