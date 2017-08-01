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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;
import org.ml_methods_group.utils.ArchitectureReloadedBundle;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class EntityPickerDialog extends DialogWrapper {
    private static final Dimension MINIMUM_SIZE = new Dimension(600, 300);

    private final JTextField input = new JBTextField();
    private final SuggestionsModel model = new SuggestionsModel();
    private final JTable suggestionsTable = new JBTable();
    private final EntitySearchResult searchResult;
    private Entity selected;

    protected EntityPickerDialog(@Nullable Project project, EntitySearchResult searchResult) {
        super(project, false);
        this.searchResult = searchResult;
        setModal(true);
        setTitle(ArchitectureReloadedBundle.message("execution.info.dialog.title"));
        setOKActionEnabled(false);
        suggestionsTable.addMouseListener((DoubleClickListener) this::onDoubleClick);
        suggestionsTable.setModel(model);
        input.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                new Thread(new SuggestionsSearcher(input.getText())).start();
            }
        });
        init();
        pack();
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                close(0, false);
            }
        }};
    }

    private void onDoubleClick() {
        final int selectedRow = suggestionsTable.getSelectedRow();
        if (selectedRow != -1) {
            selected = model.getEntity(selectedRow);
            close(0, true);
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setMinimumSize(MINIMUM_SIZE);
        panel.add(input, BorderLayout.NORTH);
        panel.add(suggestionsTable, BorderLayout.CENTER);
        return panel;
    }

    public Entity getSelected() {
        return selected;
    }

    private class SuggestionsSearcher implements Runnable {
        private final String request;
        private final ArrayList<Entity> suggestions = new ArrayList<>();

        private SuggestionsSearcher(String request) {
            this.request = request;
        }

        @Override
        public void run() {
            searchInList(searchResult.getClasses());
            searchInList(searchResult.getFields());
            searchInList(searchResult.getMethods());
            SwingUtilities.invokeLater(() -> {
                if (request.equals(input.getText())) {
                    model.updateData(suggestions);
                }
            });
        }

        private void searchInList(List<? extends Entity> list) {
            if (suggestions.size() >= 10) {
                return;
            }
            for (Entity entity : list) {
                if (entity.getName().contains(request)) {
                    suggestions.add(entity);
                    if (suggestions.size() >= 10) {
                        return;
                    }
                }
            }
        }
    }

    private class SuggestionsModel extends AbstractTableModel {
        private List<Entity> suggestions = new ArrayList<>();

        void updateData(List<Entity> list) {
            suggestions.clear();
            suggestions.addAll(list);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return suggestions.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return suggestions.get(rowIndex).getName();
        }

        private Entity getEntity(int index) {
            return suggestions.get(index);
        }
    }
}
