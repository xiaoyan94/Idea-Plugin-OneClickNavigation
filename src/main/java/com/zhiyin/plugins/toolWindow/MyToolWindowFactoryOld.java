package com.zhiyin.plugins.toolWindow;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.project.Project;
import javax.swing.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiMethod;
import com.zhiyin.plugins.service.ControllerUrlService;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MyToolWindowFactoryOld implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel panel = new JPanel();
        
        // Create an input field and button
        JTextField urlField = new JTextField(20);
        JButton jumpButton = new JButton("Jump to Controller");

        // Define the action when the button is clicked
        jumpButton.addActionListener(e -> {
            String url = urlField.getText();
            // Call method to navigate based on the URL input
            navigateToController(project, url);
        });

        // Add components to the panel
        panel.add(new JLabel("Enter URL:"));
        panel.add(urlField);
        panel.add(jumpButton);

        // Create and set up the tool window panel
        SimpleToolWindowPanel simpleToolWindowPanel = new SimpleToolWindowPanel(true, false);
        simpleToolWindowPanel.setContent(panel);
        toolWindow.getComponent().add(simpleToolWindowPanel);
    }

    private void navigateToController(Project project, String url) {
        ControllerUrlService controllerUrlService = project.getService(ControllerUrlService.class);
        List<PsiMethod> psiMethods = controllerUrlService.getMethodsForUrlFuzzy(url);

        if (!psiMethods.isEmpty()) {
            navigateToMethod(project, psiMethods);
        } else {
            Messages.showErrorDialog(project, "No Controller found for the URL", "Navigation Error");
        }
    }

    private void navigateToMethod(Project project, List<PsiMethod> psiMethods) {
        if (psiMethods == null || psiMethods.isEmpty()) {
            Messages.showErrorDialog(project, "No methods found to navigate to.", "Navigation Error");
            return;
        }

        for (PsiMethod psiMethod : psiMethods) {
            // Using IntelliJ API to navigate to the method
            NavigationUtil.activateFileWithPsiElement(psiMethod);
        }
    }

}
