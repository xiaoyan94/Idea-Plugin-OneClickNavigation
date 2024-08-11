package com.zhiyin.plugins.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.zhiyin.plugins.service.ControllerUrlService;

import java.util.*;

public class CollectUrlsAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        GlobalSearchScope scope = GlobalSearchScope.fileScope(e.getRequiredData(CommonDataKeys.PSI_FILE));
//        scope = GlobalSearchScope.allScope(project);

        ControllerUrlService urlService = project.getService(ControllerUrlService.class);
        urlService.collectControllerUrls(scope, () -> {
            // 在URL收集完成后执行
//            showCollectedUrls(project, urlService);
            testUrls(urlService);
        });

    }

    private void testUrls(ControllerUrlService urlService) {
        String[] testUrls = {
//                "/Order/OrderSort/endFastStopDevice",
//                "/MesRoot/sysadm/Role/updateRoleById",
//                "/Mold/downloadTemplate",
//                "/Report/ProcessCapacityReport/exportProcessCapacityReport",
//                "/api/v2/basic/getStationListByFactoryId",
                "/feign-api/Device/insertDeviceDetailPic",
                "feign-api/Device/insertDeviceDetailPic"
//                "/feign-api/Mold/getMoldMonitorList",
//                "feign-api/Mold/getMoldMonitorList"
        };

        for (String url : testUrls) {
            navigateToUrl(url, urlService);
            Messages.showInfoMessage("Test URL: " + url, "Test Result");
        }
    }

    private void navigateToUrl(String url, ControllerUrlService urlService) {
        if (url != null && !url.startsWith("/")) {
            url = "/" + url;
        }
        List<PsiMethod> methods = urlService.getMethodForUrl(url);
        if (methods != null && !methods.isEmpty()) {
            String finalUrl = url;
            methods.forEach(method -> {
                method.navigate(true);
                System.out.println("Successfully navigated to: " + finalUrl);
                System.out.println("Method: " + method.getName());
                System.out.println("Containing class: " + Objects.requireNonNull(method.getContainingClass()).getQualifiedName());
                System.out.println("File: " + method.getContainingFile().getVirtualFile().getPath());
                System.out.println();
            });

        } else {
            System.out.println("Could not find method for URL: " + url);
            System.out.println();
        }
    }

    @SuppressWarnings("unused")
    private void showCollectedUrls(Project project, ControllerUrlService urlService) {
        StringBuilder message = new StringBuilder("Collected URLs:\n");
        Map<PsiClass, Set<String>> allUrls = urlService.getAllControllerUrls();
        for (Map.Entry<PsiClass, Set<String>> entry : allUrls.entrySet()) {
            message.append("Controller: ").append(entry.getKey().getQualifiedName()).append("\n");
            for (String url : entry.getValue()) {
                message.append("  ").append(url).append("\n");
            }
            message.append("\n");
        }

        Messages.showMessageDialog(project, message.toString(), "Controller URLs", Messages.getInformationIcon());
    }
}
