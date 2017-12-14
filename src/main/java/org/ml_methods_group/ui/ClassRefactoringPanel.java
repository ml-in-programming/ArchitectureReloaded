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
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TableSpeedSearch;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.AlgorithmResult;
import org.ml_methods_group.algorithm.Refactoring;
import org.ml_methods_group.utils.ArchitectureReloadedBundle;
import org.ml_methods_group.utils.PsiSearchUtil;
import org.ml_methods_group.utils.RefactoringUtil;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static org.ml_methods_group.ui.RefactoringsTableModel.ACCURACY_COLUMN_INDEX;
import static org.ml_methods_group.ui.RefactoringsTableModel.SELECTION_COLUMN_INDEX;

class ClassRefactoringPanel extends JPanel {
    private static final String SELECT_ALL_BUTTON_TEXT_KEY = "select.all.button";
    private static final String DESELECT_ALL_BUTTON_TEXT_KEY = "deselect.all.button";
    private static final String REFACTOR_BUTTON_TEXT_KEY = "refactor.button";
    private static final String EXPORT_BUTTON_TEXT_KEY = "export.button";
    private static final int DEFAULT_THRESHOLD = 80; // percents

    @NotNull
    private final AnalysisScope scope;
    @NotNull
    private final RefactoringsTableModel model;
    private final JBTable table = new JBTable();
    private final JButton selectAllButton = new JButton();
    private final JButton deselectAllButton = new JButton();
    private final JButton doRefactorButton = new JButton();
    private final JButton exportButton = new JButton();
    private final JLabel infoLabel = new JLabel();
    private final JSlider thresholdSlider = new JSlider(0, 100, 0);
    private final JLabel info = new JLabel();

    private final Map<Refactoring, String> warnings;
    private final List<AlgorithmResult> results;
    private boolean isFieldDisabled;

    ClassRefactoringPanel(List<Refactoring> refactorings, List<AlgorithmResult> results, @NotNull AnalysisScope scope) {
        this.scope = scope;
        setLayout(new BorderLayout());
        model = new RefactoringsTableModel(RefactoringUtil.filter(refactorings, scope));
        model.filter(isFieldDisabled, DEFAULT_THRESHOLD / 100.0);
        warnings = RefactoringUtil.getWarnings(refactorings, scope);
        this.results = results;
        isFieldDisabled = false;
        setupGUI();
    }

    public void setEnableHighlighting(boolean isEnabled) {
        model.setEnableHighlighting(isEnabled);
    }

    public void setExcludeFieldRefactorings(boolean isDisabled) {
        isFieldDisabled = isDisabled;
        model.filter(isDisabled, thresholdSlider.getValue() / 100.0);
        infoLabel.setText("Total: " + model.getRowCount());
        setupTableLayout();
    }

    private void setupGUI() {
        add(createTablePanel(), BorderLayout.CENTER);
        add(createButtonsPanel(), BorderLayout.SOUTH);
    }

