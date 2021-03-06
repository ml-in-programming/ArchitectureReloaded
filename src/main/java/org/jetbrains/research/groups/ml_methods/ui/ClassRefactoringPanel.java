package org.jetbrains.research.groups.ml_methods.ui;

import com.intellij.analysis.AnalysisScope;
import com.intellij.ide.util.EditorHelper;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMember;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TableSpeedSearch;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.table.JBTable;
import com.sixrr.metrics.metricModel.MetricsRun;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.refactoring.CalculatedRefactoring;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveToClassRefactoring;
import org.jetbrains.research.groups.ml_methods.refactoring.logging.RefactoringFeatures;
import org.jetbrains.research.groups.ml_methods.refactoring.logging.RefactoringReporter;
import org.jetbrains.research.groups.ml_methods.refactoring.logging.RefactoringSessionInfo;
import org.jetbrains.research.groups.ml_methods.utils.ArchitectureReloadedBundle;
import org.jetbrains.research.groups.ml_methods.utils.ExportResultsUtil;
import org.jetbrains.research.groups.ml_methods.utils.RefactoringUtil;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static org.jetbrains.research.groups.ml_methods.ui.RefactoringsTableModel.ACCURACY_COLUMN_INDEX;
import static org.jetbrains.research.groups.ml_methods.ui.RefactoringsTableModel.SELECTION_COLUMN_INDEX;

class ClassRefactoringPanel extends JPanel {
    private static final String SELECT_ALL_BUTTON_TEXT_KEY = "select.all.button";
    private static final String DESELECT_ALL_BUTTON_TEXT_KEY = "deselect.all.button";
    private static final String REFACTOR_BUTTON_TEXT_KEY = "refactor.button";
    private static final String EXPORT_BUTTON_TEXT_KEY = "export.button";
    private static final int DEFAULT_THRESHOLD = 80; // percents
    private static final RefactoringReporter reporter = new RefactoringReporter();

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

    private final Map<CalculatedRefactoring, String> warnings;
    private boolean isFieldDisabled;
    private final List<CalculatedRefactoring> refactorings;
    private final Map<MoveToClassRefactoring, RefactoringFeatures> refactoringFeatures;

    private final UUID uuid = UUID.randomUUID();

    ClassRefactoringPanel(List<CalculatedRefactoring> refactorings, @NotNull AnalysisScope scope, @NotNull MetricsRun metricsRun) {
        this.scope = scope;
        this.refactorings = refactorings;

        refactoringFeatures = refactorings.stream().collect(
            Collectors.toMap(
                CalculatedRefactoring::getRefactoring,
                it -> RefactoringFeatures.extractFeatures(it.getRefactoring(), metricsRun)
            )
        );

        setLayout(new BorderLayout());
        model = new RefactoringsTableModel(RefactoringUtil.filter(refactorings));
        warnings = RefactoringsApplier.getWarnings(refactorings);
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

    public void onClose() {
        final Set<MoveToClassRefactoring> selectableRefactorings =
            model.pullSelectable().stream().map(CalculatedRefactoring::getRefactoring).collect(Collectors.toSet());

        sendLog(
            selectableRefactorings.stream().map(refactoringFeatures::get).collect(Collectors.toList()),
            Collections.emptyList(),
            Collections.emptyList()
        );
    }

    private void refreshTable() {
        model.filter(getCurrentPredicate(thresholdSlider.getValue()));
        infoLabel.setText("Total: " + model.getRowCount());
        setupTableLayout();
    }

    private Predicate<CalculatedRefactoring> getCurrentPredicate(int sliderValue) {
        return refactoring -> refactoring.getAccuracy() >= sliderValue / 100.0
                && !(isFieldDisabled && refactoring.getRefactoring().isMoveFieldRefactoring());
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
                .mapToDouble(CalculatedRefactoring::getAccuracy)
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
        model.addTableModelListener(l -> doRefactorButton.setEnabled(model.isAnySelected()));
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

        final Set<MoveToClassRefactoring> selectableRefactorings = model.pullSelectable().stream().map(CalculatedRefactoring::getRefactoring).collect(Collectors.toSet());
        final Set<MoveToClassRefactoring> selectedRefactorings = model.pullSelected().stream().map(CalculatedRefactoring::getRefactoring).collect(Collectors.toSet());

        Set<MoveToClassRefactoring> appliedRefactorings = RefactoringsApplier.moveRefactoring(new ArrayList<>(selectedRefactorings), scope, model);
        model.setAppliedRefactorings(appliedRefactorings.stream().map(m -> new CalculatedRefactoring(m, 0)).collect(Collectors.toSet()));

        Set<MoveToClassRefactoring> uncheckedRefactorings = new HashSet<>(selectableRefactorings);
        uncheckedRefactorings.removeAll(selectedRefactorings);

        Set<MoveToClassRefactoring> rejectedRefactorings = new HashSet<>(selectedRefactorings);
        rejectedRefactorings.removeAll(appliedRefactorings);

        sendLog(
            uncheckedRefactorings.stream().map(refactoringFeatures::get).collect(Collectors.toList()),
            rejectedRefactorings.stream().map(refactoringFeatures::get).collect(Collectors.toList()),
            appliedRefactorings.stream().map(refactoringFeatures::get).collect(Collectors.toList())
        );

        table.setEnabled(true);
        selectAllButton.setEnabled(true);
    }

    private void sendLog(
        final @NotNull List<RefactoringFeatures> uncheckedRefactoringsFeatures,
        final @NotNull List<RefactoringFeatures> rejectedRefactoringsFeatures,
        final @NotNull List<RefactoringFeatures> appliedRefactoringsFeatures
    ) {
        RefactoringSessionInfo info = new RefactoringSessionInfo(
            uncheckedRefactoringsFeatures,
            rejectedRefactoringsFeatures,
            appliedRefactoringsFeatures
        );

        ClassRefactoringPanel.reporter.log(uuid, info);
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
        openDefinition(model.getUnitAt(selectedRow, selectedColumn).orElse(null), scope);
    }

    private static void openDefinition(@Nullable PsiMember unit, AnalysisScope scope) {
        new Task.Backgroundable(scope.getProject(), "Search Definition"){
            private PsiElement result;

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                result = unit;
            }

            @Override
            public void onSuccess() {
                if (result != null) {
                    EditorHelper.openInEditor(result);
                }
            }
        }.queue();
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
