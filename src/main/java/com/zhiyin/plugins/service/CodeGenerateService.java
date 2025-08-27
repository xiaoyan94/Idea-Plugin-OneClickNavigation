package com.zhiyin.plugins.service;

import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsNotifier;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.ProjectTypeChecker;
import com.zhiyin.plugins.utils.StringUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service(Service.Level.PROJECT)
public final class CodeGenerateService {
    final Project project;

    public CodeGenerateService(Project project) {
        this.project = project;
    }

    public void generateModelByFields(Module module, String folder, String modelName, String tableName, List<Map<String, Object>> fields) {
        if (project == null) {
            Messages.showErrorDialog("Project is not available", "Error");
            return;
        }

        if (module == null) {
            Messages.showErrorDialog("Module is not available", "Error");
            return;
        }

        if (folder == null || folder.isEmpty()) {
            folder = "Basic";
        }

        // Define the data model for the template
        Map<String, Object> dmMoc = new HashMap<>();
        dmMoc.put("mocName", modelName);
        dmMoc.put("tableName", tableName);
        dmMoc.put("fields", fields);

        Map<String, Object> dmLayout = new HashMap<>();
        dmLayout.put("layoutName", modelName);
        Map<String, Object> dataGrid1 = new HashMap<>();
        dataGrid1.put("dataGridName", modelName);
        dataGrid1.put("ckDummyColumn", "true");
        List<Map<String, Object>> columns = new ArrayList<>(fields);
        columns.forEach(field -> {
            field.put("chs", field.get("comment"));
            List<Property> properties = MyPropertiesUtil.findModuleDataGridI18nPropertiesByValue(project, module, field.get("comment").toString());
            if (!properties.isEmpty()) {
                field.put("i18nKey", properties.get(0).getKey());
                field.put("chs", StringUtil.unicodeToString(properties.get(0).getValue()));
                if (properties.size() > 2) {
                    field.put("cht", StringUtil.unicodeToString(properties.get(1).getValue()));
                    field.put("eng", properties.get(2).getValue());
                }
            }
        });
        dataGrid1.put("columns", columns);
        List<Map<String, Object>> queryFields = new ArrayList<>(fields);
        queryFields.forEach(field -> field.put("ref", field.get("name")));
        queryFields.forEach(field -> {
            if ((field.get("isQueryField") instanceof Boolean)) {
                field.put("isQueryField", String.valueOf(field.get("isQueryField")));
            }
        });
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
        dialogFields.forEach(field -> {
            if ((field.get("isRequired") instanceof Boolean)) {
                field.put("required", String.valueOf(field.get("isRequired")));
            }
            field.put("editable", "true");
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
        String moduleName = MyPropertiesUtil.getSimpleModuleName(module);
        dmLayout.put("moduleName", StringUtil.capitalize(moduleName));
        dmLayout.put("dataGrids", List.of(dataGrid1));

        // Generate the XML file
        try {
            // TODO 生成 java 文件
            generateXmlFile(project, module, dmMoc, "moc.ftl", "src/main/webapp/WEB-INF/etc/business/model/" + folder, dmMoc.get("mocName") + ".xml");
            generateXmlFile(project, module, dmLayout, "layout.ftl", "src/main/webapp/WEB-INF/etc/business/layout/" + folder, dmLayout.get("layoutName") + ".xml");
        } catch (Exception ex) {
            Messages.showErrorDialog("无法生成件，异常： " + ex.getMessage(), "操作失败");
        }
    }

    public void generateMocFile(Module module, String folder, String modelName, String tableName, List<Map<String, Object>> fields) {
        if (project == null) {
            Messages.showErrorDialog("Project is not available", "Error");
            return;
        }

        if (module == null) {
            Messages.showErrorDialog("Module is not available", "Error");
            return;
        }

        if (folder == null || folder.isEmpty() || "web".equalsIgnoreCase(folder)) {
            String moduleName = MyPropertiesUtil.getSimpleModuleName(module);
            if (ProjectTypeChecker.isTraditionalJavaWebProject(project, module)) {
                if ("web".equals(moduleName)) {
                    modelName = "Basic";
                }
            }
            folder = StringUtil.capitalize(moduleName);
        }

        // Define the data model for the template
        Map<String, Object> dmMoc = new HashMap<>();
        dmMoc.put("mocName", modelName);
        dmMoc.put("tableName", tableName);
        dmMoc.put("fields", fields);

        // Generate the XML file
        try {
            String outputSourceContentRootPath = "src/main/webapp/WEB-INF/etc/business/model/" + folder;
            if (ProjectTypeChecker.isTraditionalJavaWebProject(project, module)){
                outputSourceContentRootPath = "src/main/resources/META-INF/resources/WEB-INF/etc/business/model/" + folder;
            }
            generateXmlFile(project, module, dmMoc, "moc.ftl", outputSourceContentRootPath, dmMoc.get("mocName") + ".xml");
        } catch (Exception ex) {
            Messages.showErrorDialog("无法生成件，异常： " + ex.getMessage(), "操作失败");
        }
    }

    public void generateLayoutFile(Module module, String folder, String modelName, String tableName, List<Map<String, Object>> fields) {
        if (project == null) {
            Messages.showErrorDialog("Project is not available", "Error");
            return;
        }

        if (module == null) {
            Messages.showErrorDialog("Module is not available", "Error");
            return;
        }

        if (folder == null || folder.isEmpty() || "web".equalsIgnoreCase(folder)) {
            String moduleName = MyPropertiesUtil.getSimpleModuleName(module);
            if (ProjectTypeChecker.isTraditionalJavaWebProject(project, module)) {
                if ("web".equals(moduleName)) {
                    modelName = "Basic";
                }
            }
            folder = StringUtil.capitalize(moduleName);
        }

        Map<String, Object> dmLayout = new HashMap<>();
        dmLayout.put("layoutName", modelName);
        Map<String, Object> dataGrid1 = new HashMap<>();
        dataGrid1.put("dataGridName", modelName);
        dataGrid1.put("ckDummyColumn", "true");
        List<Map<String, Object>> columns = new ArrayList<>(fields);
        columns.forEach(field -> {
            field.put("chs", field.get("comment"));
            List<Property> properties = MyPropertiesUtil.findModuleDataGridI18nPropertiesByValue(project, module, field.get("comment").toString());
            if (!properties.isEmpty()) {
                field.put("i18nKey", properties.get(0).getKey());
                field.put("chs", properties.get(0).getValue());
                if (properties.size() > 2) {
                    field.put("cht", properties.get(1).getValue());
                    field.put("eng", properties.get(2).getValue());
                }
            }
        });
        dataGrid1.put("columns", columns);
        List<Map<String, Object>> queryFields = new ArrayList<>(fields);
        queryFields.forEach(field -> field.put("ref", field.get("name")));
        queryFields.forEach(field -> {
            if ((field.get("isQueryField") instanceof Boolean)) {
                field.put("isQueryField", String.valueOf(field.get("isQueryField")));
            }
        });
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
        dialogFields.forEach(field -> {
            if ((field.get("isRequired") instanceof Boolean)) {
                field.put("required", String.valueOf(field.get("isRequired")));
            }
            field.put("editable", "true");
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
        String moduleName = MyPropertiesUtil.getSimpleModuleName(module);
        dmLayout.put("moduleName", StringUtil.capitalize(moduleName));
        dmLayout.put("dataGrids", List.of(dataGrid1));

        // Generate the XML file
        try {
            String outputSourceContentRootPath = "src/main/webapp/WEB-INF/etc/business/layout/" + folder;
            if (ProjectTypeChecker.isTraditionalJavaWebProject(project, module)){
                outputSourceContentRootPath = "src/main/resources/META-INF/resources/WEB-INF/etc/business/layout/" + folder;
            }
            generateXmlFile(project, module, dmLayout, "layout.ftl", outputSourceContentRootPath, dmLayout.get("layoutName") + ".xml");
        } catch (Exception ex) {
            Messages.showErrorDialog("无法生成件，异常： " + ex.getMessage(), "操作失败");
        }
    }

    public void generateBaseQueryTypeFile(Module module, String folder, String modelName, String fileName, String sql, List<Map<String, Object>> fields, Map<String, Object> paramsMap) {
        if (project == null) {
            Messages.showErrorDialog("Project is not available", "Error");
            return;
        }

        if (module == null) {
            Messages.showErrorDialog("Module is not available", "Error");
            return;
        }

        if (folder == null || folder.isEmpty() || "web".equalsIgnoreCase(folder)) {
            String moduleName = MyPropertiesUtil.getSimpleModuleName(module);
            if (ProjectTypeChecker.isTraditionalJavaWebProject(project, module)) {
                if ("web".equals(moduleName)) {
                    moduleName = "Basic";
                }
            }
            folder = StringUtil.capitalize(moduleName);
        }

        Map<String, Object> dmLayout = new HashMap<>();
        dmLayout.put("layoutName", modelName);
        Map<String, Object> dataGrid1 = new HashMap<>();
        dataGrid1.put("dataGridName", modelName);
        dataGrid1.put("ObjectName", modelName);
        // dataGrid1.put("objectName", modelName.replaceFirst(modelName.substring(0, 1), modelName.substring(0, 1).toLowerCase()));
        dataGrid1.put("objectName", StringUtil.firstLetterToLowerCase(modelName));
        dataGrid1.put("fileName", fileName);
        dataGrid1.put("tableName", paramsMap.get("tableName"));
        // dataGrid1.put("sql", sql.replace(" from ", " \nfrom ").replace(" where", " \nwhere "));
        dataGrid1.put("sql", formatSql(sql));
        dataGrid1.put("ckDummyColumn", "true");
        List<Map<String, Object>> columns = new ArrayList<>(fields);
        columns.forEach(field -> {
            field.put("chs", field.get("comment"));
            List<Property> properties = MyPropertiesUtil.findModuleDataGridI18nPropertiesByValue(project, module, field.get("comment").toString());
            if (!properties.isEmpty()) {
                field.put("i18nKey", properties.get(0).getKey());
                field.put("chs", properties.get(0).getValue());
                if (properties.size() > 2) {
                    field.put("cht", properties.get(1).getValue());
                    field.put("eng", properties.get(2).getValue());
                }
            }
        });
        dataGrid1.put("columns", columns);
        List<Map<String, Object>> queryFields = new ArrayList<>(fields);
        queryFields.forEach(field -> field.put("ref", field.get("name")));
        queryFields.forEach(field -> {
            if ((field.get("isQueryField") instanceof Boolean)) {
                field.put("isQueryField", String.valueOf(field.get("isQueryField")));
                // field.put("isQueryField", "true");
            }
        });
        dataGrid1.put("queryFields", queryFields);

        /*List<Map<String, Object>> dialogFields = new ArrayList<>(fields);
        int colEachRow = 2;
        int currentRow = 2;
        int i = 0;
        List<String> ignoreFields = List.of("id", "factoryid", "factoryname", "maintainer", "maintaintime");
        List<String> removeFields = List.of("factoryname", "maintainer", "maintaintime");
        removeFields.forEach(field -> {
            dialogFields.removeIf(f -> String.valueOf(f.get("name")).equals(field));
            queryFields.removeIf(f -> String.valueOf(f.get("name")).equals(field));
        });
        dialogFields.forEach(field -> {
            if ((field.get("isRequired") instanceof Boolean)) {
                field.put("required", String.valueOf(field.get("isRequired")));
            }
            field.put("editable", "true");
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
        dataGrid1.put("dialogFields", dialogFields);*/
        String moduleName = MyPropertiesUtil.getSimpleModuleName(module);
        dmLayout.put("moduleName", StringUtil.capitalize(moduleName));
        dmLayout.put("dataGrids", List.of(dataGrid1));

        // Generate the XML file
        try {
            String outputSourceContentRootPath = "src/main/webapp/WEB-INF/etc/business/layout/" + folder;
            String outputSourceContentHtmlPath = "src/main/webapp/WEB-INF/view/MesRoot/" + folder;
            String folderLowerCase = folder.toLowerCase();
            String outputSourceContentControllerPath = "src/main/java/com/zhiyin/controller/mes/" + folderLowerCase + "/";
            if ("basic".equals(folderLowerCase)) {
                outputSourceContentControllerPath = "src/main/java/com/zhiyin/controller/" + folderLowerCase + "/";
            }
            String outputSourceContentServicePath = "src/main/java/com/zhiyin/service/" + folderLowerCase + "/";
            String outputSourceContentDaoPath = "src/main/java/com/zhiyin/dao/" + folderLowerCase + "/";
            String outputSourceContentMapperPath = "src/main/java/com/zhiyin/maps/" + folderLowerCase + "/";

            Object outputFileName = dmLayout.get("layoutName");
            if (ProjectTypeChecker.isTraditionalJavaWebProject(project, module)){
                // TODO isTraditionalJavaWebProject方法优化
                // 先尝试生成一遍SpringBoot项目目录架构的
                if ((Boolean) paramsMap.get("layoutCheckBox")) {
                    generateXmlFile(project, module, dmLayout, "BaseQueryTypeLayout.ftl", outputSourceContentRootPath, outputFileName + ".xml");
                    outputSourceContentRootPath = "src/main/resources/META-INF/resources/WEB-INF/etc/business/layout/" + folder;
                }
            }
            if ((Boolean) paramsMap.get("layoutCheckBox")) {
                generateXmlFile(project, module, dmLayout, "BaseQueryTypeLayout.ftl", outputSourceContentRootPath, outputFileName + ".xml");
            }
            if ((Boolean) paramsMap.get("htmlCheckBox")) {
                generateXmlFile(project, module, dmLayout, "BaseQueryTypeHtml.ftl", outputSourceContentHtmlPath, outputFileName + ".html");
            }
            if ((Boolean) paramsMap.get("controllerCheckBox")) {
                generateXmlFile(project, module, dmLayout, "BaseQueryTypeController.ftl", outputSourceContentControllerPath, outputFileName + "Controller.java");
            }
            if ((Boolean) paramsMap.get("serviceCheckBox")) {
                generateXmlFile(project, module, dmLayout, "BaseQueryTypeService.ftl", outputSourceContentServicePath, outputFileName + "Service.java");
            }
            if ((Boolean) paramsMap.get("daoCheckBox")) {
                generateXmlFile(project, module, dmLayout, "BaseQueryTypeDao.ftl", outputSourceContentDaoPath, "I" + outputFileName + "Dao.java");
            }
            if ((Boolean) paramsMap.get("myBatisMapperCheckBox")) {
                generateXmlFile(project, module, dmLayout, "BaseQueryTypeMapper.ftl", outputSourceContentMapperPath, outputFileName + "Mapper.xml");
            }
        } catch (Exception ex) {
            Messages.showErrorDialog("无法生成件，异常： " + ex.getMessage(), "操作失败");
        }
    }

    private void generateXmlFile(Project project, Module module, Map<String, Object> dataModel, String templateName, String outputSourceContentRootPath, String outputFileName) throws IOException {
        Configuration cfg = FreeMarkerConfiguration.getConfiguration();
        Template template = cfg.getTemplate(templateName);

        // Define the path relative to the module's content root
        VirtualFile contentRoot = findModuleContentRoot(module);
        if (contentRoot == null) {
            throw new IOException("Module content root not found");
        }

        // Build the output file path
        VirtualFile outputDirVariable = contentRoot.findFileByRelativePath(outputSourceContentRootPath);
        if (outputDirVariable == null) {
//            outputDir = contentRoot.createChildDirectory(this, "src/main/webapp/WEB-INF/etc/business/layout/Basic");
            Messages.showErrorDialog("未找到目录。已放到根目录：" + contentRoot.getPresentableUrl(), "操作失败");
            outputDirVariable = contentRoot;
//            return;
        }


        // Create the output file
        final VirtualFile outputFile = outputDirVariable.findChild(outputFileName);

        if (outputFile != null) {
            Messages.showErrorDialog("文件已存在：" + outputFile.getPresentableUrl() + "。已放到根目录：" + contentRoot.getPresentableUrl(), "操作失败");
            outputDirVariable = contentRoot;
//            return;
        }

        VirtualFile outputDir = outputDirVariable;
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

    private String formatSql(String sql) {
        // 1. 检查输入是否为空，如果为空则直接返回空字符串
        if (StringUtils.isBlank(sql)) {
            return "";
        }

        String formattedSql = sql.trim();

        // 2. 将 SELECT, FROM, WHERE, JOIN 等关键字前面加上换行符
        // `(?i)`: 开启不区分大小写模式
        // `\\b` : 匹配单词边界，确保只匹配完整的关键字，例如不匹配 `SELECTIVE` 中的 `SELECT`
        // `\\s*` : 匹配关键字后可能存在的任意数量的空格
        String[] keywords = {"SELECT", "FROM", "WHERE", "GROUP BY", "ORDER BY", "HAVING", "LEFT JOIN", "INNER JOIN", "RIGHT JOIN", "ON"};
        for (String keyword : keywords) {
            formattedSql = formattedSql.replaceAll("(?i)\\b" + keyword + "\\b\\s*", "\n" + keyword + " ");
        }

        // 3. 将 WHERE 条件中的 AND 和 OR 放在新的一行并进行缩进
        // `$1` 是一个反向引用，它会保留匹配到的 `AND` 或 `OR`
        formattedSql = formattedSql.replaceAll("(?i)\\b(AND|OR)\\b\\s*", "\n  $1 ");

        // 4. 将 SELECT 语句中的逗号和函数参数外的逗号进行换行和缩进
        // `,\\s*`: 匹配逗号及后面的所有空格
        // `(?![^()]*\\))`: 这是一个负向先行断言，它确保我们匹配的逗号后面不跟着非括号字符和右括号。
        //                  这样可以防止匹配到函数参数内部的逗号，例如 `COUNT(col1, col2)`
        formattedSql = formattedSql.replaceAll(",\\s*(?![^()]*\\))", ",\n  ");

        // 5. 清理多余的换行符和空格，确保格式整洁
        formattedSql = formattedSql.replaceAll("\\s*\\n\\s*", "\n").trim(); // 将多余的换行符和空格合并
        formattedSql = formattedSql.replaceAll("\\s{2,}", " "); // 将多个空格替换为单个空格
        formattedSql = formattedSql.replaceAll("\n ", "\n"); // 清除换行符后的多余空格

        // 6. 针对特定关键字添加缩进，以提高可读性
        formattedSql = formattedSql.replace("SELECT \n", "SELECT\n  ");
        formattedSql = formattedSql.replace("FROM \n", "FROM\n");
        formattedSql = formattedSql.replace("WHERE \n", "WHERE\n  ");

        // 7. 每行前添加两个tab即8个空格
        // `(?m)^`: 这是一个多行模式匹配，表示匹配每行开头的位置。
        formattedSql = formattedSql.replaceAll("(?m)^", "\t\t");
        return formattedSql;
    }
}
