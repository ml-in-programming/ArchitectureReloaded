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
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.ml_methods_group.utils.RefactoringUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RefactorIntentionAction extends BaseIntentionAction {
    private final String algorithmName;
    private final Map<String, String> refactorings = new HashMap<>();
    private final AnalysisScope scope;
    private final String moveTo;

    public RefactorIntentionAction(String algorithmName, String from, String to, AnalysisScope scope) {
        this.algorithmName = algorithmName;
        this.scope = scope;
        this.moveTo = to;
        refactorings.put(from, to);
    }

    @NotNull
    @Override
    public String getText() {
        return String.format("Move to %s", moveTo);
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
                RefactoringUtil.moveRefactoring(refactorings, scope));
    }
}
