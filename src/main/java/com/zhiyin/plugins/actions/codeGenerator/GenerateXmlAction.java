package com.zhiyin.plugins.actions.codeGenerator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsNotifier;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vcs.changes.LocalChangeListImpl;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.zhiyin.plugins.service.FreeMarkerConfiguration;
import com.zhiyin.plugins.ui.codeGenerator.DataModelGenerator;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GenerateXmlAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            Messages.showErrorDialog("Project is not available", "Error");
            return;
        }

        Module module = e.getData(PlatformDataKeys.MODULE);
        if (module == null) {
            Messages.showErrorDialog("Module is not available", "Error");
            return;
        }

        // Define the data model for the template
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("name", "exampleName");
        dataModel.put("value", "exampleValue" + new Date());

        Map<String, Object> dmMoc = new HashMap<>();
        dmMoc.put("mocName", "Student");
        dmMoc.put("tableName", "biz_student");
        List<Map<String, Object>> fields = new ArrayList<>();

        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "id");
        field1.put("type", "int");

        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "factoryid");
        field2.put("type", "int");

        Map<String, Object> field3 = new HashMap<>();
        field3.put("name", "code");
        field3.put("type", "string");
        field3.put("length", 32);

        Map<String, Object> field4 = new HashMap<>();
        field4.put("name", "name");
        field4.put("type", "string");
        field4.put("length", 128);

        Map<String, Object> field5 = new HashMap<>();
        field5.put("name", "maintainer");
        field5.put("type", "string");
        field5.put("length", 128);

        Map<String, Object> field6 = new HashMap<>();
        field6.put("name", "maintaintime");
        field6.put("type", "datetime");

        Map<String, Object> field7 = new HashMap<>();
        field7.put("name", "factoryname");
        field7.put("type", "string");
        field7.put("length", 128);

        Map<String, Object> field8 = new HashMap<>();
        field8.put("name", "abbr");
        field8.put("type", "string");
        field8.put("length", 128);

        Map<String, Object> field9 = new HashMap<>(field8);
        field9.put("name", "abbr2");

        fields.add(field1);
        fields.add(field2);
        fields.add(field7);
        fields.add(field3);
        fields.add(field4);
        fields.add(field8);
        fields.add(field9);
        fields.add(field5);
        fields.add(field6);

        dmMoc.put("fields", fields);

        Map<String, Object> dmLayout = new HashMap<>();
        dmLayout.put("layoutName", "Student");
        Map<String, Object> dataGrid1 = new HashMap<>();
        dataGrid1.put("dataGridName", "Student");
        dataGrid1.put("ckDummyColumn", "true");
        List<Map<String, Object>> columns = new ArrayList<>(fields);
        dataGrid1.put("columns", columns);
        List<Map<String, Object>> queryFields = new ArrayList<>(fields);
        queryFields.forEach(field -> field.put("ref", field.get("name")));
        dataGrid1.put("queryFields", queryFields);
        List<Map<String, Object>> dialogFields = new ArrayList<>(fields);

        int colEachRow = 2;
        int currentRow = 2;
        int i = 0;
        List<String> ignoreFields = List.of("id", "factoryid", "factoryname", "maintainer", "maintaintime");
        List<String> removeFields = List.of("factoryname", "maintainer", "maintaintime");
        removeFields.forEach(field -> {
            dialogFields.removeIf(f -> String.valueOf(f.get("name")).equals(field));
            queryFields.removeIf(f -> String.valueOf(f.get("name")).equals(field));
        });
        for (Map<String, Object> field : dialogFields) {
            if (!ignoreFields.contains(String.valueOf(field.get("name")))){
                i++;
                int curCol = i % colEachRow;
                if (curCol != 0) {
                    field.put("layout", currentRow + ":" + curCol);
                } else {
                    field.put("layout", currentRow + ":" + colEachRow);
                    currentRow++;
                }
            }
            field.put("easyuiClass", "easyui-textbox");
            field.put("ref", field.get("name"));
        }
        dataGrid1.put("dialogFields", dialogFields);
        dmLayout.put("dataGrids", List.of(dataGrid1));

        // Generate the XML file
        try {
//            generateXmlFile(project, module, dataModel, "test.ftl", "src/main/webapp/WEB-INF/etc/business/layout/Basic", "generated.xml");
//            generateXmlFile(project, module, dmMoc, "moc.ftl", "src/main/webapp/WEB-INF/etc/business/model/Basic", dmMoc.get("mocName") + ".xml");
            generateXmlFile(project, module, dmLayout, "layout.ftl", "src/main/webapp/WEB-INF/etc/business/layout/Basic", dmLayout.get("layoutName") + ".xml");
        } catch (Exception ex) {
            Messages.showErrorDialog("无法生成 XML 文件： " + ex.getMessage(), "操作失败");
        }
    }

    private void generateXmlFile(Project project, Module module, Map<String, Object> dataModel, String templateName, String outputSourceContentRootPath, String outputFileName) throws IOException, TemplateException {
        Configuration cfg = FreeMarkerConfiguration.getConfiguration();
        Template template = cfg.getTemplate(templateName);

        // Define the path relative to the module's content root
        VirtualFile contentRoot = findModuleContentRoot(module);
        if (contentRoot == null) {
            throw new IOException("Module content root not found");
        }

        // Build the output file path
        VirtualFile outputDir = contentRoot.findFileByRelativePath(outputSourceContentRootPath);
        if (outputDir == null) {
//            outputDir = contentRoot.createChildDirectory(this, "src/main/webapp/WEB-INF/etc/business/layout/Basic");
            Messages.showErrorDialog("未找到目录", "操作失败");
            return;
        }

        // Create the output file
        final VirtualFile outputFile = outputDir.findChild(outputFileName);

        if (outputFile != null) {
            Messages.showErrorDialog("文件已存在", "操作失败");
            return;
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                VirtualFile virtualFile = outputDir.createChildData(this, outputFileName);

                // Write data to the file
                try (OutputStream outputStream = virtualFile.getOutputStream(this)) {
                    try {
                        template.process(dataModel, new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                    } catch (TemplateException | IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        VcsDirtyScopeManager.getInstance(project).fileDirty(virtualFile);
                        ChangeListManager.getInstance(project).scheduleUpdate();
                        // 刷新 VFS
                        virtualFile.refresh(true, true);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                ApplicationManager.getApplication().invokeLater(() -> {

                    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(VfsUtilCore.virtualToIoFile(virtualFile));
                    VcsNotifier.getInstance(project).notifySuccess("CodeGenerator", "代码生成成功", "SVN 检测到新文件： " + virtualFile.getName());
                    LocalFileSystem.getInstance().refresh(true);
                });

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private VirtualFile findModuleContentRoot(Module module) {
        VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
        return contentRoots.length > 0 ? contentRoots[0] : null;
    }
}
