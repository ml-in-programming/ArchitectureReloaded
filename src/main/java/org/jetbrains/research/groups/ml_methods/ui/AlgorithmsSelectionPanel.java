package org.jetbrains.research.groups.ml_methods.ui;

import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.config.ArchitectureReloadedConfig;
import org.jetbrains.research.groups.ml_methods.refactoring.RefactoringExecutionContext;
import org.jetbrains.research.groups.ml_methods.utils.ArchitectureReloadedBundle;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Set;

public class AlgorithmsSelectionPanel extends JPanel {
    public AlgorithmsSelectionPanel() {
        super(new GridBagLayout());
        initializePanel(createRefactoringsTab(), createAlgorithmsTab());
    }

    private void initializePanel(Tab... tabs) {
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets.top = 4;
        constraints.insets.bottom = 4;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        final JComponent separator =
            new TitledSeparator(ArchitectureReloadedBundle.message("advanced.settings"));

        add(separator, constraints);

        JTabbedPane tabbedPane = new JTabbedPane();

        Border border = BorderFactory.createEmptyBorder(0, 8, 0, 0);
        for (Tab tab : tabs) {
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
            wrapper.add(tab.getComponent());
            wrapper.setBorder(border);
            tabbedPane.addTab(tab.getTitle(), wrapper);
        }

        constraints.gridy++;
        add(tabbedPane, constraints);
    }

    private static Tab createRefactoringsTab() {
        final ArchitectureReloadedConfig config = ArchitectureReloadedConfig.getInstance();

        JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets.top = 4;
        constraints.insets.bottom = 4;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        final JCheckBox methodsRefactoringsCheckBox = new JBCheckBox(
            ArchitectureReloadedBundle.message("search.for.move.methods.refactorings"),
            true
        );

        methodsRefactoringsCheckBox.setEnabled(false);

        panel.add(methodsRefactoringsCheckBox, constraints);
        constraints.gridy++;

        final JCheckBox fieldsRefactoringsCheckBox = new JBCheckBox(
            ArchitectureReloadedBundle.message("search.for.move.field.refactorings")
        );

        fieldsRefactoringsCheckBox.addActionListener(e -> config.setFieldRefactoringsAvailable());
        panel.add(fieldsRefactoringsCheckBox, constraints);

        return new Tab(panel, ArchitectureReloadedBundle.message("refactorings.tab.text"));
    }

    private static Tab createAlgorithmsTab() {
        JPanel panel = new JPanel(new GridBagLayout());

        final String[] algorithms = RefactoringExecutionContext.getAvailableAlgorithms();
        final ArchitectureReloadedConfig config = ArchitectureReloadedConfig.getInstance();

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets.top = 4;
        constraints.insets.bottom = 4;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        final Set<String> currentSelection = config.getSelectedAlgorithms();
        for (int i = 0; i < algorithms.length; i++) {
            final String algorithm = algorithms[i];

            final JCheckBox checkBox =
                new JBCheckBox(algorithm, currentSelection.contains(algorithm));
            checkBox.addActionListener(e -> config.setSelected(algorithm, checkBox.isSelected()));

            constraints.gridy = i;
            panel.add(checkBox, constraints);
        }

        return new Tab(panel, ArchitectureReloadedBundle.message("algorithms.tab.text"));
    }

    private static class Tab {
        private final @NotNull Component component;

        private final @NotNull String title;

        public Tab(final @NotNull Component component, final @NotNull String title) {
            this.component = component;
            this.title = title;
        }

        public @NotNull Component getComponent() {
            return component;
        }

        public @NotNull String getTitle() {
            return title;
        }
    }
}
