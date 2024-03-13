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
import com.intellij.psi.*;
import com.intellij.psi.util.PsiLiteralUtil;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A folding builder identifies the folding regions in the code.
 *
 * @author yan on 2024/3/6 22:41
 */
public class MyJavaFoldingBuilder extends FoldingBuilderEx {

    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document,
                                                          boolean quick) {

        // Initialize the group of folding regions that will expand/collapse together.
        FoldingGroup group = FoldingGroup.newGroup(Constants.FOLDING_GROUP);
        // Initialize the list of folding regions
        List<FoldingDescriptor> descriptors = new ArrayList<>();

        Module module = MyPsiUtil.getModuleByPsiElement(root);
        Project project = module.getProject();

        root.accept(new JavaRecursiveElementWalkingVisitor() {

            @Override
            public void visitLiteralExpression(@NotNull PsiLiteralExpression literalExpression) {
                super.visitLiteralExpression(literalExpression);

                PsiElement parent = literalExpression.getParent();
                if (!(parent instanceof PsiExpressionList)) {
                    return;
                }

                parent = parent.getParent();
                if (!(parent instanceof PsiMethodCallExpression)) {
                    return;
                }

                String value = PsiLiteralUtil.getStringLiteralContent(literalExpression);
                if (value != null && value.startsWith(Constants.I18N_KEY_PREFIX)) {
                    // find SimpleProperty for the given key in the project
                    Property simpleProperty = MyPropertiesUtil.findModuleI18nProperty(project, module, value);
                    if (simpleProperty != null) {
                        // Add a folding descriptor for the literal expression at this node.
                        String propertyValue = simpleProperty.getValue();
                        if (propertyValue != null) {
                            propertyValue = propertyValue.replaceAll("\n", "\\n")
                                                         .replaceAll("\"", "\\\\\"");
                            propertyValue = "\"" + propertyValue + "\"";
                        } else {
                            propertyValue = StringUtil.THREE_DOTS + Constants.INVALID_I18N_KEY;
                        }
                        FoldingDescriptor foldingDescriptor = new FoldingDescriptor(literalExpression,
                                parent.getTextOffset(),
                                parent.getTextOffset() + parent.getTextLength(), group,
                                propertyValue);
                        foldingDescriptor.setGutterMarkEnabledForSingleLine(true);
                        descriptors.add(foldingDescriptor);
                    }
                }
            }
        });

        return descriptors.toArray(FoldingDescriptor.EMPTY);

    }

    /**
     * 自定义折叠显示文本
     *
     * @param node the node for which the placeholder text is requested.
     * @return 折叠时的字符串
     */
    @Override
    public @Nullable String getPlaceholderText(@NotNull ASTNode node) {
        return StringUtil.THREE_DOTS;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {

        return true;
    }

}
