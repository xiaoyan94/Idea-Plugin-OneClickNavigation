package com.zhiyin.plugins.annotator;

import com.intellij.codeInspection.ProblemHighlightType;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.zhiyin.plugins.intention.TranslateAndReplaceIntentionAction;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

/**
 * 提供语法高亮和注释提示
 *
 * 
 */
public final class MyJavaAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {

        Module module = MyPsiUtil.getModuleByPsiElement(element);
        if (module == null) {
            return;
        }

        // Ensure the PSI Element is an expression
        if (!(element instanceof PsiLiteralExpression)) {
            return;
        }

        PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
        PsiElement parent = literalExpression.getParent();
        if (!(parent instanceof PsiExpressionList)) {
            return;
        }

        parent = parent.getParent();
        if (!(parent instanceof PsiMethodCallExpression)) {
            return;
        }
        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) parent;
        if (!methodCallExpression.getMethodExpression().textMatches(Constants.I18N_METHOD_EXPRESSION)) {
            return;
        }

        // Ensure the PSI element contains a string that starts with the prefix and separator
        String i18Key = literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
//        if (i18Key == null || !i18Key.startsWith(Constants.I18N_KEY_PREFIX)) {
//            return;
//        }

        // Define the text ranges (start is inclusive, end is exclusive)
        //        TextRange keyRange = literalExpression.getTextRange();
        TextRange keyRange = MyPsiUtil.getTextRangeFromPsiLiteralExpression(literalExpression);

        // highlight key
        //        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        //              .range(keyRange).textAttributes(DefaultLanguageHighlighterColors.KEYWORD).create();

        // Get the list of properties for given key

        Project project = element.getProject();
        List<Property> properties = MyPropertiesUtil.findModuleI18nProperties(project, module, i18Key);
        if (properties.isEmpty()) {

            holder.newAnnotation(HighlightSeverity.WARNING, Constants.INVALID_I18N_KEY)
                  .range(keyRange)
                  .tooltip(Constants.INVALID_I18N_KEY)
                  .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                  .gutterIconRenderer(
                          //                          new CssColorGutterRenderer(element, JBColor.RED)
                          new MyRenderer(element, MyIcons.pandaIcon16, Constants.INVALID_I18N_KEY)
                  )
                  // TODO quickfix
                    .withFix(new TranslateAndReplaceIntentionAction())
                    .create();
        } else {
            /*
 Found at least one property, force the text attributes to Simple syntax value character
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                  .range(keyRange).textAttributes(TextAttributesKey.createTextAttributesKey("SIMPLE_VALUE",
                  DefaultLanguageHighlighterColors.STRING)).create();
*/
            String i18nValue = MyPropertiesUtil.getTop3PropertiesValueString(properties);
            holder.newAnnotation(HighlightSeverity.INFORMATION, i18nValue)
                  .tooltip(i18nValue)
                  .range(keyRange)
                  .highlightType(ProblemHighlightType.INFORMATION)
//                  .gutterIconRenderer(
//                          //                          new CssColorGutterRenderer(element, JBColor.GREEN)
//                          new MyRenderer(element, MyIcons.pandaIconSVG16_2, i18nValue)
//                  )
                  .create();

        }
    }

    public static final class MyRenderer extends GutterIconRenderer {
        private final PsiElement psiElement;

        private final Icon icon;

        private final String tips;

        public MyRenderer(PsiElement element, String tips) {
            this.icon = MyIcons.pandaIconSVG16_2;
            this.psiElement = element;
            this.tips = tips;
        }

        public MyRenderer(PsiElement element, Icon icon, String tips) {
            this.icon = icon;
            this.psiElement = element;
            this.tips = tips;
        }

        @NotNull
        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public String getTooltipText() {
            return tips;
        }

        @Override
        public boolean equals(Object obj) {
            /*if (obj instanceof MyRenderer) {
                return psiElement.equals(((MyRenderer) obj).psiElement) && tips.equals(((MyRenderer) obj).tips);
            }*/

            return false;
        }

        @Override
        public int hashCode() {
            return psiElement.hashCode() + tips.hashCode();
        }

    }
}
