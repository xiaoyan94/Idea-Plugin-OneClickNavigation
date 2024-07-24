package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.lookup.LookupElementRenderer;
import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.zhiyin.plugins.listeners.MyFileEditorManagerListener;
import com.zhiyin.plugins.resources.MyIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommonI18nLookupElement extends LookupElement {
    private final String key;
    private final String chs;
    private final String eng;

    public CommonI18nLookupElement(@NotNull String key, @NotNull String chs, @NotNull String eng) {
        super();
        this.key = key;
        this.chs = chs;
        this.eng = eng;
    }

    public CommonI18nLookupElement(@NotNull String key, @NotNull String chs) {
        super();
        this.key = key;
        this.chs = chs;
        this.eng = chs;
    }

    /**
     * 这里不能填用 key，否则无法在自动补全列表中用中文搜索
     */
    @Override
    public @NotNull String getLookupString() {
        return chs;
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context) {
        super.handleInsert(context);

        PsiElement element = context.getFile().findElementAt(context.getStartOffset());
        if (element == null) {
            return;
        }

        Language language = element.getLanguage();
        if (language instanceof XMLLanguage){
            // Find the XmlTag parent
            XmlTag xmlTag = PsiTreeUtil.getParentOfType(element, XmlTag.class);
            if (xmlTag == null) {
                return;
            }

            // Modify other attributes of the XmlTag based on the chosen LookupElement
            modifyXmlAttribute(xmlTag);
        } else if (language instanceof JavaLanguage){
            context.getDocument().replaceString(context.getStartOffset(), context.getTailOffset(), key);
        } else {
            context.getDocument().replaceString(context.getStartOffset(), context.getTailOffset(), chs);
        }

        // Commit the document changes
        commitDocument(context);

        ApplicationManager.getApplication().invokeLater(() -> {
            // Refresh the editor to reflect the changes
            Editor editor = context.getEditor();
            editor.getContentComponent().repaint();

            CaretModel caretModel = editor.getCaretModel();
            LogicalPosition position = caretModel.getLogicalPosition();
            int lineEndOffset = editor.getDocument().getLineEndOffset(position.line);
            caretModel.moveToOffset(lineEndOffset);
            MyFileEditorManagerListener.collapseFoldRegion(editor);
        });
    }

    private void modifyXmlAttribute(@NotNull XmlTag xmlTag) {
        // Modify other attributes based on the chosen LookupElement
        switch (xmlTag.getName()) {
            case "Title":
                xmlTag.setAttribute("chs", chs);
                xmlTag.setAttribute("eng", eng);
                xmlTag.setAttribute("value", key);
                break;
            case "column":
                xmlTag.setAttribute("i18n", key);
                xmlTag.setAttribute("description", chs);
                break;
            case "Field":
                xmlTag.setAttribute("label", key);
                break;
            default:
                break;
        }

    }

    private void commitDocument(@NotNull InsertionContext context) {
        // Commit the document changes
        context.commitDocument();
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        presentation.setIcon(MyIcons.pandaIconSVG16_2);
        presentation.setItemText(key);
    }

    @Override
    public @Nullable LookupElementRenderer<? extends LookupElement> getExpensiveRenderer() {
        return new LookupElementRenderer<>() {
            @Override
            public void renderElement(LookupElement element, LookupElementPresentation presentation) {
                presentation.setIcon(MyIcons.pandaIconSVG16_2);
                presentation.setItemText(chs);
                presentation.setTailText(key);
            }
        };
    }

    @Override
    public boolean isWorthShowingInAutoPopup() {
        return true;
    }

    /**
     * @return the policy determining the auto-insertion behavior when this is the only matching item produced by completion contributors
     */
    @Override
    public AutoCompletionPolicy getAutoCompletionPolicy() {
        return AutoCompletionPolicy.NEVER_AUTOCOMPLETE;
    }


    @Override
    public boolean isCaseSensitive() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CommonI18nLookupElement) {
            CommonI18nLookupElement other = (CommonI18nLookupElement) obj;
            return chs.equals(other.chs);
        }
        return false;
    }
}
