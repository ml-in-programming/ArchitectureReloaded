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

package org.ml_methods_group.refactoring;

import com.intellij.AppTopics;
import com.intellij.analysis.AnalysisScope;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.plugin.AutomaticRefactoringAction;

public class RefactoringOnFileSaved implements ApplicationComponent {
    @Override
    public void initComponent() {
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();

        MessageBusConnection connection = bus.connect();

        connection.subscribe(AppTopics.FILE_DOCUMENT_SYNC, new FileDocumentManagerAdapter() {
            @Override
            public void beforeDocumentSaving(@NotNull Document document) {
                super.beforeDocumentSaving(document);

                final VirtualFile savedFile = FileDocumentManager.getInstance().getFile(document);
                if (savedFile == null || !savedFile.getFileType().equals(JavaFileType.INSTANCE)) {
                    return;
                }

                for (Project project : ProjectManager.getInstance().getOpenProjects()) {
                    if (!project.isDefault() && project.isInitialized() && !project.isDisposed()
                            && ProjectRootManager.getInstance(project).getFileIndex().isInContent(savedFile)) {
                        AutomaticRefactoringAction.getInstance(project).analyzeBackground(project,
                                new AnalysisScope(project),
                                project.getName() + project.getLocationHash() + "|saved");
                    }
                }
            }
        });
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Recalculating autorefactorings after saving";
    }
}
