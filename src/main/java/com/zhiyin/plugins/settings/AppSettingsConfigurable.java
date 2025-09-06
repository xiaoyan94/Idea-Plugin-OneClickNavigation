package com.zhiyin.plugins.settings;

import com.intellij.openapi.options.Configurable;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.settings.ui.MySearchScopeItem;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provides controller functionality for application settings.
 */
final class AppSettingsConfigurable implements Configurable {

    private AppSettingsComponent mySettingsComponent;

    // A default constructor with no arguments is required because this implementation
    // is registered in an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "OneClickNavigation插件设置";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new AppSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AppSettingsState settings = AppSettingsState.getInstance();
        boolean isModified = mySettingsComponent.getDefaultCollapseI18nStatus() != settings.defaultCollapseI18nStatus;
        isModified |= !mySettingsComponent.getMapperToDaoSearchScope().getValue().equals(settings.mapperToDaoModuleScope);
        // Add the new field to the modification check
        isModified |= !mySettingsComponent.getCommitTemplateText().equals(settings.commitMessageTemplate);
        return isModified;
    }

    @Override
    public void apply() {
        AppSettingsState settings = AppSettingsState.getInstance();
        settings.defaultCollapseI18nStatus = mySettingsComponent.getDefaultCollapseI18nStatus();
        settings.mapperToDaoModuleScope = mySettingsComponent.getMapperToDaoSearchScope().getValue();
        // Save the new field's value
        settings.commitMessageTemplate = mySettingsComponent.getCommitTemplateText();
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        mySettingsComponent.setDefaultCollapseI18nStatus(settings.defaultCollapseI18nStatus);
        String mapperToDaoModuleScopeValue = settings.mapperToDaoModuleScope;
        if (mapperToDaoModuleScopeValue.equals(Constants.SCOPE_NAME_PROJECT)) {
            mySettingsComponent.setMapperToDaoSearchScope(MySearchScopeItem.PROJECT);
        } else {
            mySettingsComponent.setMapperToDaoSearchScope(MySearchScopeItem.MODULE);
        }
        // Load the saved value into the text field
        mySettingsComponent.setCommitTemplateText(settings.commitMessageTemplate);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}