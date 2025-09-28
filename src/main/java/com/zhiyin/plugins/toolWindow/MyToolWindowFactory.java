package com.zhiyin.plugins.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.zhiyin.plugins.service.MyToolWindowService;
import org.jetbrains.annotations.NotNull;

public class MyToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MyToolWindowUI ui = new MyToolWindowUI(project, toolWindow);

        // 注册到 ProjectService
        project.getService(MyToolWindowService.class).setUI(ui);

        toolWindow.getComponent().add(ui.getContent());
    }
}
