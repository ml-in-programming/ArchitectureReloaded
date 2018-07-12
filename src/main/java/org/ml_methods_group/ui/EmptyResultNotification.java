package org.ml_methods_group.ui;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.jetbrains.annotations.NotNull;

public class EmptyResultNotification extends Notification {
    private final static String GROUP_DISPLAY_ID = "Search For Refactorings";
    private final static String TITLE = "Empty Result";
    private final static String CONTENT = "No refactorings were found";
    private final static NotificationType NOTIFICATION_TYPE = NotificationType.INFORMATION;

    @NotNull
    public static EmptyResultNotification createDefaultEmptyResultNotification() {
        return new EmptyResultNotification(GROUP_DISPLAY_ID, TITLE, CONTENT, NOTIFICATION_TYPE);
    }

    private EmptyResultNotification(@NotNull String groupDisplayId, @NotNull String title, @NotNull String content, @NotNull NotificationType type) {
        super(groupDisplayId, title, content, type);
    }
}
