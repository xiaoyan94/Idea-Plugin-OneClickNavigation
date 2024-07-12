package com.zhiyin.plugins.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.zhiyin.plugins.notification.MyPluginMessages;
import com.zhiyin.plugins.utils.ProjectTypeChecker;
import org.jetbrains.annotations.NotNull;

public class ProjectTypeCheckAction extends AnAction {
    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        boolean traditionalJavaWebProject = ProjectTypeChecker.isTraditionalJavaWebProject(e.getProject(), e.getRequiredData(PlatformDataKeys.MODULE));
        MyPluginMessages.showInfo("Project Type Check", traditionalJavaWebProject ? "Traditional Java Web Project" : "Spring Boot Project");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Project project = e.getData(PlatformDataKeys.PROJECT);
        Module module = e.getData(PlatformDataKeys.MODULE);
        e.getPresentation().setEnabledAndVisible(project != null && module != null);
    }

    @Override
    public boolean isDumbAware() {
        return super.isDumbAware();
    }
}
