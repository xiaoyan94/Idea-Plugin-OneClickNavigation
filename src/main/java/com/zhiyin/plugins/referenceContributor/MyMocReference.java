package com.zhiyin.plugins.referenceContributor;

import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttributeValue;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Moc;
import com.zhiyin.plugins.oneClickNavigation.xml.utils.MyMapperUtils;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.service.MyProjectService;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyMocReference extends PsiReferenceBase<PsiLiteralExpression> {

    public MyMocReference(@NotNull PsiLiteralExpression element, boolean soft) {
        super(element, soft);
    }

    /**
     * @return range in the {@link PsiElement#getContainingFile containing file} of the {@link #getElement element}
     * which is considered a reference
     * @see #getRangeInElement
     */
    @Override
    public @NotNull TextRange getAbsoluteRange() {
        return super.getAbsoluteRange();
    }

    /**
     * Returns the element which is the target of the reference.
     *
     * @return the target element, or {@code null} if it was not possible to resolve the reference to a valid target.
     * @see PsiPolyVariantReference#multiResolve(boolean)
     */
    @Override
    public @Nullable PsiElement resolve() {
        PsiLiteralExpression literal = getElement();
        String value = (String) literal.getValue();
        if (value != null) {
            // Resolve the XML file based on the value.
            Project project = literal.getProject();
            List<XmlAttributeValue> mocNameElement = MyMapperUtils.getMocListByName(project, value);
            if (!mocNameElement.isEmpty()) {
                for (XmlAttributeValue xmlAttributeValue : mocNameElement) {
                    Module module1 = MyPsiUtil.getModuleByPsiElement(literal);
                    if (module1 != null && module1.equals(MyPsiUtil.getModuleByPsiElement(xmlAttributeValue))) {
                        return xmlAttributeValue;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the array of String, {@link PsiElement} and/or {@link LookupElement}
     * instances representing all identifiers that are visible at the location of the reference. The contents
     * of the returned array are used to build the lookup list for basic code completion. (The list
     * of visible identifiers may not be filtered by the completion prefix string - the
     * filtering is performed later by the IDE.)
     * <p>
     * This method is default since 2018.3.
     *
     * @return the array of available identifiers.
     */
    @Override
    public Object @NotNull [] getVariants() {
        PsiLiteralExpression element = getElement();
        Project project = element.getProject();
        MyProjectService myProjectService = project.getService(MyProjectService.class);
        List<LookupElement> variants = new ArrayList<>();
        Map<String, List<Moc>> mocFileMap = myProjectService.getMocFileMap();
        for (String mocName : mocFileMap.keySet()) {
            List<Moc> mocList = mocFileMap.get(mocName);
            if (mocList == null || mocList.isEmpty()) continue;
            if (mocList.stream().noneMatch(moc -> MyPsiUtil.isInSameModule(element, moc.getXmlElement()))) {
                continue;
            }
            variants.add(LookupElementBuilder.create(mocName)
                    .withIcon(MyIcons.pandaIconSVG16_2)
                    .withItemTextItalic(true)
                    .withTypeText("Moc", AllIcons.FileTypes.Xml, true)
                    .withBoldness(true)
                    .withCaseSensitivity(true)
//                    .withTailText("Moc", true)
                    .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
            );
        }
        return variants.toArray();
    }
}
