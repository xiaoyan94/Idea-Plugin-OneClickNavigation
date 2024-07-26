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
import java.util.Objects;

public class HtmlI18nCompletionProvider extends BaseCompletionProvider {
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        super.addCompletions(parameters, context, result);
    }

    @Override
    protected void performCompletion(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result, @NotNull String prefix) {
        PsiFile originalFile = parameters.getOriginalFile();
        Module module = MyPsiUtil.getModuleByPsiElement(originalFile);
        List<IProperty> properties = MyPropertiesUtil.getModuleI18nPropertiesCN(originalFile.getProject(), module);
        properties.stream().filter(Objects::nonNull).filter(property -> property.getKey() != null && property.getValue() != null && property.getValue().startsWith(prefix)).forEach(property -> {
            String key = property.getKey();
            String value = property.getValue();
            String unescapedValue = property.getUnescapedValue();
            if (value != null && !value.isEmpty()) {
                CommonI18nLookupElement lookupElement;
                if (unescapedValue != null) {
                    lookupElement = new CommonI18nLookupElement(Objects.requireNonNull(key), unescapedValue, value);
                    result.addElement(lookupElement);
                }
            }
        });
    }

    @Override
    protected boolean isApplicable(@NotNull PsiElement element, @NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        return true;
    }
}
