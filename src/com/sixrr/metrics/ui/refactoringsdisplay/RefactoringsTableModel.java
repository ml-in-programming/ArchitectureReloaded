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

import org.jetbrains.annotations.Nullable;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by Артём on 05.07.2017.
 */
public class RefactoringsTableModel extends AbstractTableModel {

    private List<String> methods = new ArrayList<>();
    private List<String> movements = new ArrayList<>();
    private boolean[] isSelected;

    RefactoringsTableModel(Map<String, String> refactorings) {
        for (Entry<String, String> refactoring : refactorings.entrySet()) {
            methods.add(refactoring.getKey());
            movements.add(refactoring.getValue());
        }
        isSelected = new boolean[methods.size()];
    }

    public void selectAll() {
        Arrays.fill(isSelected, true);
        fireTableDataChanged();
    }

    public Map<String, String> extractSelected() {
        final List<String> notSelectedMethods = new ArrayList<>();
        final List<String> notSelectedMovements = new ArrayList<>();
        final Map<String, String> selected = new HashMap<>();
        for (int i = 0; i < isSelected.length; i++) {
            if (isSelected[i]) {
                selected.put(methods.get(i), movements.get(i));
            } else {
                notSelectedMethods.add(methods.get(i));
                notSelectedMovements.add(movements.get(i));
            }
        }
        methods = notSelectedMethods;
        movements = notSelectedMovements;
        isSelected = new boolean[notSelectedMethods.size()];
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
            case 0: return "  ";
            case 1: return "Method";
            case 2: return "Move to";
        }
        throw new IndexOutOfBoundsException("Unexpected column index: " + column);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0? Boolean.class : String.class;
    }

    @Override
    public int getRowCount() {
        return methods.size();
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
            case 0 : return Boolean.valueOf(isSelected[rowIndex]);
            case 1 : return methods.get(rowIndex);
            case 2 : return movements.get(rowIndex);
        }
        throw new IndexOutOfBoundsException("Unexpected column index: " + columnIndex);
    }

    public String getElement(int row) {
        return methods.get(row);
    }
}
