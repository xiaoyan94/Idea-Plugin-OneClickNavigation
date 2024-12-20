package com.zhiyin.plugins.annotator;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.formatter.blocks.JSBlockEx;
import com.intellij.lang.javascript.formatter.blocks.JSParameterBlock;
import com.intellij.lang.javascript.formatter.blocks.JSParameterListBlock;
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.webcore.formatter.chainedMethods.CallChainBlock;
import com.zhiyin.plugins.intention.JavaScriptI18nTranslateIntentionAction;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MyJavaScriptBlockAnnotator implements Annotator {
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
        PsiFile psiFile = element.getContainingFile();
        if (!(psiFile instanceof XmlFile)){
            return;
        }
        Project project = element.getProject();
        Module module = MyPsiUtil.getModuleByPsiElement(element);
        if(module == null){
            return;
        }

        if (element.getParent() instanceof JSLiteralExpression) {
            if(element.getParent().getParent() instanceof JSArgumentList
                && element.getParent().getParent().getPrevSibling() instanceof JSReferenceExpression
            ){
                JSReferenceExpression jsReferenceExpression = (JSReferenceExpression) element.getParent().getParent().getPrevSibling();
//                System.out.println(jsReferenceExpression.getReferenceName());
//                System.out.println(jsReferenceExpression.getName());
//                System.out.println(jsReferenceExpression.getPresentation());
//                System.out.println(jsReferenceExpression.getCanonicalText());
//                System.out.println(jsReferenceExpression.getText());
                if("zhiyin.i18n.translate".equals(jsReferenceExpression.getCanonicalText())){
                    String i18nKey =  ((JSLiteralExpression) element.getParent()).getStringValue();
                    List<Property> properties = MyPropertiesUtil.findModuleI18nProperties(project, module, i18nKey);
                    properties.addAll(MyPropertiesUtil.findModuleWebI18nProperties(project, module, i18nKey));
                    if(!properties.isEmpty()){
                        String values = MyPropertiesUtil.getTop3PropertiesValueString(properties);
                        holder.newAnnotation(HighlightSeverity.INFORMATION, values)
                                .highlightType(ProblemHighlightType.INFORMATION)
                                .tooltip(values).needsUpdateOnTyping(true).create();
                    } else{
                        holder.newAnnotation(HighlightSeverity.WEAK_WARNING, Constants.INVALID_I18N_KEY)
                                .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
//                                .gutterIconRenderer(new MyJavaAnnotator.MyRenderer(element, MyIcons.pandaIcon16, Constants.INVALID_I18N_KEY))
                                .tooltip(Constants.INVALID_I18N_KEY).needsUpdateOnTyping(true)
                                .withFix(new JavaScriptI18nTranslateIntentionAction())
                                .create();
                    }
                }
            }
        }

    }
}
