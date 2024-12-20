package com.zhiyin.plugins.intention;

import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.IncorrectOperationException;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.ui.MyTranslateDialogWrapper;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

public class FreemarkerI18nTranslateIntentionAction extends PsiElementBaseIntentionAction implements HighPriorityAction {
    @Override
    public @IntentionName @NotNull String getText() {
        return this.getFamilyName();
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "FreeMarker: 自动翻译并替换i18n key";
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        String keyValue = MyPsiUtil.retrieveI18nKeyFromFreemarkerDirective(element);
        Module module = MyPsiUtil.getModuleByPsiElement(element);

        MyTranslateDialogWrapper myTranslateDialogWrapper = createMyTranslateDialogWrapper(project, module, keyValue);
        ApplicationManager.getApplication().invokeLater(() -> {
            if (myTranslateDialogWrapper.showAndGet()) {
                MyTranslateDialogWrapper.InputModel inputModel = myTranslateDialogWrapper.getInputModel();
                String key = inputModel.getPropertyKey();
//                JSLiteralExpression newPsiElement = JSPsiElementFactory.createJSExpression("\"" + key + "\"", jsLiteralExpression, JSLiteralExpression.class);
//                WriteCommandAction.runWriteCommandAction(project, () -> {
//                    jsLiteralExpression.replace(newPsiElement);
//                });
//                PsiElementFactory factory = PsiElementFactory.getInstance(project);
                XmlElementFactory xmlElementFactory = XmlElementFactory.getInstance(project);
                XmlText xmlText = xmlElementFactory.createDisplayText("<@message key=\"" + key + "\"/>");
                element.replace(xmlText);

                if (!inputModel.getPropertyKeyExists()) {
                    boolean isNative2AsciiForPropertiesFiles = MyPropertiesUtil.isNative2AsciiForPropertiesFiles();
                    String chsValue = inputModel.getChinese();
                    String chtValue = inputModel.getChineseTW();
                    String enValue = inputModel.getEnglish();
                    String viValue = inputModel.getVietnamese();
                    if (!isNative2AsciiForPropertiesFiles) {
                        chsValue = inputModel.getChineseUnicode();
                        chtValue = inputModel.getChineseTWUnicode();
//                        enValue = inputModel.getEnglishUnicode();
                        viValue = inputModel.getVietnameseUnicode();
                    }

                    String finalChsValue = chsValue;
                    String finalChtValue = chtValue;
                    String finalViValue = viValue;
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_WEB_ZH_CN, key, finalChsValue);
                        MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_WEB_ZH_TW, key, finalChtValue);
                        MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_WEB_EN_US, key, enValue);
                        MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_WEB_VI_VN, key, finalViValue);
                    });
                }
            }
        });
    }

    private static @NotNull MyTranslateDialogWrapper createMyTranslateDialogWrapper(@NotNull Project project, Module module, String text) {
        MyTranslateDialogWrapper myTranslateDialogWrapper = new MyTranslateDialogWrapper(project, module);
        if (text != null) {
            myTranslateDialogWrapper.setSourceCHSText(text);
        }
        myTranslateDialogWrapper.setGetI18nPropertiesFun(
                value -> MyPropertiesUtil.findModuleWebI18nPropertiesByValue(project, module, value)
        );
        myTranslateDialogWrapper.setCheckI18nKeyExistsFun(
                key -> !MyPropertiesUtil.findModuleWebI18nProperties(project, module, key).isEmpty()
        );
        return myTranslateDialogWrapper;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        Module module = MyPsiUtil.getModuleByPsiElement(element);
        if (module == null) {
            return false;
        }

        String key = MyPsiUtil.retrieveI18nKeyFromFreemarkerDirective(element);
        return key != null;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
