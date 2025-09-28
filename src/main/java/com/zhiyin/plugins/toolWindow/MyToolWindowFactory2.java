package com.zhiyin.plugins.toolWindow;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.project.Project;
import javax.swing.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.zhiyin.plugins.service.ControllerUrlService;
import com.zhiyin.plugins.service.MyProjectService;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MyToolWindowFactory2 implements ToolWindowFactory {

    private final JTextField urlField = new JTextField(20);
    private final JButton jumpButton = new JButton("Find");

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setTitle("OneClickNavigationToolWindow");
        JPanel panel = new JPanel();

        // 使用 BoxLayout 来布局，以确保 JList 占满剩余空间
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // 创建输入框和按钮
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));  // 横向排列

        JLabel urlLabel = new JLabel("Url:");
        JButton reloadButton = new JButton("Reset");
        jumpButton.setIcon(AllIcons.Actions.Find);
        jumpButton.setToolTipText("查询接口URL对应的Controller");
        reloadButton.setIcon(AllIcons.Actions.Refresh);
        reloadButton.setToolTipText("重置数据缓存");
        // 让 JTextField 占据剩余空间
        urlField.setMaximumSize(new Dimension(Integer.MAX_VALUE, urlField.getPreferredSize().height));

        inputPanel.add(urlLabel);
        inputPanel.add(Box.createHorizontalStrut(10));  // 添加间隔
        inputPanel.add(urlField);
        inputPanel.add(Box.createHorizontalGlue());  // 占据剩余空间
        inputPanel.add(jumpButton);
        inputPanel.add(reloadButton);

        // 设置 JTextField 和 JButton 的固定高度
        inputPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 30));  // 设定最大高度

        // 创建一个列表组件，用于显示匹配的控制器方法
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> methodsList = new JBList<>(listModel);
        methodsList.setCellRenderer(new MethodListCellRenderer());
        // methodsList.setSelectionBackground(new Color(0xFF8C00));  // 亮蓝色背景
        JScrollPane listScrollPane = new JBScrollPane(methodsList);

        // 设置 JScrollPane 占满剩余空间
        listScrollPane.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // 按钮点击事件：查找并显示匹配的控制器方法
        jumpButton.addActionListener(e -> {
            String url = urlField.getText().trim();
            navigateToControllerForCustomRender(project, url, listModel);  // 查找并显示方法

            autoNavigationAfterSearchAction(listModel, methodsList, toolWindow);
        });

        reloadButton.addActionListener(e -> {
            project.getService(MyProjectService.class).reInitXmlFileMap();
            project.getService(ControllerUrlService.class).initControllerUrlsCache();
        });

        // 回车事件
        urlField.addActionListener(e -> {
            String url = urlField.getText();
            navigateToControllerForCustomRender(project, url, listModel);  // 查找并显示方法

            autoNavigationAfterSearchAction(listModel, methodsList, toolWindow);
        });

        // 选择方法后跳转
        methodsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && methodsList.getSelectedIndex() != -1) {
                String selectedMethod = methodsList.getSelectedValue();
                // 提取selectedMethod

                if (selectedMethod != null) {
                    // 分割选中的项，获取 methodSignature 和 methodUrl
                    String[] parts = selectedMethod.split(" - ");
                    if (parts.length == 2) {
                        String methodSignature = parts[0];  // 方法签名部分
                        String methodUrl = parts[1];  // URL 部分

                        // 输出方法签名和 URL，或者根据需要进行处理
                        System.out.println("Method Signature: " + methodSignature);
                        System.out.println("Method URL: " + methodUrl);

                        // 你可以在这里做进一步的处理，比如跳转到相应的代码行等
                    } else {
                        // 处理意外格式的情况
                        System.out.println("Selected item is in an unexpected format.");
                    }

                    navigateToSelectedMethod(project, selectedMethod);  // 根据选择跳转
                } else {
                    System.out.println("No method selected.");
                }
            }
        });

        // 将组件添加到面板
        panel.add(inputPanel);

        // 添加竖向间隔，确保 JList 填满剩余空间
        panel.add(Box.createVerticalGlue());
        panel.add(listScrollPane);

        // panel设置最小宽度
        panel.setMinimumSize(new Dimension(300, 0));

        // 创建并设置ToolWindow的面板
        SimpleToolWindowPanel simpleToolWindowPanel = new SimpleToolWindowPanel(true, false);
        simpleToolWindowPanel.setContent(panel);
        toolWindow.getComponent().add(simpleToolWindowPanel);
    }

    private static void autoNavigationAfterSearchAction(DefaultListModel<String> listModel, JList<String> methodsList, @NotNull ToolWindow toolWindow) {
        if (listModel.size() == 1) {
            methodsList.setSelectedIndex(0);
            toolWindow.hide(null);
        }

        // 如果2个且第一个包含RestController 且第二个包含FeignService子串，则选中第一个
        if (listModel.size() >= 2 && listModel.size() < 10) {
            if (listModel.getElementAt(0).contains("RestController") && listModel.getElementAt(1).contains("FeignService")) {
                methodsList.setSelectedIndex(0);
                toolWindow.hide(null);
            }
        }
    }

    // 查找与URL匹配的控制器方法，并显示在ToolWindow中
    private void navigateToController(Project project, String url, DefaultListModel<String> listModel) {

        ControllerUrlService controllerUrlService = project.getService(ControllerUrlService.class);
        List<PsiMethod> psiMethods = controllerUrlService.getMethodsForUrlFuzzy(url);

        if (psiMethods.isEmpty()) {
            Messages.showErrorDialog(project, "No Controller found for the URL", "Navigation Error");
        } else {
            listModel.clear();
            /*// 添加匹配的PsiMethod到列表
            for (PsiMethod method : psiMethods) {
                listModel.addElement(method.getName());  // 可以根据需要显示方法的名称或其他信息
            }*/

            // 添加匹配的PsiMethod的全限定名到列表
            for (PsiMethod method : psiMethods) {
                String methodSignature = Objects.requireNonNull(method.getContainingClass()).getQualifiedName() + "." + method.getName() + "()";  // 全限定名
                listModel.addElement(methodSignature);  // 显示完整方法签名
            }

            sortListModel(listModel);
        }

    }

    // 查找与URL匹配的控制器方法，并显示在ToolWindow中
    private void navigateToControllerForCustomRender(Project project, String url, DefaultListModel<String> listModel) {
        ControllerUrlService controllerUrlService = project.getService(ControllerUrlService.class);

        // 获取 URL 和对应的 PsiMethod 列表映射
        Map<String, List<PsiMethod>> methodsMapForUrlFuzzy = controllerUrlService.getMethodsMapForUrlFuzzy(url);

        // 清空当前列表
        listModel.clear();

        // 遍历 methodsMapForUrlFuzzy，拼接 methodSignature 和 URL 并添加到 listModel
        for (Map.Entry<String, List<PsiMethod>> entry : methodsMapForUrlFuzzy.entrySet()) {
            String methodUrl = entry.getKey();  // URL
            List<PsiMethod> psiMethods = entry.getValue();  // 对应的 PsiMethod 列表

            // 对于每个方法，拼接 methodSignature + " - " + methodUrl 形式
            for (PsiMethod method : psiMethods) {
                String methodSignature = Objects.requireNonNull(method.getContainingClass()).getQualifiedName()
                        + "." + method.getName() + "()";  // 全限定名

                // 拼接成 "methodSignature - methodUrl"
                String displayText = methodSignature + " - " + methodUrl;

                // 将拼接后的内容添加到 listModel
                listModel.addElement(displayText);
            }
        }

        // 对 listModel 进行排序，如果需要的话
        sortListModel(listModel);
    }

    private static void sortListModel(DefaultListModel<String> listModel) {
        // 定义自定义的 Comparator
        Comparator<String> comparator = (s1, s2) -> {
            // 优先级排序：RestController > FeignService > 其他
            if (s1.contains("RestController") && !s2.contains("RestController")) {
                return -1;
            } else if (!s1.contains("RestController") && s2.contains("RestController")) {
                return 1;
            }

            if (s1.contains("FeignService") && !s2.contains("FeignService")) {
                return 1;
            } else if (!s1.contains("FeignService") && s2.contains("FeignService")) {
                return -1;
            }

            // 如果两者都包含相同的内容，或者都不包含，按字母顺序排序
            return s1.compareTo(s2);
        };

        // 将 listModel 中的数据转换成 List 并进行排序
        List<String> list = Collections.list(listModel.elements());
        list.sort(comparator);

        // 清空原 ListModel 并重新添加排序后的数据
        listModel.clear();
        for (String element : list) {
            listModel.addElement(element);
        }
    }

    // 跳转到用户选择的方法
    private void navigateToSelectedMethodOld(Project project, String methodName) {
        ControllerUrlService controllerUrlService = project.getService(ControllerUrlService.class);
        List<PsiMethod> psiMethods = controllerUrlService.getMethodsForUrlFuzzy(methodName);

        if (!psiMethods.isEmpty()) {
            for (PsiMethod psiMethod : psiMethods) {
                // Using IntelliJ API to navigate to the method
                NavigationUtil.activateFileWithPsiElement(psiMethod);
            }
        }
    }

    // 跳转到用户选择的方法
    private void navigateToSelectedMethod(Project project, String methodSignature) {
        // 从完整的签名中提取类名和方法名
        String className = methodSignature.substring(0, methodSignature.lastIndexOf('.'));
        String methodName = methodSignature.substring(methodSignature.lastIndexOf('.') + 1, methodSignature.lastIndexOf('('));

        // 获取类对象并查找方法
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);

        // 遍历所有模块，确保方法来自正确的模块
        PsiClass psiClass;
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            // 获取模块的类搜索范围
            GlobalSearchScope moduleScope = GlobalSearchScope.moduleScope(module);
            psiClass = psiFacade.findClass(className, moduleScope);
            if (psiClass != null) {
                // break; // 找到第一个符合条件的类
                // 查找方法
                PsiMethod selectedMethod = findMethodInClass(psiClass, methodName);
                if (selectedMethod != null) {
                    // 使用NavigationUtil进行跳转
                    NavigationUtil.activateFileWithPsiElement(selectedMethod);
                    System.out.println("Found in Module: " + module.getName());
                } else {
                    System.out.println("Not found in Module: " + module.getName());
                }
            }
        }
    }

    // 查找方法
    private PsiMethod findMethodInClass(PsiClass psiClass, String methodName) {
        for (PsiMethod method : psiClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;  // 如果没有找到方法
    }

    static class MethodListCellRenderer extends JLabel implements ListCellRenderer<String> {
        @Override
        public Component getListCellRendererComponent(
                JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {

            String[] parts = value.split(" - ");  // 假设显示内容的格式是 "methodSignature - methodUrl"
            String methodSignature = parts[0];
            String methodUrl = parts.length > 1 ? parts[1] : "";

            // 使用 HTML 格式化文本内容
            String displayText = "<html><b style='color:#4CAF50;'>" + methodUrl + "</b><br><i style='color:#A9A9A9;'>"
                    + methodSignature + "</i></html>";
            setText(displayText);

            // 设置选中时的背景色和前景色
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            // 设置每个列表项的底部间距
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));  // 添加间距 (top, left, bottom, right)

            setOpaque(true);
            return this;
        }
    }

    private void setUrlFieldText(String text) {
        urlField.setText(text);
    }
}
