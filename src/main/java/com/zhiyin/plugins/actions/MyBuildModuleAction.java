package com.zhiyin.plugins.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MyBuildModuleAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 获取当前项目
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // 1. 获取 ActionManager 实例
        ActionManager actionManager = ActionManager.getInstance();

        // 2. 查找并获取内置的 "Build Module" Action
        // "MakeModule" 是该 Action 的内置 ID
        AnAction compileAction = actionManager.getAction("MakeModule");

        if (compileAction != null) {
            // 3. 执行内置 Action
            // 这里我们创建一个新的 AnActionEvent，但使用原始事件的 DataContext
            compileAction.actionPerformed(e);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 1. 获取项目和模块信息
        Project project = e.getProject();
        Module module = e.getData(PlatformDataKeys.MODULE);

        // 获取按钮的 Presentation 对象
        e.getPresentation()
         .setEnabled(project != null && module != null && !CompilerManager.getInstance(project).isCompilationActive());

        // 2. 如果模块存在，则动态更新文本
        if (module != null) {
            String moduleName = module.getName();
            e.getPresentation().setText("Build Module: " + moduleName);
        } else {
            // 如果没有可用的模块，显示通用文本
            e.getPresentation().setText("Build Module");
        }

    }
}