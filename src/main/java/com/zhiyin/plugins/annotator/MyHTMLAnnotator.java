package com.zhiyin.plugins.annotator;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.xml.XmlText;
import com.intellij.psi.xml.XmlToken;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MyHTMLAnnotator implements Annotator {
    public static final Logger LOG = Logger.getInstance(MyHTMLAnnotator.class);
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        PsiElement parent = element.getParent();
        if (!(parent instanceof XmlText)) {
            return;
        }

        PsiElement[] children = parent.getChildren();
        if (children.length != 3) {
            return;
        }

        if (!(children[0] instanceof XmlToken)) {
            return;
        }

        if (!(children[1] instanceof PsiWhiteSpace)) {
            return;
        }

        if (!(children[2] instanceof XmlToken)) {
            return;
        }

        String text1 = children[0].getText().replaceAll(" ", "");
        if (!text1.startsWith("<@message")){
            return;
        }

        String text2 = children[2].getText().replaceAll(" ", "").replaceAll("'","\"");
        if (!text2.startsWith("key=\"com.zhiyin.")) {
            return;
        }

        int start = text2.indexOf(Constants.I18N_KEY_PREFIX);
        int end = text2.lastIndexOf("\"");

        if (start >= end) {
            return;
        }

        String key = text2.substring(start, end);
        TextRange textRange = new TextRange(element.getTextOffset(), element.getTextOffset() + element.getTextLength());
        Module module = MyPsiUtil.getModuleByPsiElement(element);
        if (module == null) {
            return;
        }
        Project project = element.getProject();
        List<Property> properties = MyPropertiesUtil.findModuleI18nProperties(project, module, key);
        properties.addAll(MyPropertiesUtil.findModuleWebI18nProperties(project, module, key));
        LOG.info("key:" + key + ", properties size:" + properties.size());
        if (!properties.isEmpty()) {
            String i18nValue = MyPropertiesUtil.getTop3PropertiesValueString(properties);
            holder.newAnnotation(HighlightSeverity.INFORMATION, i18nValue)
                  .tooltip(i18nValue)
                  .range(textRange)
                  .highlightType(ProblemHighlightType.INFORMATION)
                  .create();
        } else {
           holder.newAnnotation(HighlightSeverity.WARNING, Constants.INVALID_I18N_KEY)
                  .range(textRange)
                  .tooltip(Constants.INVALID_I18N_KEY)
                  .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                  .create();
        }


    }
}
