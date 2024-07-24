package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;

public class HtmlI18nCompletionContributor extends CompletionContributor {

    public HtmlI18nCompletionContributor() {
        super();
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new HtmlI18nCompletionProvider());
    }
}
