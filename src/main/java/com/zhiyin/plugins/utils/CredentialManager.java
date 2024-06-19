package com.zhiyin.plugins.utils;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.ui.Messages;

public class CredentialManager {

    /**
     * 保存用户名和密码到密码安全存储。
     *
     * @param username 用户名
     * @param password 密码
     */
    public static void saveCredentials(String key, String username, String password) {
        CredentialAttributes attributes = createCredentialAttributes(key);
        PasswordSafe passwordSafe = PasswordSafe.getInstance();
        Credentials credentials = new Credentials(username, password);
        try {
            passwordSafe.set(attributes, credentials);
        } catch (Exception e) {
            Messages.showErrorDialog("Failed to save credentials.", "Error");
        }
    }

    /**
     * 获取之前保存的用户名和密码。
     *
     * @return 返回Credentials对象，包含用户名和密码；如果未找到则返回null。
     */
    public static Credentials getCredentials(String key) {
        CredentialAttributes attributes = createCredentialAttributes(key);
        PasswordSafe passwordSafe = PasswordSafe.getInstance();
        try {
            return passwordSafe.get(attributes);
        } catch (Exception e) {
            Messages.showErrorDialog("Failed to retrieve credentials.", "Error");
            return null;
        }
    }

    private static CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(
                CredentialAttributesKt.generateServiceName("OneClickNavigation", key)
        );
    }
}
