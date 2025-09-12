package com.zhiyin.plugins.referenceContributor;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.highlighting.HighlightedReference;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.xml.IXmlLeafElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlToken;
import com.intellij.util.PlatformIcons;
import com.intellij.util.xml.DomJavaUtil;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericValue;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Mapper;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Statement;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.service.MyProjectService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 扩展额外引用
 *
 */
final class MyJavaMethodReference extends PsiReferenceBase<PsiLiteralExpression> implements PsiPolyVariantReference, HighlightedReference {

    private final PsiClass psiClass;

    private final String methodName;

    MyJavaMethodReference(@NotNull PsiLiteralExpression element, PsiClass psiClass, String methodName) {
        super(element);
        this.psiClass = psiClass;
        this.methodName = methodName;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        final PsiMethod[] methodsByName = psiClass.findMethodsByName(methodName, true);
        List<ResolveResult> results = new ArrayList<>();
        for (PsiMethod psiMethod : methodsByName) {
            results.add(new PsiElementResolveResult(psiMethod));
        }
        return results.toArray(new ResolveResult[0]);
    }


    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    /**
     * 代码提示:根据引用提供自动补全(ctrl + 空格)
     */
    @Override
    public Object @NotNull [] getVariants() {
        PsiMethod[] methods = psiClass.getMethods();
        List<LookupElement> variants = new ArrayList<>();
        for (final PsiMethod psiMethod : methods) {
            if (psiMethod != null) {
                LookupElement lookupElement = LookupElementBuilder.create(psiMethod)
                                                                  .withIcon(MyIcons.pandaIconSVG16_2)
                                                                  .withItemTextItalic(true)
                                                                  .withTypeText(psiClass.getName(), null, true)
                                                                  .withBoldness(true)
                                                                  .withCaseSensitivity(false)
                                                                  .withTailText(psiClass.getQualifiedName(), true)
                                                                  .withAutoCompletionPolicy(
                                                                          AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
                        ;

                // 包装成最高优先级
                LookupElement prioritized = PrioritizedLookupElement.withPriority(lookupElement, 1000.0);
                variants.add(prioritized);
            }
        }
        return variants.toArray();
    }

}