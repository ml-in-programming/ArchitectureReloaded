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
import com.intellij.ide.util.EditorHelper;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TableSpeedSearch;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.utils.ArchitectureReloadedBundle;
import org.ml_methods_group.utils.PsiSearchUtil;
import org.ml_methods_group.utils.RefactoringUtil;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static org.ml_methods_group.ui.RefactoringsTableModel.SELECTION_COLUMN_INDEX;

class ClassRefactoringPanel extends JPanel {
    private static final String SELECT_ALL_BUTTON_TEXT_KEY = "select.all.button";
    private static final String DESELECT_ALL_BUTTON_TEXT_KEY = "deselect.all.button";
    private static final String REFACTOR_BUTTON_TEXT_KEY = "refactor.button";

    @NotNull
    private final Project project;
    @NotNull
    private final AnalysisScope scope;
    @NotNull
    private final RefactoringsTableModel model;
    private final Collection<OnRefactoringFinishedListener> listeners = new ArrayList<>();
    private final JBTable table = new JBTable();
    private final JButton selectAllButton = new JButton();
    private final JButton deselectAllButton = new JButton();
    private final JButton doRefactorButton = new JButton();
    private final JLabel info = new JLabel();

    ClassRefactoringPanel(@NotNull Project project, Map<String, String> refactorings,
                          @NotNull AnalysisScope scope) {
        this.project = project;
        this.scope = scope;
        setLayout(new BorderLayout());
        model = new RefactoringsTableModel(refactorings);
        setupGUI();
    }

    private void setupGUI() {
        add(createTablePanel(), BorderLayout.CENTER);
        add(createButtonsPanel(), BorderLayout.SOUTH);
    }

    private JComponent createTablePanel() {
        new TableSpeedSearch(table);
        table.setModel(model);
        final TableColumn selectionColumn = table.getTableHeader().getColumnModel().getColumn(0);
        selectionColumn.setMaxWidth(30);
        selectionColumn.setMinWidth(30);
        table.addMouseListener((DoubleClickListener) this::onDoubleClick);
        table.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> onSelectionChanged());
        return ScrollPaneFactory.createScrollPane(table);
    }

    private JComponent createButtonsPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        final JPanel buttonsPanel = new JBPanel<>();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        selectAllButton.setText(ArchitectureReloadedBundle.message(SELECT_ALL_BUTTON_TEXT_KEY));
        selectAllButton.addActionListener(e -> model.selectAll());
        buttonsPanel.add(selectAllButton);

        deselectAllButton.setText(ArchitectureReloadedBundle.message(DESELECT_ALL_BUTTON_TEXT_KEY));
        deselectAllButton.addActionListener(e -> model.deselectAll());
        buttonsPanel.add(deselectAllButton);

        doRefactorButton.setText(ArchitectureReloadedBundle.message(REFACTOR_BUTTON_TEXT_KEY));
        doRefactorButton.addActionListener(e -> refactorSelected());
        buttonsPanel.add(doRefactorButton);
        panel.add(buttonsPanel, BorderLayout.EAST);

        panel.add(info, BorderLayout.WEST);
        return panel;
    }

    private void refactorSelected() {
        doRefactorButton.setEnabled(false);
        selectAllButton.setEnabled(false);
        table.setEnabled(false);
        final Map<String, String> movements = model.getSelected();
        RefactoringUtil.moveRefactoring(movements, project, scope);
        listeners.forEach(l -> l.onRefactoringFinished(this));
        table.setEnabled(true);
        doRefactorButton.setEnabled(true);
        selectAllButton.setEnabled(true);
    }

    private void onDoubleClick() {
        final int selectedRow = table.getSelectedRow();
        final int selectedColumn = table.getSelectedColumn();
        if (selectedRow == -1 || selectedColumn == -1 || selectedColumn == SELECTION_COLUMN_INDEX) {
            return;
        }
        PsiSearchUtil.findElement(model.getUnitAt(selectedRow, selectedColumn), scope)
                .ifPresent(EditorHelper::openInEditor);
    }

    private void onSelectionChanged() {
        final int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            info.setText("");
        } else {
            String unit = model.getUnitAt(selectedRow, 1);
            String target = model.getUnitAt(selectedRow, 2);
            info.setText(RefactoringUtil.getWarning(unit, target, scope));
        }
    }

    // todo maybe tool window should be close after refactorings
    void addOnRefactoringFinishedListener(OnRefactoringFinishedListener listener) {
        listeners.add(listener);
    }

    @FunctionalInterface
    public interface OnRefactoringFinishedListener {
        void onRefactoringFinished(ClassRefactoringPanel panel);
    }

    @FunctionalInterface
    private interface DoubleClickListener extends MouseListener {
        void onDoubleClick();

        default void mouseClicked(MouseEvent e) {
            if (e.getClickCount() >= 2) {
                onDoubleClick();
            }
        }

        default void mousePressed(MouseEvent e) {}
        default void mouseReleased(MouseEvent e) {}
        default void mouseEntered(MouseEvent e) {}
        default void mouseExited(MouseEvent e) {}
    }
}
