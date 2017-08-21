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

import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import org.ml_methods_group.config.ArchitectureReloadedConfig;
import org.ml_methods_group.refactoring.RefactoringExecutionContext;
import org.ml_methods_group.utils.ArchitectureReloadedBundle;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class AlgorithmsSelectionPanel extends JPanel {

    public AlgorithmsSelectionPanel() {
        super(new GridBagLayout());
        final String[] algorithms = RefactoringExecutionContext.getAvailableAlgorithms();
        final ArchitectureReloadedConfig config = ArchitectureReloadedConfig.getInstance();

        final JComponent separator =
                new TitledSeparator(ArchitectureReloadedBundle.message("algorithms.selection"));

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets.left = 0;
        constraints.insets.bottom = 8;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        add(separator, constraints);

        constraints.insets.left = 12;
        final Set<String> currentSelection = config.getSelectedAlgorithms();
        for (int i = 0; i < algorithms.length; i++) {
            constraints.gridy = 1 + i;
            final String algorithm = algorithms[i];
            final JCheckBox checkBox = new JBCheckBox(algorithm, currentSelection.contains(algorithm));
            checkBox.addActionListener(e -> config.setSelected(algorithm, checkBox.isSelected()));
            add(checkBox, constraints);
        }
    }
}
