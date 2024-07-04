package com.zhiyin.plugins.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.zhiyin.plugins.notification.MyPluginMessages;
import org.jetbrains.annotations.NotNull;

/**
 * 显示当前打开文件PsiFile的language信息
 */
public class ShowLanguageAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            Messages.showErrorDialog("No active project found", "Error");
            return;
        }

        // 获取当前打开的文件
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) {
            Messages.showErrorDialog("No file selected", "Error");
            return;
        }

        // 获取PsiFile
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile == null) {
            Messages.showErrorDialog("No PsiFile found", "Error");
            return;
        }

        // 获取语言信息并显示
        String languageName = psiFile.getLanguage().getDisplayName();
        MyPluginMessages.showInfo("File Language", "The language of the current file is: " + languageName, project);
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        // 这里可以添加逻辑，根据当前上下文是否有打开文件来决定是否启用动作
        e.getPresentation().setEnabled(e.getData(CommonDataKeys.VIRTUAL_FILE) != null);
    }
}
