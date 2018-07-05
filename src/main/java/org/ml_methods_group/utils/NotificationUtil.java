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

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import org.jetbrains.annotations.NotNull;

public final class NotificationUtil {
    private static String NOTIFICATION_GROUP_ID = "Architecture Reloaded Notifications";

    public static void notifyEmptyScope(@NotNull Project project) {
        notify(project, getEmptyScopeNotification(project));
    }

    private static void notify(@NotNull Project project, @NotNull Notification n) {
        Notifications.Bus.notify(n, project);
    }

    private static Notification getEmptyScopeNotification(@NotNull Project project) {
        Notification n = new Notification(NOTIFICATION_GROUP_ID,
                ArchitectureReloadedBundle.message("empty.scope.notification.title"),
                ArchitectureReloadedBundle.message("empty.scope.notification.message"),
                NotificationType.INFORMATION);
        n.addAction(new NotificationAction(ArchitectureReloadedBundle.message("open.modules.configuration.action.text")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                ModulesConfigurator.showDialog(project, null, null);
            }
        });
        return n;
    }
}
