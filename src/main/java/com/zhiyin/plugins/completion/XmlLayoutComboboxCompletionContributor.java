package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.patterns.PlatformPatterns;

public class XmlLayoutComboboxCompletionContributor extends CompletionContributor {

    public XmlLayoutComboboxCompletionContributor() {
        super();
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(XMLLanguage.INSTANCE), new XmlLayoutComboboxCompletionProvider());
    }
}
