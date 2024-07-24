package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.lang.properties.IProperty;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.util.ProcessingContext;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.zhiyin.plugins.completion.CommonI18nCompletionLogic.addPropertiesToCompletionResult;

public class JavaI18nCompletionProvider extends BaseCompletionProvider {
    @Override
    protected void performCompletion(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result, @NotNull String prefix) {
        PsiFile originalFile = parameters.getOriginalFile();
        Module module = MyPsiUtil.getModuleByPsiElement(originalFile);
        List<IProperty> properties = MyPropertiesUtil.getModuleI18nPropertiesCN(originalFile.getProject(), module);
        addPropertiesToCompletionResult(result, prefix, properties);
    }

    @Override
    protected boolean isApplicable(@NotNull PsiElement element, @NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression) MyPsiUtil.findPsiElementParentMatching(element, p -> p instanceof PsiMethodCallExpression);
        if (psiMethodCallExpression != null) {
            boolean i18nResourceMethod = MyPsiUtil.isI18nResourceMethod(psiMethodCallExpression);
            if (i18nResourceMethod) {
                PsiExpression[] expressions = psiMethodCallExpression.getArgumentList().getExpressions();
                if (expressions.length >= 2) {
                    return element.getParent() == expressions[1];
                }
            }
        }
        return false;
    }

}
