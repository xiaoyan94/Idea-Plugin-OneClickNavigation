package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
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
import java.util.Objects;
import java.util.function.Function;

import static com.zhiyin.plugins.completion.CommonI18nCompletionLogic.addPropertiesToCompletionResult;

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
        Function<IProperty, InsertHandler<LookupElement>> getLookupElementInsertHandler = getLookupElementInsertHandler(parameters);
        addPropertiesToCompletionResult(result, prefix, properties, getLookupElementInsertHandler);
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

    private @NotNull Function<IProperty, InsertHandler<LookupElement>> getLookupElementInsertHandler(@NotNull CompletionParameters parameters) {
        return property -> (insertionContext, item) -> {
            PsiElement element = parameters.getPosition();
            if (!MyPsiUtil.isXmlFileWithI18n(element)) {
                return;
            }
            XmlAttribute parent = PsiTreeUtil.getParentOfType(element, XmlAttribute.class);
            String key = property.getKey();
            if (parent == null || key == null) {
                return;
            }

            switch (parent.getName()) {
                case "value":
                case "label":
                case "i18n":
                    insertionContext.getDocument().replaceString(insertionContext.getStartOffset(), insertionContext.getTailOffset(), Objects.requireNonNull(property.getKey()));
                    break;
                default:
                    insertionContext.getDocument().replaceString(insertionContext.getStartOffset(), insertionContext.getTailOffset(), Objects.requireNonNull(property.getValue()));
                    break;
            }
            insertionContext.commitDocument();
        };
    }
}
