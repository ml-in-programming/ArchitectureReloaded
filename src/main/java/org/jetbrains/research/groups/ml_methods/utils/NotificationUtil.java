package org.jetbrains.research.groups.ml_methods.utils;

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

    public static void notifyEmptyResult(@NotNull Project project) {
        notify(project, getEmptyResultNotification());
    }

    private static void notify(@NotNull Project project, @NotNull Notification n) {
        Notifications.Bus.notify(n, project);
    }

    @NotNull
    private static Notification getEmptyResultNotification() {
        return new Notification(NOTIFICATION_GROUP_ID,
                ArchitectureReloadedBundle.message("empty.result.notification.title"),
                ArchitectureReloadedBundle.message("empty.result.notification.message"),
                NotificationType.INFORMATION);
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
