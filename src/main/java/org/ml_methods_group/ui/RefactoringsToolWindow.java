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
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.utils.ArchitectureReloadedBundle;

import java.util.Map;

public final class RefactoringsToolWindow implements Disposable {

    private static final String WINDOW_ID = "Suggested refactorings";
    private static final String TITLE_KEY = "refactorings.tool.window.title";

    private final Project project;
    private ToolWindow myToolWindow = null;

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

    public RefactoringsToolWindow addTab(String algorithmName, @NotNull Map<String, String> refactorings, AnalysisScope scope) {
        final Content content = myToolWindow.getContentManager().getFactory()
                .createContent(new ClassRefactoringPanel(project, refactorings, scope), algorithmName, true);
        myToolWindow.getContentManager().addContent(content);
        return this;
    }

    public RefactoringsToolWindow clear() {
        myToolWindow.getContentManager().removeAllContents(true);
        myToolWindow.setAvailable(false, null);
        return this;
    }

    public void show() {
        myToolWindow.setAvailable(true, null);
        myToolWindow.show(null);
    }

    @Override
    public void dispose() {
        final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        toolWindowManager.unregisterToolWindow(WINDOW_ID);
    }
}

