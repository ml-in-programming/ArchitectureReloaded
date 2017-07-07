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
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.table.JBTable;
import com.sixrr.metrics.utils.ArchitectureReloadedBundle;
import com.sixrr.metrics.utils.RefactoringUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static com.sixrr.metrics.utils.RefactoringUtil.createDescription;
import static com.sixrr.metrics.utils.RefactoringUtil.findElement;


/**
 * Created by Артём on 05.07.2017.
 */
public class ClassRefactoringPanel extends JPanel {

    private static final String SELECT_ALL_BUTTON_TEXT_KEY = "select.all.button";
    private static final String REFACTOR_BUTTON_TEXT_KEY = "refactor.button";

    private final Project project;
    private final AnalysisScope scope;
    private final RefactoringsTableModel model;
    private final Collection<OnRefactoringFinishedListener> listeners = new ArrayList<>();
    private final JBTable table = new JBTable();
    private final JavaCodePanel codePanel;
    private final JBLabel description = new JBLabel();

    public ClassRefactoringPanel(Project project, Map<String, String> refactorings, AnalysisScope scope) {
        this.project = project;
        this.scope = scope;
        setLayout(new BorderLayout());
        codePanel = new JavaCodePanel(project);
        model = new RefactoringsTableModel(refactorings);
        setupGUI();
    }

    private void setupGUI() {
        final JBSplitter splitter = new JBSplitter(false); // todo proportion key
        splitter.setFirstComponent(createTablePanel());
        splitter.setSecondComponent(createInfoPanel());
        add(splitter, BorderLayout.CENTER);
        add(createButtonsPanel(), BorderLayout.SOUTH);
    }

    private JComponent createTablePanel() {
        table.setModel(model);
        final TableColumn selectionColumn = table.getTableHeader().getColumnModel().getColumn(0);
        selectionColumn.setMaxWidth(30);
        selectionColumn.setMinWidth(30);
        final ListSelectionModel selectionModel = table.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionModel.addListSelectionListener(e -> updateInfoPanel());
        return ScrollPaneFactory.createScrollPane(table);
    }

    private JComponent createButtonsPanel() {
        final JPanel buttonsPanel = new JBPanel<>();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        final JButton selectAllButton = new JButton();
        selectAllButton.setText(ArchitectureReloadedBundle.message(SELECT_ALL_BUTTON_TEXT_KEY));
        selectAllButton.addActionListener(e -> model.selectAll());
        buttonsPanel.add(selectAllButton);

        final JButton doRefactorButton = new JButton();
        doRefactorButton.setText(ArchitectureReloadedBundle.message(REFACTOR_BUTTON_TEXT_KEY));
        doRefactorButton.addActionListener(e -> refactorSelected());
        buttonsPanel.add(doRefactorButton);
        return buttonsPanel;
    }

    private JComponent createInfoPanel() {
        final JPanel infoPanel = new JPanel(new BorderLayout());
        final JLabel codeTitlePanel = new JLabel();
        codeTitlePanel.setText("Code of element:");
        infoPanel.add(codeTitlePanel, BorderLayout.NORTH);
        final JScrollPane codePanelWrapper = ScrollPaneFactory.createScrollPane(codePanel);
        codePanelWrapper.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        infoPanel.add(codePanelWrapper, BorderLayout.CENTER);
        infoPanel.add(description, BorderLayout.SOUTH);
        description.setText("");
        return infoPanel;
    }

    private void refactorSelected() {
        final Map<String, String> movements = model.extractSelected();
        RefactoringUtil.moveRefactoring(movements, project, scope);
        if (model.getRowCount() == 0) {
            listeners.forEach(l -> l.onRefactoringFinished(this));
        }
    }

    private void updateInfoPanel() {
        final int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            codePanel.showElement(null);
            description.setText("");
            return;
        }
        final String elementName = model.getElement(selectedRow);
        final String movement = model.getMovement(selectedRow);
        new Thread(() -> {
            final PsiElement element = findElement(elementName, scope);
            final String refactoringInfo = createDescription(elementName, movement, scope);
            SwingUtilities.invokeLater(() -> {
                codePanel.showElement(element);
                description.setText(refactoringInfo);
            });
        }).start();
    }

    public void addOnRefactoringFinishedListener(OnRefactoringFinishedListener listener) {
        listeners.add(listener);
    }

    @FunctionalInterface
    public interface OnRefactoringFinishedListener {
        void onRefactoringFinished(ClassRefactoringPanel panel);
    }

    private static class JavaCodePanel extends EditorTextField {
        JavaCodePanel(Project project) {
            super("", project, JavaFileType.INSTANCE);
            setOneLineMode(false);
            setViewerEnabled(false);
        }

        @Override
        public void setText(@Nullable String text) {
            super.setText(text);
            if (getParent() != null) {
                getParent().revalidate();
            }
        }

        public void showElement(PsiElement element) {
            if (element == null) {
                setText("");
            } else {
                setText(element.getText());
            }
        }
    }
}
