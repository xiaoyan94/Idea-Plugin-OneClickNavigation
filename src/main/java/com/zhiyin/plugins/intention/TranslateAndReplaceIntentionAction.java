package com.zhiyin.plugins.intention;

import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.encoding.EncodingManager;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.ui.MyTranslateDialogWrapper;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TranslateAndReplaceIntentionAction extends PsiElementBaseIntentionAction implements IntentionAction, HighPriorityAction {

    private final Logger logger = Logger.getInstance(TranslateAndReplaceIntentionAction.class);

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
        PsiLiteralExpression literalExpression = getPsiLiteralExpressionForI18n(element);
        return literalExpression != null;

        /*String i18Key = literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
        Module module = MyPsiUtil.getModuleByPsiElement(element);
        Property property = MyPropertiesUtil.findModuleI18nProperty(project, module, i18Key);
        return property == null;*/
    }
    
    /**
     * Invokes intention action for the element under caret.
     *
     * @param project the project in which the file is opened.
     * @param editor  the editor for the file.
     * @param element the element under cursor.
     */
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        logger.info("invoke on = " + element);
        Module module = MyPsiUtil.getModuleByPsiElement(element);
        PsiLiteralExpression literalExpression = getPsiLiteralExpressionForI18n(element);
        String i18Key;
        if (literalExpression != null) {
            i18Key = literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : "";
        } else {
            i18Key = "";
        }
        MyTranslateDialogWrapper myTranslateDialogWrapper = new MyTranslateDialogWrapper(project, module);
        MyTranslateDialogWrapper.InputModel inputModel = myTranslateDialogWrapper.getInputModel();
        myTranslateDialogWrapper.setGetI18nPropertiesFun(value -> MyPropertiesUtil.findModuleI18nPropertiesByValue(project, module, value));
        myTranslateDialogWrapper.setCheckI18nKeyExistsFun(key -> !MyPropertiesUtil.findModuleI18nProperties(project, module, key).isEmpty());
        // 使用invokeLater将UI更新调度到EDT线程中
        ApplicationManager.getApplication().invokeLater(() -> {
            myTranslateDialogWrapper.setSourceCHSText(i18Key);

            if (myTranslateDialogWrapper.showAndGet()) {
                System.out.println(inputModel);

                // 确保写操作在正确的上下文中执行
                ApplicationManager.getApplication().invokeLater(() -> {
                    // 执行写操作
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        // 获取用于创建新PsiElement的工厂，并获取用于格式化新语句的代码样式管理器
                        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
//                        CodeStyleManager codeStylist = CodeStyleManager.getInstance(project);

                        // 写入Properties文件
                        if (!inputModel.getPropertyKeyExists()) {
                            boolean native2AsciiForPropertiesFiles = EncodingManager.getInstance().isNative2AsciiForPropertiesFiles();
                            String chsValue = native2AsciiForPropertiesFiles ? inputModel.getChinese() : inputModel.getChineseUnicode();
                            String chtValue = native2AsciiForPropertiesFiles ? inputModel.getChineseTW() : inputModel.getChineseTWUnicode();
//                            String enValue = native2AsciiForPropertiesFiles ? inputModel.getEnglish() : inputModel.getEnglishUnicode();
                            String enValue = inputModel.getEnglish();
                            String viValue = inputModel.getVietnamese();

                            logger.info("native2AsciiForPropertiesFiles = " + native2AsciiForPropertiesFiles + " chsValue = " + chsValue + " chtValue = " + chtValue + " enValue = " + enValue + " viValue = " + viValue);
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_ZH_CN_SUFFIX, inputModel.getPropertyKey(), chsValue);
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_ZH_TW_SUFFIX, inputModel.getPropertyKey(), chtValue);
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_EN_US_SUFFIX, inputModel.getPropertyKey(), enValue);
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_VI_VN_SUFFIX, inputModel.getPropertyKey(), viValue);
                        }


                        // 替换literalExpression的文本内容
                        if (literalExpression != null) {
                            PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
                            literalExpression.replace(factory.createExpressionFromText("\"" + inputModel.getPropertyKey() + "\"", literalExpression));
                        }
                    });
                }, ModalityState.defaultModalityState());
            }
        }, ModalityState.defaultModalityState());
    }


    private @Nullable PsiLiteralExpression getPsiLiteralExpressionForI18n(@NotNull PsiElement element) {
        Module module = MyPsiUtil.getModuleByPsiElement(element);
        if (module == null) {
            return null;
        }

        PsiLiteralExpression literalExpression;
        // 确保 PSI 元素是一个表达式
        if (element instanceof PsiLiteralExpression) {
            literalExpression = (PsiLiteralExpression) element;
        } else if(element.getParent() instanceof PsiLiteralExpression){
            literalExpression = (PsiLiteralExpression) element.getParent();
        } else{
            return null;
        }

        PsiElement parent = literalExpression.getParent();
        if (!(parent instanceof PsiExpressionList)) {
            return null;
        }

        parent = parent.getParent();
        if (!(parent instanceof PsiMethodCallExpression)) {
            return null;
        }
        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) parent;
        boolean isI18nResourceMethod = MyPsiUtil.isI18nResourceMethod(methodCallExpression);
        if (!isI18nResourceMethod) {
            return null;
        }
        return literalExpression;
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
        return "Java: 自动翻译并替换I18n key";
    }

    /**
     * If this action is applicable, returns the text to be shown in the list of intention actions available.
     */
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
