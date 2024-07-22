package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.lang.properties.IProperty;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class XmlI18nCompletionProvider extends BaseCompletionProvider {
    @Override
    protected void performCompletion(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result, @NotNull String prefix) {
        PsiFile originalFile = parameters.getOriginalFile();
        Module module = MyPsiUtil.getModuleByPsiElement(originalFile);
        List<IProperty> properties;
        if (MyPsiUtil.isLayoutFile(originalFile)){
            properties = MyPropertiesUtil.getModuleDataGridI18nPropertiesCN(originalFile.getProject(), module);
        } else {
            properties = MyPropertiesUtil.getModuleI18nPropertiesCN(originalFile.getProject(), module);
        }
        JavaI18nCompletionProvider.addPropertiesToCompletionResult(result, prefix, properties);
    }

    @Override
    protected boolean isApplicable(@NotNull PsiElement element, @NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        return MyPsiUtil.isXmlFileWithI18n(element);
    }
}
