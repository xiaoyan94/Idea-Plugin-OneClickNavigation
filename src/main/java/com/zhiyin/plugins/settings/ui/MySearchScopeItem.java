package com.zhiyin.plugins.settings.ui;

import com.zhiyin.plugins.resources.Constants;

public class MySearchScopeItem{
    private final String name;
    private final String value;

    public static final MySearchScopeItem PROJECT = create("项目", Constants.SCOPE_NAME_PROJECT);

    public static final MySearchScopeItem MODULE = create("模块", Constants.SCOPE_NAME_MODULE);
    private static MySearchScopeItem create(String name, String value) {
        return new MySearchScopeItem(name, value);
    }

    private MySearchScopeItem(String name, String value) {
        if(name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("名称不能为空");
        }
        if(value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("值不能为空");
        }
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
