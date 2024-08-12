package com.zhiyin.plugins.provider.lineMarkers;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.impl.source.xml.XmlTokenImpl;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class LayoutUrlRelatedItemLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    public void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<?
            super RelatedItemLineMarkerInfo<?>> result) {
        boolean isLayoutFile = MyPsiUtil.isLayoutFile(element);
        if (!isLayoutFile) {
            return;
        }

        PsiElement targetElement;

        if (element instanceof XmlTokenImpl) {
            targetElement = element;
            String elementType = ((LeafPsiElement) element).getElementType().toString();
            if (elementType.equals("XML_ATTRIBUTE_VALUE_TOKEN")) {
                element = element.getParent();
            }
        } else {
            return;
        }

        if (element instanceof XmlAttributeValue && element.getParent() instanceof XmlAttribute) {
            element = element.getParent();
        } else {
            return;
        }

        if (!List.of("value", "url").contains(((XmlAttribute) element).getName())) {
            return;
        }


        String stringValue = ((XmlAttribute) element).getValue();
        if (stringValue == null || !stringValue.startsWith("../")) {
            return;
        }

        JSUrlRelatedItemLineMarkerProvider.registerControllerUrlLineMarker(element, result, stringValue, targetElement);
    }
}
