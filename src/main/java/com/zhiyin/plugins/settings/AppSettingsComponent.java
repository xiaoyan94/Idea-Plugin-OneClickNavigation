package com.zhiyin.plugins.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextArea;
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

    // Add a new text field for the commit template
    private final JBTextArea commitTemplateText = new JBTextArea();

    public AppSettingsComponent() {
        mapperToDaoSearchScope.addItem(MySearchScopeItem.MODULE);
        mapperToDaoSearchScope.addItem(MySearchScopeItem.PROJECT);
        commitTemplateText.setRows(5);
        commitTemplateText.setLineWrap(true);
        commitTemplateText.setWrapStyleWord(true);
        commitTemplateText.setBorder(BorderFactory.createTitledBorder("Commit message template"));
        // 设置为微软雅黑
        commitTemplateText.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("I18n", new JSeparator())
                .addComponent(defaultCollapseI18nStatus)
                .addVerticalGap(20)
                .addLabeledComponent("Search scope", new JSeparator())
                .addLabeledComponent("Mapper to dao scope", mapperToDaoSearchScope)
                .addVerticalGap(20)
                // Add the new text field with a label
                .addLabeledComponent("SVN提交模板自定义前缀", commitTemplateText)
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

    // Add getter and setter for the new text field
    public String getCommitTemplateText() {
        if (commitTemplateText.getText() == null || commitTemplateText.getText().isEmpty()) {
            setCommitTemplateText("问题单号：DTS20151135000\n修改人：\n修改描述：\n");
        }
        return commitTemplateText.getText();
    }

    public void setCommitTemplateText(String newText) {
        commitTemplateText.setText(newText);
    }

}