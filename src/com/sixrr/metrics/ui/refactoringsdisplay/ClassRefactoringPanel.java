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

package com.sixrr.metrics.ui.refactoringsdisplay;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.table.JBTable;
import com.sixrr.metrics.utils.RefactoringUtil;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;


/**
 * Created by Артём on 05.07.2017.
 */
public class ClassRefactoringPanel extends JPanel {

    private final Project project; // necessary to do refactorings
    private final AnalysisScope scope; // necessary to do refactorings
    private final RefactoringsTableModel model;
    private final Collection<OnRefactoringFinishedListener> listeners = new ArrayList<>();

    public ClassRefactoringPanel(Project project, Map<PsiElement, PsiElement> refactorings, AnalysisScope scope) {
        this.project = project;
        this.scope = scope;
        setLayout(new BorderLayout());
        model = new RefactoringsTableModel(refactorings);
        setupGUI();
    }

    private void setupGUI() {
        final JBTable table = new JBTable(model);
        final TableColumn selectionColumn = table.getTableHeader().getColumnModel().getColumn(0);
        selectionColumn.setMaxWidth(30);
        selectionColumn.setMinWidth(30);
        add(ScrollPaneFactory.createScrollPane(table), BorderLayout.CENTER);

        final JPanel buttonsPanel = new JBPanel<>();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        add(buttonsPanel, BorderLayout.SOUTH);

        final JButton selectAllButton = new JButton("Select all");
        selectAllButton.addActionListener(e -> model.selectAll());
        buttonsPanel.add(selectAllButton);

        final JButton doRefactorButton = new JButton("Refactor");
        doRefactorButton.addActionListener(e -> {
            final Map<PsiElement, PsiElement> movements =  model.extractSelected();
            RefactoringUtil.moveRefactoring(movements, project, scope);
            if (model.getRowCount() == 0) {
                listeners.forEach(l -> l.onRefactoringFinished(this));
            }
        });
        buttonsPanel.add(doRefactorButton);
    }

    public void addOnRefactoringFinishedListener(OnRefactoringFinishedListener listener) {
        listeners.add(listener);
    }

    @FunctionalInterface
    public interface OnRefactoringFinishedListener {
        void onRefactoringFinished(ClassRefactoringPanel panel);
    }
}
