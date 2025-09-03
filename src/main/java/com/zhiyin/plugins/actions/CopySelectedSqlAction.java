package com.zhiyin.plugins.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.zhiyin.plugins.notification.MyPluginMessages;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CopySelectedSqlAction extends AnAction {

    // 正则表达式用于匹配动态标签，如 <if ...> 和 </if>
    private static final Pattern DYNAMIC_TAG_PATTERN = Pattern.compile(
            "<(select|insert|update|delete|if|where|set|trim|foreach|choose|when|otherwise).*?>|</(select|insert|update|delete|if|where|set|trim|foreach|choose|when|otherwise)>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 获取项目和当前文件
        Project project = e.getProject();
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        // 默认不显示 Action
        boolean isVisible = false;

        // 检查文件是否存在且为 XML 文件
        if (project != null && psiFile instanceof XmlFile) {
            XmlTag rootTag = ((XmlFile) psiFile).getRootTag();
            if (rootTag != null && "mapper".equalsIgnoreCase(rootTag.getName())) {
                String namespace = rootTag.getAttributeValue("namespace");
                if (namespace != null && !namespace.isEmpty()) {
                    // 如果是 MyBatis Mapper 文件，并且有选中的文本，则显示 Action
                    Editor editor = e.getData(CommonDataKeys.EDITOR);
                    if (editor != null && editor.getSelectionModel().hasSelection()) {
                        isVisible = true;
                    }
                }
            }
        }

        // 设置 Action 的可见性
        e.getPresentation().setVisible(isVisible);
        e.getPresentation().setEnabled(isVisible);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 获取编辑器对象
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }

        // 获取用户选中的文本
        String selectedText = editor.getSelectionModel().getSelectedText();
        if (selectedText == null || selectedText.trim().isEmpty()) {
            MyPluginMessages.showWarning("警告", "请先选中要转换的 SQL 语句。", e.getProject());
            return;
        }

        // 处理文本，移除动态标签
        String processedSql = removeDynamicTags(selectedText);

        // 处理参数占位符 #{...} 和 ${...}
        // 由于无法获取参数的实际值，这一步需要用户手动输入或提供一个简化的替换
        processedSql = replacePlaceholders(processedSql);

        // 去除多余的空白，移除空行，移除每行开头的空白
        processedSql = processedSql.trim().replaceAll("\\n\\s*\\n", "\n").replaceAll("\\n\\s*", "\n");

        if (!processedSql.isEmpty()) {
            // 将处理后的SQL复制到剪贴板
            CopyPasteManager.getInstance().setContents(new StringSelection(processedSql));
            MyPluginMessages.showInfo("已复制", processedSql, e.getProject());
        } else {
            CopyPasteManager.getInstance().setContents(new StringSelection(selectedText));
            MyPluginMessages.showWarning("警告", "未能提取有效的 SQL 语句。", e.getProject());
        }
    }

    /**
     * 移除动态标签
     */
    private String removeDynamicTags(String sql) {
        // 替换 `<where>` 为 WHERE，忽略大小写
        sql = sql.replaceAll("<where>", "WHERE 1=1");
        Matcher matcher = DYNAMIC_TAG_PATTERN.matcher(sql);
        return matcher.replaceAll("");
    }

    /**
     * 替换占位符
     * 这里提供一个简单的替换方案，用 '?' 或 '参数值' 代替
     */
    private String replacePlaceholders(String sql) {
        // 替換 #{language} 和 ${language} 为 'zh_CN'
        sql = sql.replaceAll("(#\\{language}|\\$\\{language})", "'zh_CN'");
        // 替换 #{...} 为 '?'
        String result = sql.replaceAll("#\\{.*?\\}", "'?'");
        // 替换 ${...} 为 '?'
        result = result.replaceAll("\\$\\{.*?\\}", "'?'");
        /*// 替换 <![CDATA[ ... ]]> 为 '...'
        result = result.replaceAll("<!\\[CDATA\\[(.*?)\\]\\]>", "$1");*/
        // 替换单行或多行CDATA标签（支持换行）。(?s) 开启 DOTALL 模式
        result = result.replaceAll("(?s)<!\\[CDATA\\[(.*?)\\]\\]>", "$1");
        return result;
    }
}