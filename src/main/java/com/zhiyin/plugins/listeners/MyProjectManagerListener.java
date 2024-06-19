package com.zhiyin.plugins.listeners;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.NoAccessDuringPsiEvents;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.util.Condition;
import com.intellij.util.xml.DomService;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.service.MyProjectService;
import org.jetbrains.annotations.NotNull;

/**
 * execute something after loading the project and after the indexing finishes
 *
 * 
 */
public class MyProjectManagerListener implements ProjectManagerListener {

    private static final Logger LOG = Logger.getInstance(MyProjectManagerListener.class);

    /**
     * Invoked on project open. Executed in EDT.
     *
     * @param project opening project
     */
    @Override
    public void projectOpened(@NotNull Project project) {
        ProjectManagerListener.super.projectOpened(project);

        LOG.info(String.format("project opened:%s", project.getName()));

//        ApplicationManager.getApplication().invokeLater(initXmlFileMapInstantly(project));
        // runWhenSmart
        DumbService.getInstance(project).runWhenSmart(() -> {
            // 异步执行
//            ApplicationManager.getApplication().executeOnPooledThread (() -> {
//                ApplicationManager.getApplication().runReadAction(initXmlFileMapInstantly(project));
//            });
            if (project.isDisposed() || NoAccessDuringPsiEvents.isInsideEventProcessing()) {
                return;
            }

            MyProjectService myProjectService = project.getService(MyProjectService.class);
            System.out.println("reInitXmlFileMap start");
            myProjectService.reInitXmlFileMap();

            System.out.println("return runWhenSmart");
        });

    }

    @NotNull
    private static Runnable initXmlFileMapInstantly(@NotNull Project project) {
        return () -> {
            if (project.isDisposed() || NoAccessDuringPsiEvents.isInsideEventProcessing()) {
                return;
            }

            MyProjectService myProjectService = project.getService(MyProjectService.class);
            int size = myProjectService.getXmlFileMap().size();
            LOG.info("myProjectService init xmlFileMap: " + size);

        };
    }

}
