package com.zhiyin.plugins.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * 移除 @SysLogger 注解
 */
public class RemoveSysLoggerFix implements IntentionAction {

    @SafeFieldForPreview
    private final PsiElement element;

    public RemoveSysLoggerFix(PsiAnnotation annotation) {
        this.element =  annotation;
    }

    @NotNull
    @Override
    public String getText() {
        return "移除 @SysLogger 注解";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return element != null && element.isValid();
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
        if (element != null && element.isValid()) {
            element.delete();
        }
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
