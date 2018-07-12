package org.ml_methods_group.refactoring;

import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.Refactoring;
import org.ml_methods_group.utils.RefactoringUtil;

import java.util.Collections;

public class RefactorIntentionAction extends BaseIntentionAction {
    private final AnalysisScope scope;
    private final Refactoring refactoring;

    RefactorIntentionAction(String unit, String to, AnalysisScope scope) {
        this.scope = scope;
        this.refactoring = new Refactoring(unit, to, 0, false);
    }

    @NotNull
    @Override
    public String getText() {
        return String.format("Move to %s", refactoring.getTarget());
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "AutoRefactoring";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        return psiFile.getFileType().equals(JavaFileType.INSTANCE);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        ApplicationManager.getApplication().invokeLater(() ->
                RefactoringUtil.moveRefactoring(Collections.singletonList(refactoring), scope, null));
    }
}
