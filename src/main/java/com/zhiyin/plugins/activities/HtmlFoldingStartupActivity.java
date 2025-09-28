package com.zhiyin.plugins.activities;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.zhiyin.plugins.component.HtmlFoldingProjectService;
import org.jetbrains.annotations.NotNull;

/**
 * 因 Service 是懒加载，这个监听器只是为了触发 Service 的构造函数
 */
public class HtmlFoldingStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        // 主动获取 Service，从而触发构造函数里的 Listener 注册
        HtmlFoldingProjectService.getInstance(project);
    }
}
