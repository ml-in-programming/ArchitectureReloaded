package org.ml_methods_group.ui;

import com.intellij.analysis.AnalysisScope;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TableSpeedSearch;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.refactoring.Refactoring;
import org.ml_methods_group.config.RefactoringPreferencesLog;
import org.ml_methods_group.refactoring.RefactoringSessionInfo;
import org.ml_methods_group.refactoring.RefactoringSessionInfoRenderer;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                && !(isFieldDisabled && refactoring.isMoveFieldRefactoring());
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

    private void refactorSelected() {
        doRefactorButton.setEnabled(false);
        selectAllButton.setEnabled(false);
        table.setEnabled(false);
        final List<Refactoring> selectedRefactorings = model.pullSelected();
        final List<Refactoring> rejectedRefactorings = new ArrayList<>(refactorings);
        rejectedRefactorings.removeAll(selectedRefactorings);

        /*
         * Actually RefactoringSessionInfoRenderer should be used by Log4J. But it can be configured
         * to use only through properties file. Unfortunately there is problem with configuring
         * Log4J through properties file. See issue #63.
         * https://github.com/ml-in-programming/ArchitectureReloaded/issues/63
         */
        RefactoringPreferencesLog.log.info(
            new RefactoringSessionInfoRenderer().doRender(
                new RefactoringSessionInfo(selectedRefactorings, rejectedRefactorings)
            )
        );

        RefactoringUtil.moveRefactoring(selectedRefactorings, scope, model);
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
