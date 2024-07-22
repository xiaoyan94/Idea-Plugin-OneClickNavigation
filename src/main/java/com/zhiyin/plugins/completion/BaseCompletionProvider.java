package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public abstract class BaseCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getPosition();
        String prefix = result.getPrefixMatcher().getPrefix();

        // 根据 isApplicable 方法判断是否应用补全
        if (isApplicable(element, parameters, context, result)) {
            performCompletion(parameters, context, result, prefix);
        }
    }

    /**
     * 子类实现具体的补全逻辑
     */
    protected abstract void performCompletion(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result, @NotNull String prefix);

    /**
     * 子类实现具体的适用性判断
     */
    protected abstract boolean isApplicable(@NotNull PsiElement element, @NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result);
}
