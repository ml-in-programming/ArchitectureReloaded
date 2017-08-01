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
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;
import org.ml_methods_group.algorithm.entity.RelevantProperties;
import org.ml_methods_group.utils.ArchitectureReloadedBundle;
import org.ml_methods_group.utils.PsiSearchUtil;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class DistanceDialog extends DialogWrapper {
    private static final Dimension MINIMUM_SIZE = new Dimension(1200, 600);

    private final EntitySearchResult searchResult;
    private final ComparisionModel model = new ComparisionModel();
    private final JTable comparision = new JBTable(model);
    private final Project project;
    private final JLabel generalInfo = new JLabel();

    DistanceDialog(@Nullable Project project, EntitySearchResult searchResult) {
        super(project, true);
        this.searchResult = searchResult;
        this.project = project;
        setModal(false);
        setTitle(ArchitectureReloadedBundle.message("distance.dialog.title"));
        init();
        pack();
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{new OkAction() {
        }};
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setMinimumSize(MINIMUM_SIZE);
        panel.add(ScrollPaneFactory.createScrollPane(comparision), BorderLayout.CENTER);
        panel.add(generalInfo, BorderLayout.NORTH);
        panel.add(createButtonsPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createButtonsPanel() {
        final JPanel result = new JPanel(new BorderLayout());
        final JButton chooseLeft = new JButton("Choose");
        chooseLeft.addActionListener(e -> {
            EntityPickerDialog dialog = new EntityPickerDialog(project, searchResult);
            dialog.show();
            model.setLeftEntity(dialog.getSelected());
            updateInfo(model.left, model.right);
        });
        final JButton chooseRight = new JButton("Choose");
        chooseRight.addActionListener(e -> {
            EntityPickerDialog dialog = new EntityPickerDialog(project, searchResult);
            dialog.show();
            model.setRightEntity(dialog.getSelected());
            updateInfo(model.left, model.right);
        });
        result.add(chooseLeft, BorderLayout.WEST);
        result.add(chooseRight, BorderLayout.EAST);
        return result;
    }

    private void updateInfo(Entity left, Entity right) {
        if (left == null && right == null) {
            generalInfo.setText("");
        } else if (right == null) {
            updateInfo(left);
        } else if (left == null) {
            updateInfo(right);
        } else {
            final String intersectionSize = "Intersection size: " +
                    left.getRelevantProperties().sizeOfIntersection(right.getRelevantProperties());
            final String distance = "Distance: " + left.distance(right);
            generalInfo.setText(intersectionSize + "    " + distance);
        }
    }

    private void updateInfo(Entity single) {
        generalInfo.setText("Total number of properties: " + single.getRelevantProperties().size());
    }

    private class ComparisionModel extends AbstractTableModel {
        private Entity left = null;
        private Entity right = null;
        private List<String> leftData = new ArrayList<>();
        private List<String> rightData = new ArrayList<>();

        void setLeftEntity(Entity entity) {
            left = entity;
            recalculateData();
        }

        void setRightEntity(Entity entity) {
            right = entity;
            recalculateData();
        }

        @Override
        public String getColumnName(int column) {
            final Entity entity = column == 0 ? left : right;
            return entity == null ? "" : entity.getName();
        }

        private void recalculateData() {
            final List<String> leftProperties = getAllProperties(left);
            final List<String> rightProperties = getAllProperties(right);
            leftData.clear();
            rightData.clear();
            int leftIndex = 0;
            int rightIndex = 0;
            while (leftIndex < leftProperties.size() && rightIndex < rightProperties.size()) {
                final String leftValue = leftProperties.get(leftIndex);
                final String rightValue = rightProperties.get(rightIndex);
                final int cmp = leftValue.compareTo(rightValue);
                if (cmp <= 0) {
                    leftData.add(leftValue);
                    leftIndex++;
                } else {
                    leftData.add("");
                }
                if (cmp >= 0) {
                    rightData.add(rightValue);
                    rightIndex++;
                } else {
                    rightData.add("");
                }
            }
            for (String data : leftProperties.subList(leftIndex, leftProperties.size())) {
                leftData.add(data);
                rightData.add("");
            }
            for (String data : rightProperties.subList(rightIndex, rightProperties.size())) {
                leftData.add("");
                rightData.add(data);
            }
            fireTableDataChanged();
            fireTableStructureChanged();
        }

        private List<String> getAllProperties(Entity entity) {
            if (entity == null) {
                return Collections.emptyList();
            }
            final RelevantProperties properties = entity.getRelevantProperties();
            final Set<String> allPropertiesAsSet = new HashSet<>();
            allPropertiesAsSet.addAll(properties.getClasses());
            allPropertiesAsSet.addAll(properties.getAllMethods());
            allPropertiesAsSet.addAll(properties.getFields());
            final List<String> allPropertiesAsList = new ArrayList<>(allPropertiesAsSet);
            allPropertiesAsList.sort(String::compareTo);
            return allPropertiesAsList;
        }

        @Override
        public int getRowCount() {
            return leftData.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return (columnIndex == 0 ? leftData : rightData).get(rowIndex);
        }
    }
}
