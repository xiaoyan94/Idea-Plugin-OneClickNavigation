package com.zhiyin.plugins.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.encoding.EncodingManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.ui.MyTranslateDialogWrapper;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class XMLTranslateIntentionAction extends PsiElementBaseIntentionAction implements IntentionAction {
    private final Logger logger = Logger.getInstance(XMLTranslateIntentionAction.class);


    /**
     * Invokes intention action for the element under caret.
     *
     * @param project the project in which the file is opened.
     * @param editor  the editor for the file.
     * @param element the element under cursor.
     */
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        XmlTag parentXmlTag = MyPsiUtil.getParentXmlTag(element);
        Module module = MyPsiUtil.getModuleByPsiElement(element);

        final Function<String, List<Property>> alreadyExistsPropertyFun = getAlreadyExistsDataGridPropertyFun(project, module);
        final Predicate<String> checkNewKeyRepeatExistsFun = getCheckDataGridNewKeyRepeatExistsFun(project, module);

        if (parentXmlTag != null && "Title".equals(parentXmlTag.getName())) {
            ApplicationManager.getApplication().invokeLater(() -> {
                String sourceText = parentXmlTag.getAttributeValue("value");
                if (sourceText == null) {
                    sourceText = parentXmlTag.getAttributeValue("chs");
                }
                if (sourceText == null) {
                    sourceText = "";
                }

//                System.out.println("Current thread is " + Thread.currentThread().getName() + " sourceText:" + sourceText);
                MyTranslateDialogWrapper myTranslateDialogWrapper = new MyTranslateDialogWrapper(project, module);
                myTranslateDialogWrapper.setSourceCHSText(sourceText);
                myTranslateDialogWrapper.setGetI18nPropertiesFun(alreadyExistsPropertyFun);
                myTranslateDialogWrapper.setCheckI18nKeyExistsFun(checkNewKeyRepeatExistsFun);
                if (myTranslateDialogWrapper.showAndGet()) {
                    MyTranslateDialogWrapper.InputModel inputModel = myTranslateDialogWrapper.getInputModel();
                    String propertyKey = inputModel.getPropertyKey();
//                    System.out.println("Current thread is " + Thread.currentThread().getName() + " propertyKey:" + propertyKey);
                    // 写操作
                    WriteCommandAction.runWriteCommandAction(project, () -> {
//                        System.out.println("Current thread is " + Thread.currentThread().getName() + " propertyKey:" + propertyKey);
                        String enValue = inputModel.getEnglish();
                        parentXmlTag.setAttribute("value", propertyKey);
                        parentXmlTag.setAttribute("chs", inputModel.getChinese());
                        parentXmlTag.setAttribute("eng", enValue);
                        // 写入Properties文件
                        if (!inputModel.getPropertyKeyExists()) {
                            boolean native2AsciiForPropertiesFiles = EncodingManager.getInstance().isNative2AsciiForPropertiesFiles();
                            logger.debug("XMLTranslateIntentionAction.invoke on XmlTag<Title>: native2AsciiForPropertiesFiles:" + native2AsciiForPropertiesFiles);
                            String chsValue = native2AsciiForPropertiesFiles ? inputModel.getChinese() : inputModel.getChineseUnicode();
                            String chtValue = native2AsciiForPropertiesFiles ? inputModel.getChineseTW() : inputModel.getChineseTWUnicode();
                            String viValue = native2AsciiForPropertiesFiles ? inputModel.getVietnamese() : inputModel.getVietnameseUnicode();
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_DATAGRID_ZH_CN_SUFFIX, inputModel.getPropertyKey(), chsValue, true);
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_DATAGRID_ZH_TW_SUFFIX, inputModel.getPropertyKey(), chtValue, true);
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_DATAGRID_EN_US_SUFFIX, inputModel.getPropertyKey(), enValue, true);
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_DATAGRID_VI_VN_SUFFIX, inputModel.getPropertyKey(), viValue, true);
                        }
                    });
                }
            });
        }
        else if (parentXmlTag != null && "Field".equals(parentXmlTag.getName())) {
            ApplicationManager.getApplication().invokeLater(() -> {
                String sourceText = parentXmlTag.getAttributeValue("label");
                if (sourceText == null) {
                    sourceText = parentXmlTag.getAttributeValue("id");
                }
                if (sourceText == null) {
                    sourceText = "";
                }

//                System.out.println("Current thread is " + Thread.currentThread().getName() + " sourceText:" + sourceText);
                MyTranslateDialogWrapper myTranslateDialogWrapper = new MyTranslateDialogWrapper(project, module);
                myTranslateDialogWrapper.setSourceCHSText(sourceText);
                myTranslateDialogWrapper.setGetI18nPropertiesFun(alreadyExistsPropertyFun);
                myTranslateDialogWrapper.setCheckI18nKeyExistsFun(checkNewKeyRepeatExistsFun);
                if (myTranslateDialogWrapper.showAndGet()) {
                    MyTranslateDialogWrapper.InputModel inputModel = myTranslateDialogWrapper.getInputModel();
                    String propertyKey = inputModel.getPropertyKey();
//                    System.out.println("Current thread is " + Thread.currentThread().getName() + " propertyKey:" + propertyKey);
                    // 写操作
                    WriteCommandAction.runWriteCommandAction(project, () -> {
//                        System.out.println("Current thread is " + Thread.currentThread().getName() + " propertyKey:" + propertyKey);
                        String enValue = inputModel.getEnglish();
                        parentXmlTag.setAttribute("label", propertyKey);
                        // 写入Properties文件
                        if (!inputModel.getPropertyKeyExists()) {
                            boolean native2AsciiForPropertiesFiles = EncodingManager.getInstance().isNative2AsciiForPropertiesFiles();
                            logger.debug("XMLTranslateIntentionAction.invoke on XmlTag<Field>: native2AsciiForPropertiesFiles:" + native2AsciiForPropertiesFiles);
                            String chsValue = native2AsciiForPropertiesFiles ? inputModel.getChinese() : inputModel.getChineseUnicode();
                            String chtValue = native2AsciiForPropertiesFiles ? inputModel.getChineseTW() : inputModel.getChineseTWUnicode();
                            String viValue = native2AsciiForPropertiesFiles ? inputModel.getVietnamese() : inputModel.getVietnameseUnicode();
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_DATAGRID_ZH_CN_SUFFIX, inputModel.getPropertyKey(), chsValue);
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_DATAGRID_ZH_TW_SUFFIX, inputModel.getPropertyKey(), chtValue);
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_DATAGRID_EN_US_SUFFIX, inputModel.getPropertyKey(), enValue);
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_DATAGRID_VI_VN_SUFFIX, inputModel.getPropertyKey(), viValue);
                        }
                    });
                }
            });
        }
        else if (parentXmlTag != null && "column".equals(parentXmlTag.getName())){
            ApplicationManager.getApplication().invokeLater(() -> {
                String sourceText = parentXmlTag.getAttributeValue("description");
                if (sourceText == null) {
                    sourceText = parentXmlTag.getAttributeValue("name");
                }
                if (sourceText == null) {
                    sourceText = "";
                }

//                System.out.println("Current thread is " + Thread.currentThread().getName() + " sourceText:" + sourceText);
                MyTranslateDialogWrapper myTranslateDialogWrapper = new MyTranslateDialogWrapper(project, module);
                myTranslateDialogWrapper.setSourceCHSText(sourceText);
                myTranslateDialogWrapper.setGetI18nPropertiesFun(
                        value -> MyPropertiesUtil.findModuleI18nPropertiesByValue(project, module, value)
                );
                myTranslateDialogWrapper.setCheckI18nKeyExistsFun(
                        key -> !MyPropertiesUtil.findModuleI18nProperties(project, module, key).isEmpty()
                );
                if (myTranslateDialogWrapper.showAndGet()) {
                    MyTranslateDialogWrapper.InputModel inputModel = myTranslateDialogWrapper.getInputModel();
                    String propertyKey = inputModel.getPropertyKey();
//                    System.out.println("Current thread is " + Thread.currentThread().getName() + " propertyKey:" + propertyKey);
                    // 写操作
                    WriteCommandAction.runWriteCommandAction(project, () -> {
//                        System.out.println("Current thread is " + Thread.currentThread().getName() + " propertyKey:" + propertyKey);
                        String enValue = inputModel.getEnglish();
                        parentXmlTag.setAttribute("i18n", propertyKey);
                        // 写入Properties文件
                        if (!inputModel.getPropertyKeyExists()) {
                            boolean native2AsciiForPropertiesFiles = EncodingManager.getInstance().isNative2AsciiForPropertiesFiles();
                            logger.debug("XMLTranslateIntentionAction.invoke on XmlTag<column>: native2AsciiForPropertiesFiles:" + native2AsciiForPropertiesFiles);
                            String chsValue = native2AsciiForPropertiesFiles ? inputModel.getChinese() : inputModel.getChineseUnicode();
                            String chtValue = native2AsciiForPropertiesFiles ? inputModel.getChineseTW() : inputModel.getChineseTWUnicode();
                            String viValue = native2AsciiForPropertiesFiles ? inputModel.getVietnamese() : inputModel.getVietnameseUnicode();
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_ZH_CN_SUFFIX, inputModel.getPropertyKey(), chsValue);
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_ZH_TW_SUFFIX, inputModel.getPropertyKey(), chtValue);
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_EN_US_SUFFIX, inputModel.getPropertyKey(), enValue);
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_VI_VN_SUFFIX, inputModel.getPropertyKey(), viValue);
                        }
                    });
                }
            });
        }
    }

    /**
     * 用于判断新Key是否冲突
     */
    private Predicate<String> getCheckDataGridNewKeyRepeatExistsFun(Project project, Module module) {
        return (text) -> {
            List<Property> properties = MyPropertiesUtil.findModuleDataGridI18nProperties(project, module, text);
            // 键值对非空，即key已存在
            return !properties.isEmpty();
        };
    }

    /**
     * 用于判断输入中文是否已在资源文件有可复用Key
     */
    private static Function<String, List<Property>> getAlreadyExistsDataGridPropertyFun(@NotNull Project project, Module module) {
        return (value) -> MyPropertiesUtil.findModuleDataGridI18nPropertiesByValue(project, module, value);
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

        return MyPsiUtil.isXmlFileWithI18n(element);
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
        return "XML: 自动翻译并替换I18n key";
    }

    @Override
    public @NotNull String getText() {
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
