package com.zhiyin.plugins.annotator;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.*;
import com.zhiyin.plugins.intention.XMLTranslateIntentionAction;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Layout XML Annotator
 */
public class MyLayoutXMLAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        boolean isLayoutFile = MyPsiUtil.isLayoutFile(psiElement);
        if (!isLayoutFile) {
            return;
        }
        Module module = MyPsiUtil.getModuleByPsiElement(psiElement);
        if (module == null) {
            return;
        }

        if (psiElement instanceof XmlToken && ((XmlToken) psiElement).getTokenType() == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN && psiElement.getParent() instanceof XmlAttributeValue && psiElement.getParent().getParent() instanceof XmlAttribute) {
            XmlAttribute xmlAttribute = (XmlAttribute) psiElement.getParent().getParent();
            String xmlAttributeName = xmlAttribute.getName();
            if (xmlAttributeName.equals("value") || xmlAttributeName.equals("label") || xmlAttributeName.equals("i18n")) {
                XmlTag parentXmlTag = MyPsiUtil.getParentXmlTag(xmlAttribute);
                if (parentXmlTag != null && (this.getParentXmlTagNameByAttributeName(xmlAttributeName).equals(parentXmlTag.getName()))) {
                    XmlToken xmlToken = (XmlToken) psiElement;
                    String i18nKey = xmlToken.getText();
                    List<Property> properties = MyPropertiesUtil.findModuleDataGridI18nProperties(psiElement.getProject(), module, i18nKey);
                    if (properties.isEmpty()) {
                        annotationHolder.newAnnotation(HighlightSeverity.WEAK_WARNING, Constants.INVALID_I18N_KEY)
                                .range(xmlToken.getTextRange())
                                .tooltip(Constants.INVALID_I18N_KEY)
                                .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                                .gutterIconRenderer(new MyJavaAnnotator.MyRenderer(psiElement, MyIcons.pandaIcon16, Constants.INVALID_I18N_KEY))
                                .withFix(new XMLTranslateIntentionAction())
                                .needsUpdateOnTyping(true)
                                .create();
                    } else {
                        String value = MyPropertiesUtil.getTop3PropertiesValueString(properties);
                        annotationHolder.newAnnotation(HighlightSeverity.INFORMATION, value).range(xmlToken.getTextRange())
//                                .gutterIconRenderer(
//                                        new GutterActionRenderer(new AnAction() {
//                                            @Override
//                                            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
//                                                PsiNavigateUtil.navigate(properties.get(0));
//                                            }
//
//                                        }) {
//                                            @Override
//                                            public @NotNull Icon getIcon() {
//                                                return MyIcons.pandaIconSVG16_2;
//                                            }
//
//                                            @Override
//                                            public String getTooltipText() {
//                                                return value;
//                                            }
//                                        }
//                                )
                                .tooltip(value)
                                .needsUpdateOnTyping(true)
                                .create();
                    }
                }
            }
        }
    }

    private String getParentXmlTagNameByAttributeName(String attributeName){
        switch (attributeName){
            case "value":
                return "Title";
            case "label":
                return "Field";
            case "i18n":
                return "column";
            default:
                return "";
        }
    }
}
