package com.zhiyin.plugins.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.zhiyin.plugins.notification.MyPluginMessages;
import com.zhiyin.plugins.utils.TranslateUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 清除翻译缓存
 */
public class ClearTranslationCacheAction extends AnAction {
    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        doAction();
    }

    public static void doAction() {
        TranslateUtil.clearTranslationCache();
        MyPluginMessages.showInfo("提示", "翻译缓存已清空");
    }
}
