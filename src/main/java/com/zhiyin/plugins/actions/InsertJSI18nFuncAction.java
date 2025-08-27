package com.zhiyin.plugins.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.ui.MyTranslateDialogWrapper;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import org.jetbrains.annotations.NotNull;

public class InsertJSI18nFuncAction extends AnAction {

    private final static String toInsertTextFormat = "zhiyin.i18n.translate('%s')";

    @Override
    public boolean isDumbAware() {
        return false;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        Caret currentCaret = editor.getCaretModel().getCurrentCaret();
        int caretOffset = currentCaret.getOffset();
        int selectionStart = currentCaret.getSelectionStart();
        int selectionEnd = currentCaret.getSelectionEnd();

        String selectedText = currentCaret.getSelectedText();
        if (selectedText!= null) {
            selectedText = selectedText.trim();
            if (selectedText.charAt(0) == '\'' || selectedText.charAt(0) == '\"') {
                selectedText = selectedText.substring(1, selectedText.length());
            }
            if (selectedText.charAt(selectedText.length() - 1) == '\'' || selectedText.charAt(selectedText.length() - 1) == '\"') {
                selectedText = selectedText.substring(0, selectedText.length() - 1);
            }
        }
        Document document = currentCaret.getEditor().getDocument();
        Project project = e.getRequiredData(PlatformDataKeys.PROJECT);
        Module module = e.getRequiredData(PlatformDataKeys.MODULE);

        CharSequence documentText = document.getCharsSequence();
        // 如果selectionStart前是`'`, 且selectionEnd后是`'`, 则扩展选中范围至包含引号
        // 检查 selectionStart 前面是否是单引号
        if (selectionStart > 0 && (documentText.charAt(selectionStart - 1) == '\'' || documentText.charAt(selectionStart - 1) == '\"')) {
            selectionStart--; // 扩展选择范围，包含前面的单引号
        }

        // 检查 selectionEnd 后面是否是单引号
        if (selectionEnd < documentText.length() && (documentText.charAt(selectionEnd) == '\'' || documentText.charAt(selectionEnd) == '\"')) {
            selectionEnd++; // 扩展选择范围，包含后面的单引号
        }

        int startOffset = selectionStart;
        int endOffset = selectionEnd;

        MyTranslateDialogWrapper myTranslateDialogWrapper = createMyTranslateDialogWrapper(project, module, selectedText);
        ApplicationManager.getApplication().invokeLater(() -> {
            if (myTranslateDialogWrapper.showAndGet()) {
                MyTranslateDialogWrapper.InputModel inputModel = myTranslateDialogWrapper.getInputModel();
                String key = inputModel.getPropertyKey();
                String toInsertText = String.format(toInsertTextFormat, key);

                // 支持 jsp
                /*VirtualFile virtualFile = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE);
                boolean isJsp = "jsp".equalsIgnoreCase(virtualFile.getExtension());
                if (isJsp) {
                    toInsertText = toInsertText.replace("@message", "mes:message");
                }*/

                // 写入Document
                String finalToInsertText = toInsertText;
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    // 检查是否有选中文本
                    if (startOffset != endOffset) {
                        // 有选中文本，替换选中的文本
                        document.replaceString(startOffset, endOffset, finalToInsertText);
                        // 设置光标到新插入文本的末尾
                        currentCaret.moveToOffset(startOffset + finalToInsertText.length());
                    } else {
                        // 没有选中文本，在光标位置插入文本
                        document.insertString(caretOffset, finalToInsertText);
                        // 移动光标到新插入文本的末尾
                        currentCaret.moveToOffset(caretOffset + finalToInsertText.length());
                    }

                });
                // 确保光标可见
                EditorModificationUtil.scrollToCaret(editor);

                // 没有可复用的key，写入.properties文件
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

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean isAvailable = false;
        if (e.getData(CommonDataKeys.EDITOR) != null && e.getData(CommonDataKeys.VIRTUAL_FILE) != null
                && e.getData(PlatformDataKeys.MODULE) != null && e.getData(PlatformDataKeys.PROJECT) != null) {
            VirtualFile virtualFile = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE);
            isAvailable = "html".equalsIgnoreCase(virtualFile.getExtension());
            isAvailable = isAvailable || "ftl".equalsIgnoreCase(virtualFile.getExtension());
            isAvailable = isAvailable || "jsp".equalsIgnoreCase(virtualFile.getExtension());
        }
        e.getPresentation().setEnabledAndVisible(isAvailable);
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
}
