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
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.psi.xml.XmlToken;
import com.zhiyin.plugins.notification.MyPluginMessages;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MyJspI18nFoldingBuilder extends FoldingBuilderEx {
    public static final Logger LOG = Logger.getInstance(MyJspI18nFoldingBuilder.class);

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

        //region JSP 自定义mes:message标签支持
        root.accept(new PsiRecursiveElementWalkingVisitor(){

            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);
                if (!(element instanceof XmlTag) || !"mes:message".equalsIgnoreCase(((XmlTag) element).getName())) {
                    return;
                } /*else {
                    MyPluginMessages.showInfo("mes:message标签不支持折叠", "mes:message标签不支持折叠", project);
                }*/

                String keyValue = ((XmlTag) element).getAttributeValue("key");

                if (keyValue != null) {
                    String propertyValue = MyPropertiesUtil.findModuleWebI18nPropertyValue(project, module, keyValue);
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
