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

import com.intellij.ui.BooleanTableCellRenderer;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.Refactoring;
import org.ml_methods_group.utils.ArchitectureReloadedBundle;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RefactoringsTableModel extends AbstractTableModel {
    private static final String ENTITY_COLUMN_TITLE_KEY = "unit.column.title";
    private static final String MOVE_TO_COLUMN_TITLE_KEY = "move.to.column.title";
    private static final String ACCURACY_COLUMN_TITLE_KEY = "accuracy.column.title";

    private static final Color GREEN = new Color(133, 255, 51);
    private static final Color PALE_GREEN = new Color(193, 255, 139);
    private static final Color YELLOW = new Color(255, 255, 111);
    private static final Color RED = new Color(255, 181, 165);

    static final int SELECTION_COLUMN_INDEX = 0;
    static final int ENTITY_COLUMN_INDEX = 1;
    static final int MOVE_TO_COLUMN_INDEX = 2;
    static final int ACCURACY_COLUMN_INDEX = 3;
    private static final int COLUMNS_COUNT = 4;

    private final List<Refactoring> refactorings = new ArrayList<>();
    private final List<Integer> virtualRows = new ArrayList<>();
    private final boolean[] isSelected;
    private final boolean[] isActive;
    private boolean enableHighlighting;

    RefactoringsTableModel(List<Refactoring> refactorings) {
        this.refactorings.addAll(refactorings);
        isSelected = new boolean[refactorings.size()];
        isActive = new boolean[refactorings.size()];
        Arrays.fill(isActive, true);
        IntStream.range(0, refactorings.size())
                .forEachOrdered(virtualRows::add);
    }

    void selectAll() {
        virtualRows.forEach(i -> isSelected[i] = true);
        fireTableDataChanged();
    }

    void deselectAll() {
        Arrays.fill(isSelected, false);
        fireTableDataChanged();
    }

    void setEnableHighlighting(boolean isEnabled) {
        this.enableHighlighting = isEnabled;
        fireTableDataChanged();
    }

    List<Refactoring> pullSelected() {
        final List<Refactoring> result = IntStream.range(0, isSelected.length)
                .filter(i -> isSelected[i] && isActive[i])
                .peek(i -> isActive[i] = false)
                .mapToObj(refactorings::get)
                .collect(Collectors.toList());
        fireTableDataChanged();
        return result;
    }

    void filter(boolean disableFieldRefactorings, double threshold) {
        virtualRows.clear();
        deselectAll();
        IntStream.range(0, refactorings.size())
                .filter(i -> refactorings.get(i).getAccuracy() >= threshold && !(disableFieldRefactorings && refactorings.get(i).isField()))
                .forEachOrdered(virtualRows::add);
        fireTableStructureChanged();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS_COUNT;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case SELECTION_COLUMN_INDEX:
                return "";
            case ENTITY_COLUMN_INDEX:
                return ArchitectureReloadedBundle.message(ENTITY_COLUMN_TITLE_KEY);
            case MOVE_TO_COLUMN_INDEX:
                return ArchitectureReloadedBundle.message(MOVE_TO_COLUMN_TITLE_KEY);
            case ACCURACY_COLUMN_INDEX:
                return ArchitectureReloadedBundle.message(ACCURACY_COLUMN_TITLE_KEY);
        }
        throw new IndexOutOfBoundsException("Unexpected column index: " + column);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == SELECTION_COLUMN_INDEX && isActive[rowIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == SELECTION_COLUMN_INDEX ? Boolean.class : String.class;
    }

    @Override
    public int getRowCount() {
        return virtualRows.size();
    }


    @Override
    public void setValueAt(Object value, int virtualRow, int columnIndex) {
        isSelected[virtualRows.get(virtualRow)] = (Boolean) value;
        fireTableCellUpdated(virtualRow, columnIndex);
    }

    @Override
    @Nullable
    public Object getValueAt(int virtualRow, int columnIndex) {
        final int rowIndex = virtualRows.get(virtualRow);
        switch (columnIndex) {
            case SELECTION_COLUMN_INDEX:
                return isSelected[rowIndex];
            case ENTITY_COLUMN_INDEX:
                return refactorings.get(rowIndex).getUnit();
            case MOVE_TO_COLUMN_INDEX:
                return refactorings.get(rowIndex).getTarget();
            case ACCURACY_COLUMN_INDEX:
                final double accuracy = refactorings.get(rowIndex).getAccuracy();
                return String.format("%.2f", accuracy);
        }
        throw new IndexOutOfBoundsException("Unexpected column index: " + columnIndex);
    }

    String getUnitAt(int virtualRow, int column) {
        final int row = virtualRows.get(virtualRow);
        switch (column) {
            case ENTITY_COLUMN_INDEX:
                return refactorings.get(row).getUnit();
            case MOVE_TO_COLUMN_INDEX:
                return refactorings.get(row).getTarget();
        }
        throw new IndexOutOfBoundsException("Unexpected column index: " + column);
    }

    Set<Refactoring> getRefactorings() {
        return new HashSet<>(refactorings);
    }

    Refactoring getRefactoring(int virtualRow) {
        return refactorings.get(virtualRows.get(virtualRow));
    }

    void setupRenderer(JTable table) {
        table.setDefaultRenderer(Boolean.class, new BooleanTableCellRenderer() {
            private final JLabel EMPTY_LABEL = new JLabel();

            {
                EMPTY_LABEL.setBackground(Color.LIGHT_GRAY);
                EMPTY_LABEL.setOpaque(true);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus,
                                                           int row, int column) {
                final int realRow = virtualRows.get(table.convertRowIndexToModel(row));
                if (isActive[realRow]) {
                    return super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, column);
                } else {
                    return EMPTY_LABEL;
                }
            }
        });
        table.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int virtualRow, int column) {
                final int row = virtualRows.get(table.convertRowIndexToModel(virtualRow));
                if (!isActive[row]) {
                    setBackground(Color.LIGHT_GRAY);
                } else if (isSelected) {
                    setBackground(table.getSelectionBackground());
                } else {
                    setBackground(enableHighlighting ? toneFor(refactorings.get(row).getAccuracy())
                            : table.getBackground());
                }
                setHorizontalAlignment(column == ACCURACY_COLUMN_INDEX ? CENTER : LEFT);
                setEnabled(isActive[row]);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
    }

    private static Color toneFor(double accuracy) {
        if (accuracy > 0.8) {
            return GREEN;
        } else if (accuracy > 0.6) {
            return PALE_GREEN;
        } else if (accuracy > 0.4) {
            return YELLOW;
        }
        return RED;
    }
}
