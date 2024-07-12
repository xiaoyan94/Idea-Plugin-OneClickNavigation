package com.zhiyin.plugins.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.zhiyin.plugins.service.MyApplicationService;

public class ProjectTypeChecker {
    public static boolean isTraditionalJavaWebProject(Project project, Module module) {
        MyApplicationService service = ApplicationManager.getApplication().getService(MyApplicationService.class);
        return service.checkIsOldMesProject(project, module);
    }


}
