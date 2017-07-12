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

package org.ml_methods_group.ui;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTabbedPane;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.utils.ArchitectureReloadedBundle;

import javax.swing.*;
import java.awt.*;
import java.util.Map;


public class RefactoringDialog extends DialogWrapper {

    private static final String DIMENSIONS_SERVICE_KEY = "ArchitectureReloaded.RefactoringDialog";
    private static final String DIALOG_TITLE_KEY = "refactoring.dialog.title";
    private static final Action[] EMPTY_ACTION_ARRAY = new Action[0];
    private final JBTabbedPane pane = new JBTabbedPane();
    private final Project project;
    private final AnalysisScope scope;

    public RefactoringDialog(Project project, AnalysisScope scope) {
        super(project, false);
        this.project = project;
        this.scope = scope;
        setModal(true);
        setTitle(ArchitectureReloadedBundle.message(DIALOG_TITLE_KEY));
        init();
        pack();
    }

    @Override
    @NonNls
    protected String getDimensionServiceKey() {
        return DIMENSIONS_SERVICE_KEY;
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

    public RefactoringDialog addSolution(String algorithmName, Map<String, String> refactorings) {
        final ClassRefactoringPanel panel = new ClassRefactoringPanel(project, refactorings, scope);
        panel.addOnRefactoringFinishedListener(p -> close(0, true));
        pane.addTab(algorithmName, panel);
        return this;
    }
}
