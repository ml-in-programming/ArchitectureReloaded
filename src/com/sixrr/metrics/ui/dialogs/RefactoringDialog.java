/*
 * Copyright 2005-2017 Sixth and Red River Software, Bas Leijdekkers
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

package com.sixrr.metrics.ui.dialogs;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import com.intellij.ui.components.JBTabbedPane;
import com.sixrr.metrics.ui.refactoringsdisplay.ClassRefactoringPanel;
import com.sixrr.metrics.utils.MetricsReloadedBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Map;


public class RefactoringDialog extends DialogWrapper {

    private static final Action[] EMPTY_ACTION_ARRAY = new Action[0];
    private final JBTabbedPane pane = new JBTabbedPane();
    private final Project project;
    private final AnalysisScope scope;

    public RefactoringDialog(Project project, AnalysisScope scope) {
        super(project, false);
        this.project = project;
        this.scope = scope;
        setModal(true);
        setTitle(MetricsReloadedBundle.message("refactoring.dialog.title"));
        init();
        pack();
    }

    @Override
    @NonNls
    protected String getDimensionServiceKey() {
        return "MetricsReloaded.RefactoringDialog";

    }

    @NotNull
    @Override
    public Action[] createActions() {
        return EMPTY_ACTION_ARRAY;
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(pane, BorderLayout.CENTER);
        return panel;
    }

    public RefactoringDialog addSolution(String algorithmName, Map<PsiElement, PsiElement> refactorings) {
        final ClassRefactoringPanel panel = new ClassRefactoringPanel(project, refactorings, scope);
        panel.addOnRefactoringFinishedListener(p -> closeTab(algorithmName));
        pane.addTab(algorithmName, panel);
        return this;
    }

    private void closeTab(String tabName) {
        final int tabIndex = pane.indexOfTab(tabName);
        pane.removeTabAt(tabIndex);
        if (pane.getTabCount() == 0) {
            close(0, true);
        }
    }
}
