package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.service.ComboboxUrlService;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class XmlLayoutComboboxCompletionProvider extends BaseCompletionProvider {
    @Override
    protected void performCompletion(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result, @NotNull String prefix) {
        Project project = parameters.getOriginalFile().getProject();
        ComboboxUrlService service = new ComboboxUrlService(project);
        List<String> cachedResults = service.getCachedResults();

        for (String value : cachedResults) {
            LookupElement lookupElement = LookupElementBuilder.create(value)
                    .withPresentableText(value)
                    .withIcon(MyIcons.pandaIconSVG16_2)
//                    .withCaseSensitivity(false)
//                    .withTailText()
                    .withLookupString(value);
            result.addElement(lookupElement);
        }
    }

    @Override
    protected boolean isApplicable(@NotNull PsiElement element, @NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        boolean isMyFile = MyPsiUtil.isLayoutFile(element);
        if (!isMyFile) {
            return false;
        }
        XmlAttribute xmlAttribute = PsiTreeUtil.getParentOfType(element, XmlAttribute.class);
        if (xmlAttribute == null) {
            return false;
        }
        XmlTag xmlTag = xmlAttribute.getParent();
        if (xmlTag == null || !Objects.equals("ComboxUrl", xmlTag.getName())) {
            return false;
        }
        String xmlAttributeName = xmlAttribute.getName();
        return Objects.equals("value", xmlAttributeName);
    }

}
