package com.zhiyin.plugins.foldingBuilder;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 折叠 HTML 中的指令式资源串
 */
public class CustomHtmlFoldingBuilder extends FoldingBuilderEx {

    // 修改后的正则表达式，同时匹配单引号和双引号
    private static final Pattern PATTERN = Pattern.compile("<@message\\s+key=['\"](.*?)['\"]\\s*/>");

    public static final FoldingGroup GROUP = FoldingGroup.newGroup(Constants.FOLDING_GROUP);

    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        String text = document.getText();
        Matcher matcher = PATTERN.matcher(text);

        while (matcher.find()) {
            int startOffset = matcher.start();
            int endOffset = matcher.end();

            // 查找可以代表整个匹配区域的 PSI 元素
            PsiElement element = findEnclosingElement(root, new TextRange(startOffset, endOffset));

            if (element != null) {
                TextRange range = new TextRange(startOffset, endOffset);
                descriptors.add(new FoldingDescriptor(element.getNode(), range, GROUP));
            }
        }
        return descriptors.toArray(new FoldingDescriptor[0]);
    }

    /**
     * 辅助方法：向上遍历 PSI 树，找到一个能完整包含给定范围的元素。
     */
    private PsiElement findEnclosingElement(@NotNull PsiElement root, @NotNull TextRange range) {
        PsiElement currentElement = root.findElementAt(range.getStartOffset());
        if (currentElement == null) {
            return null;
        }

        // 向上遍历，直到找到一个包含整个范围的父元素
        while (currentElement != null && !currentElement.getTextRange().contains(range)) {
            currentElement = currentElement.getParent();
        }

        // 确保找到的元素不包含多余的子元素
        // 这是一个更精确的检查，以确保我们只折叠 <@message ... />
        if (currentElement != null && currentElement.getTextRange().equals(range)) {
            return currentElement;
        }

        // 如果找不到完全匹配的，返回最接近的祖先元素
        return currentElement;
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

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        String tagText = node.getText();
        Matcher matcher = PATTERN.matcher(tagText);
        if (matcher.find()) {
            // group(1) 依然是 key 的值
            String key = matcher.group(1);

            // 1. 从 ASTNode 获取 PsiElement
            PsiElement psiElement = node.getPsi();
            if (psiElement == null) {
                return "${" + key + "}";
            }

            // 2. 获取 Project
            Project project = psiElement.getProject();

            Module module = MyPsiUtil.getModuleByPsiElement(psiElement);
            if (module == null) {
                return "${" + key + "}";
            }

            String property = MyPropertiesUtil.findModuleI18nPropertyValue(project, module, key);
            if (property != null && !property.isEmpty()) {
                return property;
            } else {
                property = MyPropertiesUtil.findModuleWebI18nPropertyValue(project, module, key);
                if (property != null && !property.isEmpty()) {
                    return property;
                }
                return "${" + key + "}";
            }
        }
        return "...";
    }

}