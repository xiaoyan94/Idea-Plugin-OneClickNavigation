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
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.zhiyin.plugins.notification.MyPluginMessages;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.ui.MyTranslateDialogWrapper;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class InsertJavaI18nFuncAction extends AnAction {

    private final static String toInsertTextFormat = "I18nUtil.getMessage(%s, \"%s\")";
    private final static String TARGET_VARIABLE_NAME = "userCode";
    private final static String TARGET_VARIABLE_NAME_LOWER_CASE = "usercode";
    private final static String VARIABLE_STATEMENT_TEXT = "String userCode = params.get(\"usercode\").toString();";

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

        PsiFile psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE);
        PsiElement element = psiFile.findElementAt(caretOffset);
        PsiMethod psiMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (psiMethod == null) {
            MyPluginMessages.showInfo("提示", "这个地方不能调用资源串方法啊喂。");
            return;
        }

        MyTranslateDialogWrapper myTranslateDialogWrapper = createMyTranslateDialogWrapper(project, module, selectedText);
        ApplicationManager.getApplication().invokeLater(() -> {
            if (myTranslateDialogWrapper.showAndGet()) {
                MyTranslateDialogWrapper.InputModel inputModel = myTranslateDialogWrapper.getInputModel();
                String key = inputModel.getPropertyKey();
                String toInsertText = String.format(toInsertTextFormat, "userCode", key);

                // 检查方法内部是否包含指定名称的变量
                boolean hasVariable = hasVariableInMethod(psiMethod, TARGET_VARIABLE_NAME);
                if (hasVariable) {
                    toInsertText = String.format(toInsertTextFormat, TARGET_VARIABLE_NAME, key);
                } else {
                    hasVariable = hasVariableInMethod(psiMethod, TARGET_VARIABLE_NAME_LOWER_CASE);
                    if (hasVariable) {
                        toInsertText = String.format(toInsertTextFormat, TARGET_VARIABLE_NAME_LOWER_CASE, key);
                    }
                }

                if (!hasVariable && false) {
                    // 变量不存在，执行写操作来插入代码
                    // WriteCommandAction 是一个推荐的写操作封装类
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        try {
                            // 获取方法体
                            PsiCodeBlock body = psiMethod.getBody();
                            if (body == null) {
                                return;
                            }

                            // 1. 创建新的语句元素
                            PsiElementFactory factory = PsiElementFactory.getInstance(project);
                            PsiStatement newStatement = factory.createStatementFromText(VARIABLE_STATEMENT_TEXT, null);

                            // 2. 找到插入点（方法体的开括号）
                            PsiElement firstStatement = body.getLBrace();
                            if (firstStatement == null) {
                                return;
                            }

                            // 3. 在开括号后插入新语句
                            // body.addAfter(newStatement, firstStatement);
                            // 添加一个换行，让代码格式更美观

                            // 创建一个包含换行符的空白元素
                            PsiParserFacade parserFacade = PsiParserFacade.SERVICE.getInstance(project);
                            PsiElement newLine = parserFacade.createWhiteSpaceFromText("\n");
                            // 在开括号后插入新语句和换行符
                            body.addAfter(newLine, firstStatement);
                            body.addAfter(newStatement, firstStatement);

                            MyPluginMessages.showInfo("操作提示", "缺少userCode变量，需在方法中添加userCode变量声明。请重新操作翻译资源串。", project);

                        } catch (Exception ex) {
                            MyPluginMessages.showError("错误", "插入代码时出错: " + ex.getMessage(), project);
                        }
                    });

                    return;
                }

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

                    MyPluginMessages.showInfo("操作成功", "资源串已替换成功，请检查", project);
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
            isAvailable = "JAVA".equalsIgnoreCase(virtualFile.getExtension());
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

    /**
     * 检查给定的方法内部是否包含指定名称的变量（包括参数和本地变量）。
     *
     * @param method 要检查的 PsiMethod 对象
     * @param variableName 目标变量名
     * @return 如果找到则返回 true，否则返回 false
     */
    private boolean hasVariableInMethod(PsiMethod method, String variableName) {
        // 1. 检查方法参数
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            if (Objects.equals(parameter.getName(), variableName)) {
                return true;
            }
        }

        // 2. 检查方法体内的本地变量
        PsiCodeBlock body = method.getBody();
        if (body == null) {
            return false;
        }

        // 使用 PsiRecursiveElementVisitor 遍历方法体中的所有元素
        // 这种方法可以处理嵌套的代码块，更可靠
        PsiLocalVariableVisitor visitor = new PsiLocalVariableVisitor(variableName);
        body.accept(visitor);

        return visitor.isVariableFound();
    }

    /**
     * 自定义的 PSI 访问者，用于查找特定名称的本地变量。
     */
    private static class PsiLocalVariableVisitor extends JavaRecursiveElementVisitor {
        private final String targetName;
        private boolean found = false;

        public PsiLocalVariableVisitor(String targetName) {
            this.targetName = targetName;
        }

        @Override
        public void visitLocalVariable(PsiLocalVariable variable) {
            super.visitLocalVariable(variable); // ⚠️ 这里在 JavaRecursiveElementVisitor 里是合法的
            if (Objects.equals(variable.getName(), targetName)) {
                found = true;
            }
        }

        public boolean isVariableFound() {
            return found;
        }
    }
}
