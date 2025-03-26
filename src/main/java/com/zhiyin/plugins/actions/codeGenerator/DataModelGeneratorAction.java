package com.zhiyin.plugins.actions.codeGenerator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.search.ProjectScope;
import com.zhiyin.plugins.ui.codeGenerator.DataModelGenerator;
import org.jetbrains.annotations.NotNull;

public class DataModelGeneratorAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            Messages.showErrorDialog("Project is null", "Error");
            return;
        }
        Module module = e.getData(PlatformDataKeys.MODULE);
        if (module == null) {
            Messages.showErrorDialog("Module is null", "Error");
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            // DataModelGenerator dataModelGenerator = project.getService(DataModelGenerator.class);
            DataModelGenerator dataModelGenerator = new DataModelGenerator(project, module);
            dataModelGenerator.show();
        });
    }
}
