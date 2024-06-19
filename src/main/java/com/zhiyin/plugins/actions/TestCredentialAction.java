package com.zhiyin.plugins.actions;

import com.intellij.credentialStore.OneTimeString;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.credentialStore.Credentials;
import com.zhiyin.plugins.utils.CredentialManager;

public class TestCredentialAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 弹出输入对话框获取用户名和密码
        String username = Messages.showInputDialog("Enter your username:", "Username", Messages.getQuestionIcon());
        String password = Messages.showPasswordDialog(e.getProject(),"Enter your password:", "Password", AllIcons.General.ArrowLeft);

        // 如果用户名或密码为空，不执行后续操作
        if (username == null || password == null) {
            return;
        }

        String key = "OneClickNavigationTest";
        CredentialManager.saveCredentials(key, username, password);

        Credentials storedCredentials = CredentialManager.getCredentials(key);
        if (storedCredentials != null) {
            String storedUsername = storedCredentials.getUserName();
            String storedPassword = storedCredentials.getPasswordAsString();
            OneTimeString oneTimeString = storedCredentials.getPassword();
            // 使用存储的用户名和密码进行后续操作
            Messages.showMessageDialog("Stored Username: " + storedUsername + "\nStored Password: " + storedPassword
                    + "\nOneTimeString: " + oneTimeString, "Stored Credentials", Messages.getInformationIcon());
        }
    }

}
