package com.zhiyin.plugins.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;

/**
 * PSI 相关操作方法
 *
 * @author yan on 2024/3/7 23:33
 */
public class MyPsiUtil {

    /**
     * 获取所属模块
     * @param psiElement 元素
     * @return 模块
     */
    public static Module getModuleByPsiElement(PsiElement psiElement) {
        return ModuleUtilCore.findModuleForPsiElement(psiElement);
    }

    public static TextRange getTextRangeFromPsiLiteralExpression(PsiLiteralExpression psiLiteralExpression) {
        int startOffset = psiLiteralExpression.getTextOffset();
        return new TextRange(startOffset, startOffset + psiLiteralExpression.getTextLength());
    }
}
