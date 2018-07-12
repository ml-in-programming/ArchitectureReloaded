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

package org.jetbrains.research.groups.ml_methods.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.utils.ArchitectureReloadedBundle;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class IntersectionDialog extends DialogWrapper {
    private final Map<String, Boolean> selection = new HashMap<>();

    IntersectionDialog(Project project, Set<String> algorithms) {
        super(project, false);
        algorithms.forEach(algorithm -> selection.put(algorithm, false));
        setModal(true);
        setTitle(ArchitectureReloadedBundle.message("intersection.dialog.title"));
        init();
        pack();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel content = new JPanel(new GridBagLayout());
        final JComponent separator =
                new TitledSeparator(ArchitectureReloadedBundle.message("algorithms.intersection"));

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets.left = 0;
        constraints.insets.bottom = 8;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        content.add(separator, constraints);

        constraints.insets.left = 12;
        for (String algorithm : selection.keySet()) {
            constraints.gridy++;
            final JCheckBox checkBox = new JBCheckBox(algorithm, false);
            checkBox.addActionListener(e -> selection.put(algorithm, checkBox.isSelected()));
            content.add(checkBox, constraints);
        }
        return content;
    }

    Set<String> getSelected() {
        return selection.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
