package org.jetbrains.research.groups.ml_methods.intention;

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
import org.jetbrains.research.groups.ml_methods.refactoring.MoveToClassRefactoring;
import org.jetbrains.research.groups.ml_methods.ui.RefactoringsApplier;

import java.util.Collections;

import static org.jetbrains.research.groups.ml_methods.utils.PSIUtil.getHumanReadableName;

public class RefactorIntentionAction extends BaseIntentionAction {
    private final AnalysisScope scope;
    private final MoveToClassRefactoring refactoring;

    RefactorIntentionAction(MoveToClassRefactoring refactoring, AnalysisScope scope) {
        this.scope = scope;
        this.refactoring = refactoring;
    }

    @NotNull
    @Override
    public String getText() {
        return String.format("Move to %s", getHumanReadableName(refactoring.getTargetClassOrThrow()));
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
                RefactoringsApplier.moveRefactoring(Collections.singletonList(refactoring), scope, null));
    }
}
