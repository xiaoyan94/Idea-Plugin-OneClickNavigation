package com.zhiyin.plugins.foldingBuilder;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSRecursiveWalkingElementVisitor;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaScript
 *
 * @author yan on 2024/3/11 00:49
 */
public class MyJavaScriptFoldingBuilder extends FoldingBuilderEx {
    public static final FoldingGroup group = FoldingGroup.newGroup(Constants.FOLDING_GROUP);
    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document,
                                                          boolean quick) {

        // Initialize the group of folding regions that will expand/collapse together.
//        FoldingGroup group = FoldingGroup.newGroup(Constants.FOLDING_GROUP);
        // Initialize the list of folding regions
        List<FoldingDescriptor> descriptors = new ArrayList<>();

        Project project = root.getProject();
        Module module = MyPsiUtil.getModuleByPsiElement(root);

        root.accept(new JSRecursiveWalkingElementVisitor(){

            @Override
            public void visitJSLiteralExpression(JSLiteralExpression literalExpression) {
                super.visitJSLiteralExpression(literalExpression);
                PsiElement parent = literalExpression.getParent();
                if (parent == null || parent.getParent() == null) return;
                if (! (parent.getParent() instanceof JSCallExpression)) return;
                PsiElement foldingParent = parent.getParent();
                String value = String.valueOf(literalExpression.getValue());
                if (value != null && value.startsWith(Constants.I18N_KEY_PREFIX)) {

                    List<Property> simpleProperty = MyPropertiesUtil.findModuleI18nProperties(project, module, value);

                    if (simpleProperty.size() > 0) {
                        String placeholderText = simpleProperty.get(0)
                                                      .getValue();
                        placeholderText = placeholderText == null ? StringUtil.THREE_DOTS :
                                "\"" + placeholderText + "\"";
                        FoldingDescriptor foldingDescriptor = new FoldingDescriptor(literalExpression,
                                foldingParent.getTextOffset(),
                                foldingParent.getTextOffset() + foldingParent.getTextLength(), group,
                                placeholderText);
                        descriptors.add(foldingDescriptor);
                    }

                }

            }

        });

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
