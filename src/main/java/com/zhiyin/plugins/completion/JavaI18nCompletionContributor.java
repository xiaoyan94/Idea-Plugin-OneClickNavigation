package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.patterns.PlatformPatterns;

public class JavaI18nCompletionContributor extends CompletionContributor {

    public JavaI18nCompletionContributor() {
        super();
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(JavaLanguage.INSTANCE), new JavaI18nCompletionProvider());
    }
}
