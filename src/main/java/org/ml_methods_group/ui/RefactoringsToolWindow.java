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

import com.intellij.analysis.AnalysisScope;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.utils.ArchitectureReloadedBundle;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class RefactoringsToolWindow implements Disposable {

    private static final String WINDOW_ID = "Suggested refactorings";
    private static final String TITLE_KEY = "refactorings.tool.window.title";

    private final Project project;
    private ToolWindow myToolWindow = null;
    private Map<String, Map<String, String>> refactorings;
    private AnalysisScope scope;

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

    private void addTab(String algorithmName, @NotNull Map<String, String> refactorings, AnalysisScope scope) {
        final JComponent component = new ClassRefactoringPanel(project, refactorings, scope);
        final ActionToolbar toolbar = createToolbar();
        final JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(component, BorderLayout.CENTER);
        contentPanel.add(toolbar.getComponent(), BorderLayout.WEST);
        final Content content = myToolWindow.getContentManager().getFactory()
                .createContent(contentPanel, algorithmName, true);
        myToolWindow.getContentManager().addContent(content);
    }

    private ActionToolbar createToolbar() {
        final DefaultActionGroup toolbarGroup = new DefaultActionGroup();
        toolbarGroup.add(new IntersectAction());
        toolbarGroup.add(new CloseAction());
        return ActionManager.getInstance()
                .createActionToolbar(WINDOW_ID, toolbarGroup, false);
    }

    public void show(Map<String, Map<String, String>> refactorings, AnalysisScope scope) {
        this.refactorings = refactorings;
        this.scope = scope;
        myToolWindow.getContentManager().removeAllContents(true);
        myToolWindow.setAvailable(false, null);
        for (Map.Entry<String, Map<String, String>> entry : refactorings.entrySet()) {
            addTab(entry.getKey(), entry.getValue(), scope);
        }
        myToolWindow.setAvailable(true, null);
        myToolWindow.show(null);
    }

    @Override
    public void dispose() {
        final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        toolWindowManager.unregisterToolWindow(WINDOW_ID);
        refactorings = null;
        scope = null;
    }

    private void intersect(Set<String> algorithms) {
        HashMap<String, String> results = null;
        for (String algorithm : algorithms) {
            final Map<String, String> result = refactorings.get(algorithm);
            if (results == null) {
                results = new HashMap<>(result);
            }
            results.entrySet().retainAll(result.entrySet());
        }
        if (results != null) {
            final String tabName = algorithms.stream()
                    .collect(Collectors.joining("&"));
            addTab(tabName, results, scope);
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
            if (refactorings != null) {
                final IntersectionDialog dialog = new IntersectionDialog(project, refactorings.keySet());
                dialog.show();
                if (dialog.isOK()) {
                    intersect(dialog.getSelected());
                }
            }
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
            refactorings = null;
            scope = null;
        }
    }
}

