package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.lang.properties.IProperty;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ProcessingContext;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.zhiyin.plugins.completion.CommonI18nCompletionLogic.*;

public class XmlI18nCompletionProvider extends BaseCompletionProvider {
    @Override
    protected void performCompletion(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result, @NotNull String prefix) {
        PsiFile originalFile = parameters.getOriginalFile();
        Module module = MyPsiUtil.getModuleByPsiElement(originalFile);
        List<IProperty> properties;
        if (MyPsiUtil.isLayoutFile(originalFile)) {
            properties = MyPropertiesUtil.getModuleDataGridI18nPropertiesCN(originalFile.getProject(), module);
        } else {
            properties = MyPropertiesUtil.getModuleI18nPropertiesCN(originalFile.getProject(), module);
        }
        addPropertiesToCompletionResultForXml(result, prefix, properties);
    }

    @Override
    protected boolean isApplicable(@NotNull PsiElement element, @NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        boolean isXmlFileWithI18n = MyPsiUtil.isXmlFileWithI18n(element);
        if (!isXmlFileWithI18n) {
            return false;
        }
        XmlAttribute xmlAttribute = PsiTreeUtil.getParentOfType(element, XmlAttribute.class);
        if (xmlAttribute == null) {
            return false;
        }
        String xmlAttributeName = xmlAttribute.getName();
        return List.of("value", "chs", "eng", "viet", "label", "i18n").contains(xmlAttributeName);
    }

}