    private JComponent createTablePanel() {
        new TableSpeedSearch(table);
        table.setModel(model);
        model.setupRenderer(table);
        table.addMouseListener((DoubleClickListener) this::onDoubleClick);
        table.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> onSelectionChanged());
        table.setAutoCreateRowSorter(true);
        setupTableLayout();
        return ScrollPaneFactory.createScrollPane(table);
    }

    private void setupTableLayout() {
        final TableColumn selectionColumn = table.getTableHeader().getColumnModel().getColumn(SELECTION_COLUMN_INDEX);
        selectionColumn.setMaxWidth(30);
        selectionColumn.setMinWidth(30);
        final TableColumn accuracyColumn = table.getTableHeader().getColumnModel().getColumn(ACCURACY_COLUMN_INDEX);
        accuracyColumn.setMaxWidth(100);
        accuracyColumn.setMinWidth(100);
    }

    private JComponent createButtonsPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        final JPanel buttonsPanel = new JBPanel<>();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        final double maxAccuracy = model.getRefactorings()
                .stream()
                .mapToDouble(Refactoring::getAccuracy)
                .max()
                .orElse(1);
        final int recommendedPercents = (int) (maxAccuracy * 80);
        thresholdSlider.setToolTipText("Accuracy filter");
        thresholdSlider.addChangeListener(e -> {
            model.filter(isFieldDisabled, thresholdSlider.getValue() / 100.0);
            infoLabel.setText("Total: " + model.getRowCount());
            setupTableLayout();
        });
        thresholdSlider.setValue(recommendedPercents);
        buttonsPanel.add(thresholdSlider);

        infoLabel.setText("Total: " + model.getRowCount());
        infoLabel.setPreferredSize(new Dimension(80, 30));
        buttonsPanel.add(infoLabel);

        selectAllButton.setText(ArchitectureReloadedBundle.message(SELECT_ALL_BUTTON_TEXT_KEY));
        selectAllButton.addActionListener(e -> model.selectAll());
        buttonsPanel.add(selectAllButton);

        deselectAllButton.setText(ArchitectureReloadedBundle.message(DESELECT_ALL_BUTTON_TEXT_KEY));
        deselectAllButton.addActionListener(e -> model.deselectAll());
        buttonsPanel.add(deselectAllButton);

        doRefactorButton.setText(ArchitectureReloadedBundle.message(REFACTOR_BUTTON_TEXT_KEY));
        doRefactorButton.addActionListener(e -> refactorSelected());
        buttonsPanel.add(doRefactorButton);

        exportButton.setText(ArchitectureReloadedBundle.message(EXPORT_BUTTON_TEXT_KEY));
        exportButton.addActionListener(e -> export());
        buttonsPanel.add(exportButton);
        panel.add(buttonsPanel, BorderLayout.EAST);

        panel.add(info, BorderLayout.WEST);
        return panel;
    }

    private void refactorSelected() {
        doRefactorButton.setEnabled(false);
        selectAllButton.setEnabled(false);
        table.setEnabled(false);
        final List<Refactoring> refactorings = model.pullSelected();
        RefactoringUtil.moveRefactoring(refactorings, scope);
        table.setEnabled(true);
        doRefactorButton.setEnabled(true);
        selectAllButton.setEnabled(true);
    }

    private void export() {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fileChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.CANCEL_OPTION)
            return;
        try {
            StringBuilder allResults = new StringBuilder();
            String pathString = fileChooser.getSelectedFile().getCanonicalPath() + File.separator;
            for (AlgorithmResult result: results) {
                Path path = Paths.get(pathString + result.getAlgorithmName() + ".txt");
                Files.createFile(path);
                StringBuilder currentResults = new StringBuilder();
                for (Refactoring refactoring: result.getRefactorings()) {
                    currentResults.append(refactoring.toString()).append('\n');
                }
                Files.write(path, currentResults.toString().getBytes(), StandardOpenOption.APPEND);
                allResults.append(currentResults);
            }
            Path path = Paths.get(pathString + "All.txt");
            Files.createFile(path);
            Files.write(path, allResults.toString().getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Failed to create file");
        }
    }

    private void onDoubleClick() {
        final int selectedRow = table.getSelectedRow() == -1 ? -1 : table.convertRowIndexToModel(table.getSelectedRow());
        final int selectedColumn = table.getSelectedColumn();
        if (selectedRow == -1 || selectedColumn == -1 || selectedColumn == SELECTION_COLUMN_INDEX) {
            return;
        }
        PsiSearchUtil.openDefinition(model.getUnitAt(selectedRow, selectedColumn), scope);
    }

    private void onSelectionChanged() {
        final int selectedRow = table.getSelectedRow() == -1 ? -1 : table.convertRowIndexToModel(table.getSelectedRow());
        info.setText(selectedRow == -1 ? "" : warnings.get(model.getRefactoring(selectedRow)));
    }

    @FunctionalInterface
    private interface DoubleClickListener extends MouseListener {
        void onDoubleClick();

        default void mouseClicked(MouseEvent e) {
            if (e.getClickCount() >= 2) {
                onDoubleClick();
            }
        }

        default void mousePressed(MouseEvent e) {
        }

        default void mouseReleased(MouseEvent e) {
        }

        default void mouseEntered(MouseEvent e) {
        }

        default void mouseExited(MouseEvent e) {
        }
    }
}
