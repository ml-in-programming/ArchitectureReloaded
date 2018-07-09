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
import com.intellij.psi.*;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TableSpeedSearch;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.table.JBTable;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.Refactoring;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.utils.ArchitectureReloadedBundle;
import org.ml_methods_group.utils.ExportResultsUtil;
import org.ml_methods_group.utils.PsiSearchUtil;
import org.ml_methods_group.utils.RefactoringUtil;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

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
    private boolean isFieldDisabled;
    private final List<Refactoring> refactorings;
    private Logger logger;
    {
        try {
            logger = Logging.getRefactoringLogger(ClassRefactoringPanel.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ClassRefactoringPanel(List<Refactoring> refactorings, @NotNull AnalysisScope scope) {
        this.scope = scope;
        this.refactorings = refactorings;
        setLayout(new BorderLayout());
        model = new RefactoringsTableModel(RefactoringUtil.filter(refactorings, scope));
        warnings = RefactoringUtil.getWarnings(refactorings, scope);
        isFieldDisabled = false;
        model.filter(getCurrentPredicate(DEFAULT_THRESHOLD));
        setupGUI();
    }

    public void setEnableHighlighting(boolean isEnabled) {
        model.setEnableHighlighting(isEnabled);
    }

    public void excludeFieldRefactorings(boolean isDisabled) {
        isFieldDisabled = isDisabled;
        refreshTable();
    }

    private void refreshTable() {
        model.filter(getCurrentPredicate(thresholdSlider.getValue()));
        infoLabel.setText("Total: " + model.getRowCount());
        setupTableLayout();
    }

    private Predicate<Refactoring> getCurrentPredicate(int sliderValue) {
        return refactoring -> refactoring.getAccuracy() >= sliderValue / 100.0
                && !(isFieldDisabled && refactoring.isUnitField());
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
            refreshTable();
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

    private static int countLines(String str){
        String[] lines = str.split("\r\n|\r|\n");
        return  lines.length;
    }

    private void refactorSelected() {
        doRefactorButton.setEnabled(false);
        selectAllButton.setEnabled(false);
        table.setEnabled(false);
        final List<Refactoring> refactorings = model.pullSelected();
        for (Refactoring refactoring : refactorings) {
            logger.info("-------");
            logger.info(refactoring.toString());
            logger.info("Is field - " + refactoring.isUnitField());
            if (!refactoring.isUnitField()) {
                PsiMethod psiMethod = (PsiMethod) refactoring.getElement();
                String name = psiMethod.getName();
                PsiStatement[] statements = Objects.requireNonNull(psiMethod.getBody()).getStatements();
                int numberOfStatements = 0;
                final int numberOfAsserts[] = new int[1];
                final int numberOfLoops[] = new int[1];
                final int numberOfLocalVariables[] = new int[1];
                for (PsiStatement statement : statements){
                    statement.accept(new JavaRecursiveElementVisitor() {

                        @Override
                        public void visitLocalVariable(PsiLocalVariable variable) {
                            super.visitLocalVariable(variable);
                            numberOfLocalVariables[0]++;
                        }

                        @Override
                        public void visitDoWhileStatement(PsiDoWhileStatement statement) {
                            super.visitDoWhileStatement(statement);
                            numberOfLoops[0]++;
                        }

                        @Override
                        public void visitForStatement(PsiForStatement statement) {
                            super.visitForStatement(statement);
                            numberOfLoops[0]++;
                        }

                        @Override
                        public void visitForeachStatement(PsiForeachStatement statement) {
                            super.visitForeachStatement(statement);
                            numberOfLoops[0]++;
                        }

                        @Override
                        public void visitWhileStatement(PsiWhileStatement statement) {
                            super.visitWhileStatement(statement);
                            numberOfLoops[0]++;
                        }

                        @Override
                        public void visitAssertStatement(PsiAssertStatement statement) {
                            super.visitAssertStatement(statement);
                            numberOfAsserts[0]++;
                        }


                    });
                    numberOfStatements += countLines(statement.getText().replaceAll("(?m)^[ \t]*\r?\n", ""));
                }
                logger.info("Number of local variables = " + numberOfLocalVariables[0]);
                logger.info("Number of loops = " + numberOfLoops[0]);
                logger.info("Number of asserts = " + numberOfAsserts[0]);
                logger.info("Number of lines = " + numberOfStatements);
                logger.info("Is static = " + psiMethod.getModifierList().hasExplicitModifier("static"));
                logger.info("Is private = " + psiMethod.getModifierList().hasExplicitModifier("private"));
                logger.info("Number of parameters = " + psiMethod.getParameterList().getParametersCount());
                logger.info("Return type = " + psiMethod.getReturnType());
                logger.info("Is constructor = " + psiMethod.isConstructor());
                logger.info("Throws an exception = " + (psiMethod.getThrowsList().getReferencedTypes().length != 0));
                logger.info("Method's name is (" + name + ") length = " + name.length());
            }
            DateFormat  dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            logger.info(dateFormat.format(date));
        }
        RefactoringUtil.moveRefactoring(refactorings, scope, model);
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
            ExportResultsUtil.exportToFile(refactorings, fileChooser.getSelectedFile().getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
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
