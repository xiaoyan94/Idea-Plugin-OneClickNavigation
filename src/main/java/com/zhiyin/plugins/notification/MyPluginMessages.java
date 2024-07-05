package com.zhiyin.plugins.notification;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.resources.MyIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MyPluginMessages {
    private MyPluginMessages() {
    }

    public static void showInfo(String title, String message) {
        showInfo(title, message, null);
    }

    public static void showInfo(String title, String message, @Nullable Project project) {
        final Notification notification = new Notification(Constants.NOTIFICATION_GROUP_ID, title, message, NotificationType.INFORMATION);
        notification.setIcon(MyIcons.pandaIconSVG16_2);
        Notifications.Bus.notify(notification, project);
    }

    public static void showError(String title, String message, @Nullable Project project) {
        Notification notification = new Notification(Constants.NOTIFICATION_GROUP_ID, title, message, NotificationType.ERROR);
        notification.setIcon(MyIcons.pandaIconSVG16_2);
        Notifications.Bus.notify(notification, project);
    }

    public static void showWarning(String title, String message, @Nullable Project project) {
        Notification notification = new Notification(Constants.NOTIFICATION_GROUP_ID, title, message, NotificationType.WARNING);
//        notification.setListener(NotificationListener.URL_OPENING_LISTENER);
        Notifications.Bus.notify(notification, project);
    }

    public static int showDialog(@Nullable Project project, @NotNull String message, @NotNull @Nls String title, @NotNull String[] options, int defaultOptionIndex) {
        return Messages.showIdeaMessageDialog(project, message, title, options, defaultOptionIndex, MyIcons.pandaIconSVG32_2, null);
    }
}
