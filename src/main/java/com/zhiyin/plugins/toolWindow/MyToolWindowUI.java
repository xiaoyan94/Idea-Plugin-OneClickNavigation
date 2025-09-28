package com.zhiyin.plugins.toolWindow;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.zhiyin.plugins.notification.MyPluginMessages;
import com.zhiyin.plugins.service.ControllerUrlService;
import com.zhiyin.plugins.service.MyProjectService;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MyToolWindowUI {

    private final JTextField urlField = new JTextField(20);
    private final JButton jumpButton = new JButton("Find");
    private final JButton reloadButton = new JButton("Reset");
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> methodsList = new JBList<>(listModel);

    private final Project project;
    private final ToolWindow toolWindow;

    public MyToolWindowUI(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        initUI();
    }

    private void initUI() {
        jumpButton.setIcon(AllIcons.Actions.Find);
        jumpButton.setToolTipText("查询接口URL对应的Controller");
        reloadButton.setIcon(AllIcons.Actions.Refresh);
        reloadButton.setToolTipText("重置数据缓存");

        methodsList.setCellRenderer(new MethodListCellRenderer());

        // 查询按钮事件
        jumpButton.addActionListener(e -> {
            String url = urlField.getText().trim();
            navigateToControllerForCustomRender(project, url);
            autoNavigationAfterSearchAction();
        });

        // 回车事件
        urlField.addActionListener(e -> {
            String url = urlField.getText().trim();
            navigateToControllerForCustomRender(project, url);
            autoNavigationAfterSearchAction();
        });

        // 重置按钮
        reloadButton.addActionListener(e -> {
            project.getService(MyProjectService.class).reInitXmlFileMap();
            project.getService(ControllerUrlService.class).initControllerUrlsCache();
        });

        // 列表选择事件
        methodsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && methodsList.getSelectedIndex() != -1) {
                String selected = methodsList.getSelectedValue();
                if (selected.contains(" - ")) {
                    String[] split = selected.split(" - ");
                    String methodSignature;
                    if (split.length == 3) {
                        methodSignature = split[1];
                    } else if (split.length == 2) {
                        methodSignature = split[0];
                    } else {
                        methodSignature = split[0];
                    }
                    navigateToSelectedMethod(project, methodSignature);
                } else {
                    navigateToSelectedMethod(project, selected);
                }
            }
        });
    }

    public JComponent getContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));

        JLabel urlLabel = new JLabel("Url:");
        urlField.setMaximumSize(new Dimension(Integer.MAX_VALUE, urlField.getPreferredSize().height));

        inputPanel.add(urlLabel);
        inputPanel.add(Box.createHorizontalStrut(10));
        inputPanel.add(urlField);
        inputPanel.add(Box.createHorizontalGlue());
        inputPanel.add(jumpButton);
        inputPanel.add(reloadButton);

        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JScrollPane listScrollPane = new JBScrollPane(methodsList);
        listScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        panel.add(inputPanel);
        panel.add(Box.createVerticalGlue());
        panel.add(listScrollPane);

        panel.setMinimumSize(new Dimension(300, 0));

        SimpleToolWindowPanel simplePanel = new SimpleToolWindowPanel(true, false);
        simplePanel.setContent(panel);

        return simplePanel;
    }

    public void setUrlText(String text) {
        urlField.setText(text);
    }

    public void clickJumpButton() {
        jumpButton.doClick();
    }

    // === 以下保留你原来的一些方法 ===

    private void navigateToControllerForCustomRender(Project project, String url) {
        ControllerUrlService service = project.getService(ControllerUrlService.class);
        Map<String, List<PsiMethod>> methodsMap = service.getMethodsMapForUrlFuzzy(url);

        listModel.clear();
        for (Map.Entry<String, List<PsiMethod>> entry : methodsMap.entrySet()) {
            String methodUrl = entry.getKey();
            for (PsiMethod method : entry.getValue()) {
                String methodSignature = Objects.requireNonNull(method.getContainingClass()).getQualifiedName()
                        + "#" + method.getName() + "()";
                Module module = MyPsiUtil.getModuleByPsiElement(method);
                if (module!= null) {
                    String moduleName = MyPropertiesUtil.getSimpleModuleName(module);
                    listModel.addElement(moduleName + " - " + methodSignature + " - " + methodUrl);
                } else {
                    listModel.addElement(methodSignature + " - " + methodUrl);
                }
            }
        }
        sortListModel();
    }

    private void autoNavigationAfterSearchAction() {
        if (!listModel.isEmpty()) {
            if (listModel.getElementAt(0).endsWith(urlField.getText().trim())) {
                methodsList.setSelectedIndex(0);
                if (listModel.size() < 3) {
                    toolWindow.hide(null);
                }
                return;
            }
        }
        if (listModel.size() == 1) {
            methodsList.setSelectedIndex(0);
            toolWindow.hide(null);
        }
        if (listModel.size() >= 2 && listModel.size() < 10) {
            if (listModel.getElementAt(0).contains("RestController") &&
                listModel.getElementAt(1).contains("FeignService")) {
                methodsList.setSelectedIndex(0);
                if (listModel.size() < 3) {
                    toolWindow.hide(null);
                }
            }
        }
    }

    private void sortListModel() {
        List<String> list = Collections.list(listModel.elements());
        list.sort((s1, s2) -> {
            if (s1.contains("RestController") && !s2.contains("RestController")) return -1;
            if (!s1.contains("RestController") && s2.contains("RestController")) return 1;
            if (s1.contains("FeignService") && !s2.contains("FeignService")) return 1;
            if (!s1.contains("FeignService") && s2.contains("FeignService")) return -1;
            return s1.compareTo(s2);
        });
        listModel.clear();
        list.forEach(listModel::addElement);
    }

    private void navigateToSelectedMethod(Project project, String methodSignature) {
        String className = methodSignature.substring(0, methodSignature.lastIndexOf('#'));
        String methodName = methodSignature.substring(methodSignature.lastIndexOf('#') + 1, methodSignature.lastIndexOf('('));

        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            PsiClass psiClass = psiFacade.findClass(className, GlobalSearchScope.moduleScope(module)
                    .union(GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)));
            if (psiClass != null) {
                for (PsiMethod method : psiClass.getMethods()) {
                    if (method.getName().equals(methodName)) {
                        NavigationUtil.activateFileWithPsiElement(method);
                        return;
                    }
                }
            }
        }

        MyPluginMessages.showInfo("Method not found", "Method not found: " + methodSignature, project);
    }

    static class MethodListCellRenderer extends JLabel implements ListCellRenderer<String> {
        @Override
        public Component getListCellRendererComponent(
                JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            String[] parts = value.split(" - ");
            String methodSignature = "";
            String methodUrl = "";
            String moduleName = "";
            if (parts.length == 3) {
                moduleName = parts[0];
                methodSignature = parts[1];
                methodUrl = parts[2];
            } else if (parts.length == 2) {
                methodSignature = parts[0];
                methodUrl = parts[1];
            } else if (parts.length == 1) {
                methodSignature = parts[0];
                methodUrl = parts[0];
            }

            if (methodSignature.contains("Controller")) {
                setIcon(AllIcons.Nodes.Class);
            } else {
                setIcon(AllIcons.Nodes.Interface);
            }


            setText("<html><table cellpadding='0' cellspacing='0'>" +
                    "<tr>" +
                    "<td colspan='2' align='left' text-align:left;><b style='color:#4CAF50;'>" + methodUrl + "</b></td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td align='left' style='color:#00A5FF; text-align:left; width:50px;'>" + moduleName + "</td>" +
                    "<td align='left' style='color:#A9A9A9; text-align:left;'><i>" + methodSignature + "</i></td>" +
                    "</tr>" +
                    "</table></html>");

            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
    }
}
