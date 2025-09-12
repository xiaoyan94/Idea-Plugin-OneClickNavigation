package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.*;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
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
import java.util.Set;

public class XmlLayoutComboboxCompletionProvider extends BaseCompletionProvider {
    @Override
    protected void performCompletion(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result, @NotNull String prefix) {
        Project project = parameters.getOriginalFile().getProject();
        ComboboxUrlService service = project.getService(ComboboxUrlService.class);
        PsiElement element = parameters.getPosition();
        XmlAttribute xmlAttribute = PsiTreeUtil.getParentOfType(element, XmlAttribute.class);
        if (xmlAttribute == null) {
            return;
        }
        XmlTag xmlTag = xmlAttribute.getParent();
        String xmlTagName = xmlTag.getName();
        if (xmlTag == null) {
            return;
        }
        String xmlAttributeName = xmlAttribute.getName();

        Set<String> cachedResults = null;
        if (Objects.equals(xmlTagName, "ComboxUrl")) {
            if ("value".equals(xmlAttributeName)) {
                cachedResults = service.getCachedResults("ComboxUrl", "value");
            }
        } else if (Objects.equals(xmlTagName, "Field")) {
            if ("easyuiClass".equals(xmlAttributeName)) {
                cachedResults = service.getCachedResults("Field", "easyuiClass");
            }
        } else if (Objects.equals(xmlTagName, "Column")) {
            if ("type".equals(xmlAttributeName)) {
                cachedResults = service.getCachedResults("Column", "type");
            }
        }

        if (cachedResults == null || cachedResults.isEmpty()) {
            return;
        }

        for (String value : cachedResults) {
            LookupElement lookupElement = LookupElementBuilder.create(value)
                                                              // 最好不要混用渲染方法
//                                                              .withExpensiveRenderer(new LookupElementRenderer<>() {
//
//                                                                  @Override
//                                                                  public void renderElement(LookupElement element, LookupElementPresentation presentation) {
//                                                                      presentation.setIcon(MyIcons.pandaIconSVG16_2);
//                                                                      presentation.setItemText(value);
//                                                                  }
//                                                              })
                                                              .withIcon(MyIcons.pandaIconSVG16_2)
                                                              .withItemTextItalic(true)
                                                              .withTypeText("Layout", null, true)
                                                              .withBoldness(true)
                                                              .withCaseSensitivity(false)
                                                              .withInsertHandler((insertionContext, item) -> {
                                                                  /*int startOffset = insertionContext.getStartOffset();
                                                                  int tailOffset = insertionContext.getTailOffset();
                                                                  insertionContext.getDocument().replaceString(
                                                                          startOffset,
                                                                          tailOffset,
                                                                          value
                                                                  );
                                                                  insertionContext.getEditor().getCaretModel().moveToOffset(
                                                                          startOffset + value.length()
                                                                  );*/

                                                                  // 避免 PrefixMatcher 的工作方式（IDEA 在计算 getPrefix() 时，会自动去掉一些非单词字符（比如 . / 等），用于匹配 LookupElement）
                                                                  // 导致的重复 ../../../ 问题
                                                                  XmlAttribute valueAttr = PsiTreeUtil.getParentOfType(
                                                                          insertionContext.getFile()
                                                                                          .findElementAt(
                                                                                                  insertionContext.getStartOffset()),
                                                                          XmlAttribute.class
                                                                  );

                                                                  if (valueAttr != null &&
                                                                      valueAttr.getValueElement() != null) {
                                                                      TextRange range = valueAttr.getValueElement()
                                                                                                 .getValueTextRange();
                                                                      insertionContext.getDocument().replaceString(
                                                                              range.getStartOffset(),
                                                                              range.getEndOffset(),
                                                                              value
                                                                      );
                                                                      insertionContext.getEditor()
                                                                                      .getCaretModel()
                                                                                      .moveToOffset(
                                                                                              range.getStartOffset() +
                                                                                              value.length()
                                                                                      )
                                                                      ;
                                                                  }
                                                              })
                                                              .withAutoCompletionPolicy(
                                                                      AutoCompletionPolicy.NEVER_AUTOCOMPLETE)
                    ;
            LookupElement prioritized = PrioritizedLookupElement.withPriority(lookupElement, 1000.0);
            result.addElement(prioritized);
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
        if (xmlTag == null || !(Objects.equals("ComboxUrl", xmlTag.getName()) || Objects.equals("Column", xmlTag.getName()) || Objects.equals("Field", xmlTag.getName()))) {
            return false;
        }
        String xmlAttributeName = xmlAttribute.getName();
        return Objects.equals("value", xmlAttributeName) || Objects.equals("easyuiClass", xmlAttributeName) || Objects.equals("type", xmlAttributeName);
    }

}
