package com.zhiyin.plugins.foldingBuilder;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.XmlRecursiveElementWalkingVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyXMLFoldingBuilder extends FoldingBuilderEx {

    public static final FoldingGroup group = FoldingGroup.newGroup(Constants.FOLDING_GROUP);

    /**
     * Builds the folding regions for the specified node in the AST tree and its children.
     *
     * @param root     the element for which folding is requested.
     * @param document the document for which folding is built. Can be used to retrieve line
     *                 numbers for folding regions.
     * @param quick    whether the result should be provided as soon as possible. Is true, when
     *                 an editor is opened and we need to auto-fold something immediately, like Java imports.
     *                 If true, one should perform no reference resolving and avoid complex checks if possible.
     * @return the array of folding descriptors.
     */
    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        if (!root.getLanguage().isKindOf("XML")) return descriptors.toArray(new FoldingDescriptor[0]);

        Project project = root.getProject();
        Module module = MyPsiUtil.getModuleByPsiElement(root);

        root.accept(new XmlRecursiveElementWalkingVisitor(true) {
            /**
             * @param tag XML标签
             */
            @Override
            public void visitXmlTag(XmlTag tag) {
                super.visitXmlTag(tag);
                handleXmlTagI18nAttribute(tag, project, module, "Title", "value", descriptors, 1);
                handleXmlTagI18nAttribute(tag, project, module, "Field", "label", descriptors, 1);
//                handleXmlTagI18nAttribute(tag, project, module, "Enum", "display", descriptors);
                handleXmlTagI18nAttribute(tag, project, module, "column", "i18n", descriptors, 2);
            }
        });


        return descriptors.toArray(new FoldingDescriptor[0]);
    }

    /**
     * 处理XML标签的i18n属性
     * @param tag XML标签
     * @param project 项目
     * @param module 模块
     * @param tagName 标签Name
     * @param attributeName 该标签某属性Name
     * @param descriptors 折叠描述符
     * @param i18nType 1:datagrid; 2:module app; 3:web;
     */
    private static void handleXmlTagI18nAttribute(XmlTag tag, Project project, Module module, @NotNull String tagName, @NotNull String attributeName, List<FoldingDescriptor> descriptors, int i18nType) {
        if (tag.getName().equals(tagName)) {
            String key = tag.getAttributeValue(attributeName);
            if (key == null || key.isBlank()) return;

            List<Property> i18nProperties;

            if (i18nType == 1) {
                i18nProperties = MyPropertiesUtil.findModuleDataGridI18nProperties(project, module, key);
            } else if (i18nType == 2) {
                i18nProperties = MyPropertiesUtil.findModuleI18nProperties(project, module, key);
            } else if (i18nType == 3) {
                i18nProperties = MyPropertiesUtil.findModuleWebI18nProperties(project, module, key);
            } else {
                i18nProperties = Collections.emptyList();
            }

            List<Property> simpleProperty = i18nProperties;

            if (!simpleProperty.isEmpty()) {
                String placeholderText = simpleProperty.get(0).getValue();
                placeholderText = placeholderText == null ? StringUtil.THREE_DOTS :
                        "\"" + placeholderText + "\"";
                XmlAttribute xmlAttribute = tag.getAttribute(attributeName);
                if (xmlAttribute == null || xmlAttribute.getValueElement() == null) return;
                XmlAttributeValue valueElement = xmlAttribute.getValueElement();
                FoldingDescriptor foldingDescriptor = new FoldingDescriptor(valueElement,
                        valueElement.getTextOffset() - 1,
                        valueElement.getTextOffset() + valueElement.getTextLength(),
                        group,
                        placeholderText);
                descriptors.add(foldingDescriptor);
            }
        }
    }

    /**
     * Returns the text which is displayed in the editor for the folding region related to the
     * specified node when the folding region is collapsed.
     *
     * @param node the node for which the placeholder text is requested.
     * @return the placeholder text.
     */
    @Override
    public @Nullable String getPlaceholderText(@NotNull ASTNode node) {
        return null;
    }

    /**
     * Returns the default collapsed state for the folding region related to the specified node.
     *
     * @param node the node for which the collapsed state is requested.
     * @return true if the region is collapsed by default, false otherwise.
     */
    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return true;
    }

    /**
     * @param foldingDescriptor foldingDescriptor
     * @return true
     */
    @Override
    public boolean isCollapsedByDefault(@NotNull FoldingDescriptor foldingDescriptor) {
        return true;
    }
}
