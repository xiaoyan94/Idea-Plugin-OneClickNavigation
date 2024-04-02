package com.zhiyin.plugins.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.zhiyin.plugins.resources.Constants;
import org.jetbrains.annotations.NotNull;

/**
 * Supports storing the application settings in a persistent way.
 * The {@link State} and {@link Storage} annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 */
@State(
    name = "com.zhiyin.plugins.settings.AppSettingsState",
    storages = @Storage("OneClickNavigationPluginSettings.xml")
)
@Service
public final class AppSettingsState implements PersistentStateComponent<AppSettingsState> {

  /**
   * 默认是否折叠i18n
   */
  public boolean defaultCollapseI18nStatus = true;

  /**
   * Project | Module
   */
  public String mapperToDaoModuleScope = Constants.SCOPE_NAME_MODULE;

  public static AppSettingsState getInstance() {
    return ApplicationManager.getApplication().getService(AppSettingsState.class);
  }

  @Override
  public @NotNull AppSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull AppSettingsState state) {
    XmlSerializerUtil.copyBean(state, this);
  }

}