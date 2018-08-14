package org.jetbrains.research.groups.ml_methods.ui;

import com.intellij.analysis.AnalysisScope;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.sixrr.metrics.metricModel.MetricsRun;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.AlgorithmResult;
import org.jetbrains.research.groups.ml_methods.algorithm.AlgorithmsRepository.AlgorithmType;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.EntitiesStorage;
import org.jetbrains.research.groups.ml_methods.refactoring.CalculatedRefactoring;
import org.jetbrains.research.groups.ml_methods.utils.ArchitectureReloadedBundle;
import org.jetbrains.research.groups.ml_methods.utils.RefactoringUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class RefactoringsToolWindow implements Disposable {

    private static final String WINDOW_ID = "Suggested refactorings";
    private static final String TITLE_KEY = "refactorings.tool.window.title";

    private final Project project;
    private final List<ClassRefactoringPanel> contents = new ArrayList<>();
    private ToolWindow myToolWindow = null;
    private List<AlgorithmResult> results;
    private EntitiesStorage entitiesStorage;
    private MetricsRun metricsRun;
    private AnalysisScope scope;
    private boolean enableHighlighting;
    private boolean excludeFieldRefactorings;


    private RefactoringsToolWindow(@NotNull Project project) {
        this.project = project;
        register();
    }

    private void register() {
        final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        myToolWindow = toolWindowManager.registerToolWindow(WINDOW_ID, true, ToolWindowAnchor.BOTTOM);
        myToolWindow.setTitle(ArchitectureReloadedBundle.message(TITLE_KEY));
        myToolWindow.setAvailable(false, null);
    }

    private void addTab(String tabName, @NotNull List<CalculatedRefactoring> refactorings, boolean isClosable) {
        final ClassRefactoringPanel panel = new ClassRefactoringPanel(refactorings, scope, metricsRun);
        panel.setEnableHighlighting(enableHighlighting);
        final ActionToolbar toolbar = createToolbar();
        final JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.add(toolbar.getComponent(), BorderLayout.WEST);
        final Content content = myToolWindow.getContentManager().getFactory()
                .createContent(contentPanel, tabName, true);
        content.setCloseable(isClosable);
        contents.add(panel);
        myToolWindow.getContentManager().addContent(content);
    }

    private ActionToolbar createToolbar() {
        final DefaultActionGroup toolbarGroup = new DefaultActionGroup();
        toolbarGroup.add(new IntersectAction());
        toolbarGroup.add(new ColorAction());
        toolbarGroup.add(new InfoAction());
        toolbarGroup.add(new ExcludeFieldRefactoringsAction());
        toolbarGroup.add(new CloseAction());
        return ActionManager.getInstance()
                .createActionToolbar(WINDOW_ID, toolbarGroup, false);
    }

    public void show(
        List<AlgorithmResult> results,
        EntitiesStorage entitiesStorage,
        AnalysisScope scope,
        final @NotNull MetricsRun metricsRun
    ) {
        this.results = results;
        this.scope = scope;
        this.entitiesStorage = entitiesStorage;
        this.metricsRun = metricsRun;

        myToolWindow.getContentManager().removeAllContents(true);
        contents.clear();
        myToolWindow.setAvailable(false, null);
        final List<List<CalculatedRefactoring>> refactorings = results.stream()
                .map(AlgorithmResult::getRefactorings)
                .collect(Collectors.toList());
        final List<CalculatedRefactoring> measured = RefactoringUtil.combine(refactorings);
        addTab("Total", measured, false);
        myToolWindow.setAvailable(true, null);
        myToolWindow.show(null);
    }

    @Override
    public void dispose() {
        final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        toolWindowManager.unregisterToolWindow(WINDOW_ID);
        results = null;
        scope = null;
        entitiesStorage = null;
    }

    private void intersect(Set<AlgorithmType> algorithms) {
        final List<List<CalculatedRefactoring>> refactorings = results.stream()
                .filter(result -> algorithms.contains(result.getAlgorithmType()))
                .map(AlgorithmResult::getRefactorings)
                .collect(Collectors.toList());
        // todo may be should use combine instead of intersect
        final List<CalculatedRefactoring> intersection = RefactoringUtil.intersect(refactorings);
        if (!algorithms.isEmpty()) {
            final String tabName = algorithms.stream()
                    .map(Enum::toString)
                    .collect(Collectors.joining(" & "));
            addTab(tabName, intersection, true);
        }
    }

    private class IntersectAction extends AnAction {
        IntersectAction() {
            super(ArchitectureReloadedBundle.message("intersect.action.text"),
                    ArchitectureReloadedBundle.message("intersect.action.description"),
                    AllIcons.Actions.Edit);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            if (results != null) {
                final Set<AlgorithmType> algorithms = results.stream()
                        .map(AlgorithmResult::getAlgorithmType)
                        .collect(Collectors.toSet());
                final IntersectionDialog dialog = new IntersectionDialog(project, algorithms);
                dialog.show();
                if (dialog.isOK()) {
                    intersect(dialog.getSelected());
                }
            }
        }
    }

    private class InfoAction extends AnAction {
        InfoAction() {
            super(ArchitectureReloadedBundle.message("info.action.text"),
                    ArchitectureReloadedBundle.message("info.action.description"),
                    AllIcons.Actions.Help);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            if (results != null && entitiesStorage != null) {
                final DialogWrapper dialog = new ExecutionInfoDialog(project, entitiesStorage, results);
                dialog.show();
            }
        }
    }

    private class ColorAction extends ToggleAction {
        private static final String COLOR_ACTION_ICON_PATH = "/images/color.png";

        ColorAction() {
            super(ArchitectureReloadedBundle.message("color.action.text"),
                    ArchitectureReloadedBundle.message("color.action.description"),
                    IconLoader.getIcon(COLOR_ACTION_ICON_PATH));
        }

        @Override
        public boolean isSelected(AnActionEvent e) {
            return enableHighlighting;
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            enableHighlighting = state;
            contents.forEach(panel -> panel.setEnableHighlighting(enableHighlighting));
        }
    }

    private class CloseAction extends AnAction {
        CloseAction() {
            super(ArchitectureReloadedBundle.message("close.action.text"),
                    ArchitectureReloadedBundle.message("close.action.description"),
                    AllIcons.Actions.Close);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            myToolWindow.setAvailable(false, null);
            results = null;
            scope = null;
        }
    }

    private class ExcludeFieldRefactoringsAction extends ToggleAction {
        ExcludeFieldRefactoringsAction() {
            super(ArchitectureReloadedBundle.message("exclude.field.refactorings.action.text"),
                    ArchitectureReloadedBundle.message("exclude.field.refactorings.action.description"),
                    AllIcons.Actions.Exclude);
        }

        @Override
        public boolean isSelected(AnActionEvent e) {
            return excludeFieldRefactorings;
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            excludeFieldRefactorings = state;
            contents.forEach(panel -> panel.excludeFieldRefactorings(excludeFieldRefactorings));
        }
    }
}

