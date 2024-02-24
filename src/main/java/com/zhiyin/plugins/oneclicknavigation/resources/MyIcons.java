package com.zhiyin.plugins.oneclicknavigation.resources;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * 图标资源
 *
 * @author yan on 2024/2/24 23:33
 */
public interface MyIcons {

    /**
     * 右箭头
     */
    Icon rightArrow = IconLoader.getIcon("/icons/arrow_right.svg", MyIcons.class);
    Icon pluginIcon = IconLoader.getIcon("/META-INF/pluginIcon.svg", MyIcons.class);

}
