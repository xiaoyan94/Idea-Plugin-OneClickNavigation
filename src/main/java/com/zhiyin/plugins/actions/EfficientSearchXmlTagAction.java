package com.zhiyin.plugins.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.zhiyin.plugins.service.ComboboxUrlService;

import java.util.List;

public class EfficientSearchXmlTagAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        String tagName = Messages.showInputDialog(project, "Enter XML tag name to search:", "Search XML Tag", null);
        if (tagName == null || tagName.isEmpty()) return;

        ComboboxUrlService service = project.getService(ComboboxUrlService.class);
        service.searchAndCacheXmlTags(tagName, "value");

        List<String> cachedResults = service.getCachedResults();

        if (cachedResults.isEmpty()) {
            Messages.showInfoMessage("No matching tags found.", "Search Result");
        } else {
            String resultMessage = String.join("\n", cachedResults);
            Messages.showInfoMessage(resultMessage, "Search Result");
        }
    }
}
