package com.zhiyin.plugins.annotator;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.xml.XmlText;
import com.zhiyin.plugins.intention.TranslateAndReplaceIntentionAction;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class MyFreemarkerHTMLAnnotator implements Annotator {
    /**
     * Annotates the specified PSI element.
     * It is guaranteed to be executed in non-reentrant fashion.
     * I.e, there will be no call of this method for this instance before previous call get completed.
     * Multiple instances of the annotator might exist simultaneously, though.
     *
     * @param element to annotate.
     * @param holder  the container which receives annotations created by the plugin.
     */
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        Module module = MyPsiUtil.getModuleByPsiElement(element);
        if (module == null) {
            return;
        }

//        if (element instanceof PsiWhiteSpace || !(element instanceof XmlText)){
//            return;
//        }

        if (Arrays.stream(element.getChildren()).anyMatch(child -> child instanceof OuterLanguageElement && child.getText().contains("<@message"))){
            System.out.println(element.getText());
        } else {
            return;
        }

        if (element.getText().contains("<@message key")) {
            if (element.getTextLength() < 200){
                System.out.println("MyFreemarkerHTMLAnnotator.annotate TEXT: " + element.getText());
            }
            System.out.println("MyFreemarkerHTMLAnnotator.annotate: " + element.toString());
            System.out.println(Arrays.toString(element.getChildren()));
            System.out.println("-------");
        }

        String keyValue = MyPsiUtil.retrieveI18nKeyFromFreemarkerDirective(element);
        if (keyValue == null) {
            return;
        }

        String propertyValue = MyPropertiesUtil.findModuleWebI18nPropertyValue(element.getProject(), module, keyValue);
        if (propertyValue == null) {
            return;
        }

        holder.newAnnotation(HighlightSeverity.WARNING, Constants.INVALID_I18N_KEY)
                .range(element)
                .tooltip(Constants.INVALID_I18N_KEY)
                .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                .gutterIconRenderer(
                        //                          new CssColorGutterRenderer(element, JBColor.RED)
                        new MyJavaAnnotator.MyRenderer(element, MyIcons.pandaIcon16, Constants.INVALID_I18N_KEY)
                )
                // TODO quickfix
                .withFix(new TranslateAndReplaceIntentionAction())
                .create();
    }
}
