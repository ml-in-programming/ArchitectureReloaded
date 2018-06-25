/*
 * Copyright 2018 Machine Learning Methods in Software Engineering Group of JetBrains Research
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

package org.ml_methods_group.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DialogUtil {
    public static void simpleDialog(@NotNull Project project, @NotNull String title, @NotNull String message) {
        DialogBuilder db = new DialogBuilder(project);
        db.setTitle(title);
        db.setCenterPanel(getSimpleDialogBody(message));
        db.resizable(false);
        db.removeAllActions();
        db.addOkAction();
        ApplicationManager.getApplication().invokeLater(db::show);
    }

    private static JComponent getSimpleDialogBody(@NotNull String message) {
        return new JLabel(message);
    }
}
