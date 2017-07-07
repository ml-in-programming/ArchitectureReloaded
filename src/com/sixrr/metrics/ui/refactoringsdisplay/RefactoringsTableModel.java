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

import com.sixrr.metrics.utils.ArchitectureReloadedBundle;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by Артём on 05.07.2017.
 */
public class RefactoringsTableModel extends AbstractTableModel {

    private static final String UNIT_COLUMN_TITLE_KEY = "unit.column.title";
    private static final String MOVE_TO_COLUMN_TITLE_KEY = "move.to.column.title";

    private List<String> units = new ArrayList<>();
    private List<String> movements = new ArrayList<>();
    private boolean[] isSelected;

    RefactoringsTableModel(Map<String, String> refactorings) {
        for (Entry<String, String> refactoring : refactorings.entrySet()) {
            units.add(refactoring.getKey());
            movements.add(refactoring.getValue());
        }
        isSelected = new boolean[units.size()];
    }

    public void selectAll() {
        Arrays.fill(isSelected, true);
        fireTableDataChanged();
    }

    public Map<String, String> extractSelected() {
        final List<String> notSelectedUnits = new ArrayList<>();
        final List<String> notSelectedMovements = new ArrayList<>();
        final Map<String, String> selected = new HashMap<>();
        for (int i = 0; i < isSelected.length; i++) {
            if (isSelected[i]) {
                selected.put(units.get(i), movements.get(i));
            } else {
                notSelectedUnits.add(units.get(i));
                notSelectedMovements.add(movements.get(i));
            }
        }
        units = notSelectedUnits;
        movements = notSelectedMovements;
        isSelected = new boolean[notSelectedUnits.size()];
        fireTableDataChanged();
        return selected;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "";
            case 1:
                return ArchitectureReloadedBundle.message(UNIT_COLUMN_TITLE_KEY);
            case 2:
                return ArchitectureReloadedBundle.message(MOVE_TO_COLUMN_TITLE_KEY);
        }
        throw new IndexOutOfBoundsException("Unexpected column index: " + column);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? Boolean.class : String.class;
    }

    @Override
    public int getRowCount() {
        return units.size();
    }


    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        isSelected[rowIndex] = ((Boolean) value).booleanValue();
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    @Nullable
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Boolean.valueOf(isSelected[rowIndex]);
            case 1:
                return units.get(rowIndex);
            case 2:
                return movements.get(rowIndex);
        }
        throw new IndexOutOfBoundsException("Unexpected column index: " + columnIndex);
    }

    public String getElement(int row) {
        return units.get(row);
    }

    public String getMovement(int row) {
        return movements.get(row);
    }
}
