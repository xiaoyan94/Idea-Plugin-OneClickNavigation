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

public class InsertFreeMarkerI18nDirectiveAction extends AnAction {

    private final static String toInsertTextFormat = "<@message key='%s'/>";

    @Override
    public boolean isDumbAware() {
        return false;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // TODO insert a text from String after current caret
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        Caret currentCaret = editor.getCaretModel().getCurrentCaret();
        int caretOffset = currentCaret.getOffset();
        int selectionStart = currentCaret.getSelectionStart();
        int selectionEnd = currentCaret.getSelectionEnd();
        String selectedText = currentCaret.getSelectedText();
        Document document = currentCaret.getEditor().getDocument();
        Project project = e.getRequiredData(PlatformDataKeys.PROJECT);
        Module module = e.getRequiredData(PlatformDataKeys.MODULE);

        MyTranslateDialogWrapper myTranslateDialogWrapper = createMyTranslateDialogWrapper(project, module, selectedText);
        ApplicationManager.getApplication().invokeLater(() -> {
            if (myTranslateDialogWrapper.showAndGet()) {
                MyTranslateDialogWrapper.InputModel inputModel = myTranslateDialogWrapper.getInputModel();
                String key = inputModel.getPropertyKey();
                String toInsertText = String.format(toInsertTextFormat, key);

                // 写入Document
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    // 检查是否有选中文本
                    if (selectionStart != selectionEnd) {
                        // 有选中文本，替换选中的文本
                        document.replaceString(selectionStart, selectionEnd, toInsertText);
                        // 设置光标到新插入文本的末尾
                        currentCaret.moveToOffset(selectionStart + toInsertText.length());
                    } else {
                        // 没有选中文本，在光标位置插入文本
                        document.insertString(caretOffset, toInsertText);
                        // 移动光标到新插入文本的末尾
                        currentCaret.moveToOffset(caretOffset + toInsertText.length());
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
                    if (!isNative2AsciiForPropertiesFiles) {
                        chsValue = inputModel.getChineseUnicode();
                        chtValue = inputModel.getChineseTWUnicode();
//                        enValue = inputModel.getEnglishUnicode();
                    }

                    String finalChsValue = chsValue;
                    String finalChtValue = chtValue;
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_ZH_CN_SUFFIX, key, finalChsValue);
                        MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_ZH_TW_SUFFIX, key, finalChtValue);
                        MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_EN_US_SUFFIX, key, enValue);
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
        }
        e.getPresentation().setEnabled(isAvailable);
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
