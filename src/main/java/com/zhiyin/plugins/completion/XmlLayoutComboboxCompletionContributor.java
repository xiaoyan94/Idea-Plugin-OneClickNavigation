package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.xml.XmlAttributeValue;

public class XmlLayoutComboboxCompletionContributor extends CompletionContributor {

    public XmlLayoutComboboxCompletionContributor() {
        super();
        // extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(XMLLanguage.INSTANCE), new XmlLayoutComboboxCompletionProvider());
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().inside(XmlAttributeValue.class), new XmlLayoutComboboxCompletionProvider());
    }
}
