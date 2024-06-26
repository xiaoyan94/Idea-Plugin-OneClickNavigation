package com.zhiyin.plugins.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.panels.RowGridLayout;
import com.intellij.util.ui.FormBuilder;
import com.zhiyin.plugins.settings.ui.MySearchScopeItem;

import javax.swing.*;
import java.awt.*;

/**
 * Supports creating and managing a {@link JPanel} for the Settings Dialog.
 */
public class AppSettingsComponent {

    private final JPanel myMainPanel;

    private final JBCheckBox defaultCollapseI18nStatus = new JBCheckBox("I18n资源串默认折叠显示");

    private final ComboBox<MySearchScopeItem> mapperToDaoSearchScope = new ComboBox<>();

    public AppSettingsComponent() {
        mapperToDaoSearchScope.addItem(MySearchScopeItem.MODULE);
        mapperToDaoSearchScope.addItem(MySearchScopeItem.PROJECT);
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("I18n", new JSeparator())
                .addComponent(defaultCollapseI18nStatus)
                .addVerticalGap(20)
                .addLabeledComponent("Search scope", new JSeparator())
                .addLabeledComponent("Mapper to dao scope", mapperToDaoSearchScope)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return defaultCollapseI18nStatus;
    }

    public boolean getDefaultCollapseI18nStatus() {
        return defaultCollapseI18nStatus.isSelected();
    }

    public void setDefaultCollapseI18nStatus(boolean newStatus) {
        defaultCollapseI18nStatus.setSelected(newStatus);
    }

    public MySearchScopeItem getMapperToDaoSearchScope() {
        return mapperToDaoSearchScope.getItem();
    }

    public void setMapperToDaoSearchScope(MySearchScopeItem newScope) {
        mapperToDaoSearchScope.setItem(newScope);
    }

}