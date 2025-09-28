package com.zhiyin.plugins.service;

import com.intellij.openapi.components.Service;
import com.zhiyin.plugins.toolWindow.MyToolWindowUI;
import org.jetbrains.annotations.Nullable;

@Service(Service.Level.PROJECT)
public final class MyToolWindowService {
    private MyToolWindowUI ui;

    public void setUI(MyToolWindowUI ui) {
        this.ui = ui;
    }

    @Nullable
    public MyToolWindowUI getUI() {
        return ui;
    }
}
