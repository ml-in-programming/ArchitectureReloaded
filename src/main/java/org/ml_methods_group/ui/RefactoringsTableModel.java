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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RefactoringsTableModel extends AbstractTableModel {
    private static final String UNIT_COLUMN_TITLE_KEY = "unit.column.title";
    private static final String MOVE_TO_COLUMN_TITLE_KEY = "move.to.column.title";
    static final int SELECTION_COLUMN_INDEX = 0;
    static final int UNIT_COLUMN_INDEX = 1;
    static final int MOVE_TO_COLUMN_INDEX = 2;
    static final int WEIGHT_COLUMN_INDEX = 3;
    private static final int COLUMNS_COUNT = 4;

    private final List<Refactoring> refactorings = new ArrayList<>();
    private final boolean[] isSelected;
    private final boolean[] isActive;

    RefactoringsTableModel(List<Refactoring> refactorings) {
        this.refactorings.addAll(refactorings);
        isSelected = new boolean[refactorings.size()];
        isActive = new boolean[refactorings.size()];
        Arrays.fill(isActive, true);
        // todo sorting
    }

    void selectAll() {
        Arrays.fill(isSelected, true);
        fireTableDataChanged();
    }

    void deselectAll() {
        Arrays.fill(isSelected, false);
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

    @Override
    public int getColumnCount() {
        return COLUMNS_COUNT;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case SELECTION_COLUMN_INDEX:
                return "";
            case UNIT_COLUMN_INDEX:
                return ArchitectureReloadedBundle.message(UNIT_COLUMN_TITLE_KEY);
            case MOVE_TO_COLUMN_INDEX:
                return ArchitectureReloadedBundle.message(MOVE_TO_COLUMN_TITLE_KEY);
            case WEIGHT_COLUMN_INDEX:
                return "Вес";
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
        return refactorings.size();
    }


    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        isSelected[rowIndex] = (Boolean) value;
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    @Nullable
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case SELECTION_COLUMN_INDEX:
                return isSelected[rowIndex];
            case UNIT_COLUMN_INDEX:
                return refactorings.get(rowIndex).getUnit();
            case MOVE_TO_COLUMN_INDEX:
                return refactorings.get(rowIndex).getTarget();
            case WEIGHT_COLUMN_INDEX:
                return refactorings.get(rowIndex).getAccuracy() + "";
        }
        throw new IndexOutOfBoundsException("Unexpected column index: " + columnIndex);
    }

    String getUnitAt(int row, int column) {
        switch (column) {
            case UNIT_COLUMN_INDEX:
                return refactorings.get(row).getUnit();
            case MOVE_TO_COLUMN_INDEX:
                return refactorings.get(row).getTarget();
        }
        throw new IndexOutOfBoundsException("Unexpected column index: " + column);
    }

    List<Refactoring> getRefactorings() {
        return refactorings;
    }

    Refactoring getRefactoring(int row) {
        return refactorings.get(row);
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
                if (isActive[row]) {
                    return super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, column);
                } else {
                    return EMPTY_LABEL;
                }
            }
        });
        table.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                if (!isActive[row]) {
                    setBackground(Color.LIGHT_GRAY);
                } else {
                    setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                }
                setEnabled(isActive[row]);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });


    }
}
