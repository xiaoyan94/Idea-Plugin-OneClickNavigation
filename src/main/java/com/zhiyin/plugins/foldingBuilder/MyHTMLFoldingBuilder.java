package com.zhiyin.plugins.foldingBuilder;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.intellij.psi.xml.XmlText;
import com.intellij.psi.xml.XmlToken;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyHTMLFoldingBuilder extends FoldingBuilderEx {
    public static final Logger LOG = Logger.getInstance(MyHTMLFoldingBuilder.class);

    public static final FoldingGroup GROUP = FoldingGroup.newGroup(Constants.FOLDING_GROUP);

    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document,
                                                          boolean quick) {

        // Initialize the group of folding regions that will expand/collapse together.
//        FoldingGroup group = FoldingGroup.newGroup(Constants.FOLDING_GROUP);
        // Initialize the list of folding regions
        List<FoldingDescriptor> descriptors = new ArrayList<>();

//        Project project = root.getProject();
        Module module = MyPsiUtil.getModuleByPsiElement(root);
        if (module == null) {
            return descriptors.toArray(new FoldingDescriptor[0]);
        }
        Project project = root.getProject();

        root.accept(new XmlRecursiveElementVisitor() {
            private void visitXmlAttributeValueToken(XmlToken xmlToken) {
                XmlAttributeValueImpl xmlAttributeValue = (XmlAttributeValueImpl) xmlToken.getParent();
                PsiElement[] children = xmlAttributeValue.getChildren();
                if (children.length != 3) {
                    return;
                }

                PsiElement child = children[0];
                if (child instanceof XmlToken && !child.textContains('@')) {
//                    LOG.info("skip non \"");
                    return;
                }

                child = children[1];
                String text = child.getText();
                int startFolding = text.indexOf(Constants.I18N_KEY_PREFIX);
                if (!(child instanceof XmlToken) || startFolding < 0) {
//                    LOG.info("skip non <@message key=, text:" + text);
                    return;
                }

                int start = text.indexOf(Constants.I18N_KEY_PREFIX);
                if (start == -1){
                    return;
                }
                int end = start + text.substring(start).indexOf('\'');
                if (start >= end) {
                    return;
                }

                String key = text.substring(start, end);

                Property property = MyPropertiesUtil.findModuleI18nProperty(project, module, key);
//                LOG.info("key:" + key + ", property :" + property);

//                int endFolding = text.lastIndexOf('>') + 1;
                int endFolding = end + 1;
                if (property != null) {
                    String placeholderText = property.getValue();
                    placeholderText = placeholderText == null ? StringUtil.THREE_DOTS : placeholderText;
                    int textOffset = xmlToken.getTextOffset();
                    FoldingDescriptor foldingDescriptor = new FoldingDescriptor(xmlToken, textOffset + startFolding,
                            textOffset + endFolding, GROUP, placeholderText);
                    descriptors.add(foldingDescriptor);
                }
            }

            @Override
            public void visitXmlToken(XmlToken xmlToken) {
                super.visitXmlToken(xmlToken);

                if (xmlToken.getNextSibling() != null) {
                    if (xmlToken.getNextSibling().getNextSibling() == null && xmlToken.getParent() instanceof XmlAttributeValueImpl) {
                        visitXmlAttributeValueToken(xmlToken);
                    }
                    return;
                }

                PsiElement parent = xmlToken.getParent();
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

                String text1 = children[0].getText()
                                          .replaceAll(" ", "");
                if (!text1.startsWith("<@message")) {
                    return;
                }

                String text2 = children[2].getText()
                                          .replaceAll(" ", "")
                                          .replaceAll("'", "\"");
                if (!text2.startsWith("key=\"com.zhiyin.")) {
                    return;
                }

                int start = text2.indexOf(Constants.I18N_KEY_PREFIX);
                int end = text2.lastIndexOf("\"");

                if (start >= end) {
                    return;
                }

                String key = text2.substring(start, end);

                addFoldingDescriptor(xmlToken, parent, key);
            }

            private void addFoldingDescriptor(XmlToken xmlToken, PsiElement parent, String key) {
                String propertyValue = MyPropertiesUtil.findModuleWebI18nPropertyValue(project, module, key);
                LOG.info("key:" + key + ", propertyValue :" + propertyValue);

                if (propertyValue != null) {
                    FoldingDescriptor foldingDescriptor = new FoldingDescriptor(xmlToken, parent.getTextOffset(),
                            parent.getTextOffset() + parent.getTextLength(), GROUP, propertyValue);
                    descriptors.add(foldingDescriptor);
                }
            }

        });


        //region Freemarker 自定义@message指令支持
        root.accept(new PsiRecursiveElementWalkingVisitor(){

            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);

//                System.out.println(element.getParent() + " -- " + element);
                String keyValue = MyPsiUtil.retrieveI18nKeyFromFreemarkerDirective(element);
                if (keyValue != null) {
                    String propertyValue = MyPropertiesUtil.findModuleWebI18nPropertyValue(project, module, keyValue);
//                    LOG.info("Freemarker FtlStringLiteral child: key:" + keyValue + ", propertyValue :" + propertyValue);

                    if (propertyValue != null) {
                        FoldingDescriptor foldingDescriptor = new FoldingDescriptor(element, element.getTextOffset(),
                                element.getTextOffset() + element.getTextLength(), GROUP, propertyValue);
                        descriptors.add(foldingDescriptor);
                    }
                }

            }

            @Override
            protected void elementFinished(PsiElement element) {
                super.elementFinished(element);
            }

            @Override
            public void visitFile(@NotNull PsiFile file) {
                super.visitFile(file);
            }

            @Override
            public void stopWalking() {
                super.stopWalking();
            }
        });
        //endregion

        return descriptors.toArray(FoldingDescriptor.EMPTY);
    }

    @Override
    public @Nullable String getPlaceholderText(@NotNull ASTNode node) {
        return null;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return true;
    }

}
