package org.jetbrains.research.groups.ml_methods.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.TitledSeparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.AlgorithmResult;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.EntitiesStorage;
import org.jetbrains.research.groups.ml_methods.utils.ArchitectureReloadedBundle;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ExecutionInfoDialog extends DialogWrapper {
    private final List<AlgorithmResult> results;
    private final EntitiesStorage entitiesStorage;

    ExecutionInfoDialog(Project project, EntitiesStorage entitiesStorage, List<AlgorithmResult> results) {
        super(project, false);
        this.results = results;
        this.entitiesStorage = entitiesStorage;
        setResizable(false);
        setModal(true);
        setTitle(ArchitectureReloadedBundle.message("execution.info.dialog.title"));
        init();
        pack();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel content = new JPanel(new BorderLayout());
        JPanel algorithmsInfo = new JPanel(new FlowLayout(FlowLayout.LEADING));
        results.stream()
                .map(this::createInfoPanel)
                .forEach(algorithmsInfo::add);
        content.add(createInfoPanel(entitiesStorage), BorderLayout.NORTH);
        content.add(algorithmsInfo, BorderLayout.SOUTH);
        return content;
    }

    private JPanel createInfoPanel(AlgorithmResult result) {
        final JPanel panel = new JPanel(new GridBagLayout());

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets.left = 0;
        constraints.insets.bottom = 8;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        panel.add(new TitledSeparator(result.getAlgorithm().getName() + " execution"), constraints);

        constraints.insets.left = 12;
        constraints.gridy++;
        panel.add(new JLabel("Execution time: " + (result.getExecutionTime()) / 1000 + " secs"), constraints);
        constraints.gridy++;
        panel.add(new JLabel("Threads used: " + result.getThreadUsed()), constraints);
        constraints.gridy++;
        panel.add(new JLabel("Refactorings found: " + result.getRefactorings().size()), constraints);
        return panel;
    }

    private JPanel createInfoPanel(EntitiesStorage entitiesStorage) {
        final JPanel panel = new JPanel(new GridBagLayout());

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets.left = 0;
        constraints.insets.bottom = 8;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        panel.add(new TitledSeparator("Entities preprocessing"), constraints);

        constraints.insets.left = 12;
        constraints.gridy++;
        panel.add(new JLabel("Preprocessing time: " + (entitiesStorage.getBuildTime()) / 1000 + " secs"), constraints);
        constraints.gridy++;
        panel.add(new JLabel("Classes found: " + entitiesStorage.getClasses().size()), constraints);
        constraints.gridy++;
        panel.add(new JLabel("Methods found: " + entitiesStorage.getMethods().size()), constraints);
        constraints.gridy++;
        panel.add(new JLabel("Fields found: " + entitiesStorage.getFields().size()), constraints);
        constraints.gridy++;
        panel.add(new JLabel("Total number of properties: " + entitiesStorage.getPropertiesCount()), constraints);
        return panel;
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{new OkAction(){}};
    }
}
