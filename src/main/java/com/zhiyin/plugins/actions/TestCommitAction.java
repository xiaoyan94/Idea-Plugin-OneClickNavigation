// src/main/java/com/yourpackage/TestCommitAction.java
package com.zhiyin.plugins.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.ui.CommitMessage;
import com.zhiyin.plugins.notification.MyPluginMessages;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;

/**
 * 最简单的测试Action
 * 用于测试获取和修改VCS提交信息的核心功能
 */
public class TestCommitAction extends AnAction {

    public TestCommitAction() {
        // super("添加前缀模板", "Tips:一键添加定义好的模板作为前缀", MyIcons.pandaIconSVG16_2);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 直接使用CommitMessage接口
        CommitMessage commitMessage = (CommitMessage) e.getDataContext().getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL);

        // Get the template from the settings
        String prefixTemplate = AppSettingsState.getInstance().commitMessageTemplate;
        if (commitMessage != null) {
            String currentText = commitMessage.getComment();
            if (currentText.startsWith(prefixTemplate)) {
                MyPluginMessages.showInfo("已添加前缀模板", "已添加前缀模板: " + prefixTemplate, e.getProject());
                return;
            }
            String newText = prefixTemplate + currentText;
            commitMessage.setCommitMessage(newText);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Get the Presentation object from the event
        Presentation presentation = e.getPresentation();

        // Set the text
        presentation.setText("添加前缀模板");

        // Set the description
        presentation.setDescription("Tips:一键添加定义好的模板作为前缀");

        // Set the icon
        presentation.setIcon(MyIcons.pandaIconSVG16_2);

        Object commitMessageControl = e.getDataContext().getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL);
        e.getPresentation().setEnabledAndVisible(commitMessageControl instanceof CommitMessage);
    }
}