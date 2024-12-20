package com.zhiyin.plugins.intention;

import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.FoldingModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.IncorrectOperationException;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.ui.MyTranslateDialogWrapper;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JavaScriptI18nTranslateIntentionAction extends PsiElementBaseIntentionAction implements HighPriorityAction {
    /**
     * Invokes intention action for the element under caret.
     *
     * @param project the project in which the file is opened.
     * @param editor  the editor for the file.
     * @param element the element under cursor.
     */
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        JSLiteralExpression jsLiteralExpression = (JSLiteralExpression) element.getParent();
        String text = jsLiteralExpression.getStringValue();
        Module module = MyPsiUtil.getModuleByPsiElement(element);
        List<Property> properties = MyPropertiesUtil.findModuleI18nPropertiesByValue(project, module, text);
        if (!properties.isEmpty()) {
            String key = properties.get(0).getUnescapedKey();
            if (key != null) {
                JSLiteralExpression newPsiElement = JSPsiElementFactory.createJSExpression("\"" + key + "\"", jsLiteralExpression, JSLiteralExpression.class);
                jsLiteralExpression.replace(newPsiElement);
            }
        } else {
            ApplicationManager.getApplication().invokeLater(() -> {
                MyTranslateDialogWrapper myTranslateDialogWrapper = createMyTranslateDialogWrapper(project, module, text);
                if (myTranslateDialogWrapper.showAndGet()) {

                    MyTranslateDialogWrapper.InputModel inputModel = myTranslateDialogWrapper.getInputModel();
                    String key = inputModel.getPropertyKey();
                    JSLiteralExpression newPsiElement = JSPsiElementFactory.createJSExpression("\"" + key + "\"", jsLiteralExpression, JSLiteralExpression.class);
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        jsLiteralExpression.replace(newPsiElement);
                    });

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
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_ZH_CN_SUFFIX, key, finalChsValue);
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_ZH_TW_SUFFIX, key, finalChtValue);
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_EN_US_SUFFIX, key, enValue);
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_VI_VN_SUFFIX, key, finalViValue);
                        });
                    }
                }
            });

        }
    }

    private static @NotNull MyTranslateDialogWrapper createMyTranslateDialogWrapper(@NotNull Project project, Module module, String text) {
        MyTranslateDialogWrapper myTranslateDialogWrapper = new MyTranslateDialogWrapper(project, module);
        if (text != null) {
            myTranslateDialogWrapper.setSourceCHSText(text);
        }
        myTranslateDialogWrapper.setGetI18nPropertiesFun(
                value -> MyPropertiesUtil.findModuleI18nPropertiesByValue(project, module, value)
        );
        myTranslateDialogWrapper.setCheckI18nKeyExistsFun(
                key -> !MyPropertiesUtil.findModuleI18nProperties(project, module, key).isEmpty()
        );
        return myTranslateDialogWrapper;
    }

    /**
     * Checks whether this intention is available at a caret offset in file.
     * If this method returns true, a light bulb for this intention is shown.
     *
     * @param project the project in which the availability is checked.
     * @param editor  the editor in which the intention will be invoked.
     * @param element the element under caret.
     * @return true if the intention is available, false otherwise.
     */
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (!(element.getContainingFile() instanceof XmlFile)) {
            return false;
        }
        Module module = MyPsiUtil.getModuleByPsiElement(element);
        if (module == null) {
            return false;
        }

        if (element.getParent() instanceof JSLiteralExpression) {
            if (element.getParent().getParent() instanceof JSArgumentList
                    && element.getParent().getParent().getPrevSibling() instanceof JSReferenceExpression
            ) {
                JSReferenceExpression jsReferenceExpression = (JSReferenceExpression) element.getParent().getParent().getPrevSibling();
                return "zhiyin.i18n.translate".equals(jsReferenceExpression.getCanonicalText());
            }
        }

        return false;
    }

    /**
     * Returns the name of the family of intentions. It is used to externalize
     * "auto-show" state of intentions. When the user clicks on a light bulb in intention list,
     * all intentions with the same family name get enabled/disabled.
     * The name is also shown in settings tree.
     *
     * @return the intention family name.
     */
    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "JS: 自动翻译并替换i18n key";
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return getFamilyName();
    }

    /**
     * Indicate whether this action should be invoked inside write action.
     * @return false
     */
    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
