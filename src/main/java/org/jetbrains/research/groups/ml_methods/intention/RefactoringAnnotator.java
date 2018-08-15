package org.jetbrains.research.groups.ml_methods.intention;

import com.intellij.analysis.AnalysisScope;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMember;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;
import org.jetbrains.research.groups.ml_methods.algorithm.AlgorithmsRepository;
import org.jetbrains.research.groups.ml_methods.algorithm.RefactoringExecutionContext;
import org.jetbrains.research.groups.ml_methods.algorithm.AlgorithmsRepository;
import org.jetbrains.research.groups.ml_methods.plugin.AutomaticRefactoringAction;
import org.jetbrains.research.groups.ml_methods.refactoring.CalculatedRefactoring;

import java.util.List;

public class RefactoringAnnotator implements Annotator {
    @Override
    public synchronized void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (!(psiElement instanceof PsiMember)) {
            return;
        }
        final PsiMember psiMember = (PsiMember) psiElement;
        final Project project = psiMember.getProject();
        final AnalysisScope scope = new AnalysisScope(project);

        for (Algorithm algorithm : AlgorithmsRepository.getAvailableAlgorithms()) {
            try {
                setAnnotations(psiMember,
                        algorithm.getDescriptionString(),
                        AutomaticRefactoringAction.getInstance(project).getRefactoringsForType(algorithm.getAlgorithmType()),
                        annotationHolder, scope);
            } catch (IllegalArgumentException e) {
                //ignore
            }
        }
    }

    private static void setAnnotations(@NotNull PsiMember element,
                                       @NotNull String algorithmName,
                                       final List<CalculatedRefactoring> refactorings,
                                       @NotNull AnnotationHolder holder,
                                       @NotNull AnalysisScope scope) {
        if (refactorings == null || refactorings.isEmpty()) {
            return;
        }
        final CalculatedRefactoring refactoring = refactorings.stream()
                .filter(refactoringToCheck -> refactoringToCheck.getRefactoring().getEntityOrThrow().equals(element))
                .findAny()
                .orElse(null);
        if (refactoring != null) {
            final Annotation annotation = holder.createWarningAnnotation(
                    getAnnotationPart(element),
                    String.format("Can be moved to %s (%s)",
                            refactoring.getRefactoring().getTargetClassOrThrow().getQualifiedName(), algorithmName));

            annotation.registerFix(new RefactorIntentionAction(refactoring.getRefactoring(), scope));
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
