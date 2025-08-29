package com.zhiyin.plugins.ui.codeGenerator;

//import com.intellij.ui.table.JBTable;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.table.JBTable;
import com.zhiyin.plugins.notification.MyPluginMessages;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.service.CodeGenerateService;
import com.zhiyin.plugins.utils.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Service(Service.Level.PROJECT)
public class DataModelGenerator {
    private final JFrame frame;
    private final JTable table;
    private final DefaultTableModel tableModel;

    private final List<Map<String, Object>> fields = new ArrayList<>();
    private final Project project;
    private final Module module;
    private final String[] COLUMNS_NAME;
    private final ButtonGroup pageTypeButtonGroup;

    private String tableName;
    private String sql;
    private JCheckBox mocCheckBox;
    private JCheckBox layoutCheckBox;
    private JCheckBox htmlCheckBox;
    private JCheckBox controllerCheckBox;
    private JCheckBox serviceCheckBox;
    private JCheckBox daoCheckBox;
    private JCheckBox myBatisMapperCheckBox;
    private JRadioButton dataMaintenanceRadioButton;
    private JRadioButton dataQueryRadioButton;

    public DataModelGenerator(Project project, Module module) {
        this.project = project;
        this.module = module;
        frame = new JFrame("Module: " + module.getName() + " - " + project.getName() + " - DataModelGenerator");
        frame.setIconImage(MyIcons.appIcon.getImage());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1200, 600);
        frame.setLayout(new BorderLayout());

        JButton queryButton = new JButton("从数据库读取表字段");
        queryButton.addActionListener(e -> fetchFieldsFromDatabase());
//        queryButton.setEnabled(false);

        JButton parseCreateSQLButton = new JButton("从建表DDL解析字段");
        parseCreateSQLButton.addActionListener(e -> fetchFieldsFromTableSQL());
//        parseCreateSQLButton.setEnabled(false);

        // TODO
        JButton parseSelectSQLButton = new JButton("从查询SQL解析字段");
        parseSelectSQLButton.addActionListener(e -> fetchFieldsFromQuerySQL());

        // TODO
        COLUMNS_NAME = new String[]{"name", "type", "length", "comment", "isColumnField", "isQueryField", "isDialogField", "isRequired", "isEditHidden", "easyuiClass"};
        tableModel = new DefaultTableModel(COLUMNS_NAME, 0){
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column);
                // Sync the data to the original List<Map>
                String columnName = getColumnName(column);
                fields.get(row).put(columnName, aValue);
            }

            @Override
            public String getColumnName(int column) {
                return super.getColumnName(column);
            }
        };
        table = new JBTable(tableModel);
        // 设置自定义的渲染器
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        table.setRowSelectionAllowed(false);
        table.setCellSelectionEnabled(true);

        // Add mouse listener for double-click
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // Double click
                    int row = table.rowAtPoint(e.getPoint());
                    int column = table.columnAtPoint(e.getPoint());

                    if (row >= 0 && column >= 0) {
                        String columnName = table.getColumnName(column);

                        // Check if the column name is 'isQueryField'
                        if (columnName.startsWith("is")) {
                            Object currentValue = table.getValueAt(row, column);
                            if (currentValue instanceof Boolean) {
                                boolean newValue = !(Boolean) currentValue;
                                table.setValueAt(newValue, row, column);
                            } else if (currentValue instanceof String) {
                                boolean newValue = !"true".equalsIgnoreCase((String) currentValue);
                                table.setValueAt(newValue, row, column);
                            } else {
                                table.setValueAt(false, row, column);
                            }
                        } else {
                            // Trigger editing mode manually
                            table.editCellAt(row, column);
                        }
                    }
                }
            }
        });

        JButton addButton = new JButton("添加字段");
        addButton.addActionListener(e -> showEditFieldDialog(null, -1));

        JButton editButton = new JButton("编辑字段");
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                Map<String, Object> field = fields.get(selectedRow);
                showEditFieldDialog(field, selectedRow);
            }
        });

        JButton deleteButton = new JButton("删除字段");
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                tableModel.removeRow(selectedRow);
                fields.remove(selectedRow);
            }
            updateTableModel();
        });


        JPanel buttonPanel = new JPanel();
        buttonPanel.add(queryButton);
        buttonPanel.add(parseCreateSQLButton);
        buttonPanel.add(parseSelectSQLButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
//        buttonPanel.add(generateButton);

        JPanel generateTypeSelectPanel = new JPanel();
        generateTypeSelectPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        // 添加多选框，可选择 Moc、Layout、Service、Controller、Service、Dao、MyBatisMapper

        // 创建一组单选框，表示“页面功能类型”，两个的单选框可选择 数据维护、记录查询
        dataMaintenanceRadioButton = new JRadioButton("数据维护");
        dataQueryRadioButton = new JRadioButton("记录查询");
        pageTypeButtonGroup = new ButtonGroup();
        pageTypeButtonGroup.add(dataMaintenanceRadioButton);
        pageTypeButtonGroup.add(dataQueryRadioButton);
        dataMaintenanceRadioButton.setEnabled(false);
        dataMaintenanceRadioButton.setSelected(false);
        dataQueryRadioButton.setSelected(true);
        JPanel pageTypePanel = new JPanel();
        pageTypePanel.setLayout(new BoxLayout(pageTypePanel, BoxLayout.Y_AXIS));
        pageTypePanel.add(dataMaintenanceRadioButton);
        pageTypePanel.add(dataQueryRadioButton);

        JBLabel label = new JBLabel("页面功能类型：");
        generateTypeSelectPanel.add(label);
        generateTypeSelectPanel.add(pageTypePanel);

        JPanel checkBoxPanel = createCheckBoxPanel();
        JButton generateButton = new JButton("一键生成");
        generateButton.addActionListener(e -> generateDataModel());
        generateTypeSelectPanel.add(checkBoxPanel);
        generateTypeSelectPanel.add(generateButton);

        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.add(generateTypeSelectPanel, BorderLayout.SOUTH);
        frame.setMinimumSize(new Dimension(800, 600));
    }

    private @NotNull JPanel createCheckBoxPanel() {
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.X_AXIS));
        JLabel checkBoxLabel = new JLabel("文件类型：");
        checkBoxPanel.add(checkBoxLabel);
        mocCheckBox = new JCheckBox("Moc", true);
        layoutCheckBox = new JCheckBox("Layout", true);
        htmlCheckBox = new JCheckBox("Html", true);
        controllerCheckBox = new JCheckBox("Controller", true);
        serviceCheckBox = new JCheckBox("Service", true);
        daoCheckBox = new JCheckBox("Dao", true);
        myBatisMapperCheckBox = new JCheckBox("MyBatisMapper", true);

        checkBoxPanel.add(mocCheckBox);
        checkBoxPanel.add(layoutCheckBox);
        checkBoxPanel.add(htmlCheckBox);
        checkBoxPanel.add(controllerCheckBox);
        checkBoxPanel.add(serviceCheckBox);
        checkBoxPanel.add(daoCheckBox);
        checkBoxPanel.add(myBatisMapperCheckBox);
        return checkBoxPanel;
    }

    private void fetchFieldsFromDatabase() {
        // 实现查询数据库字段的逻辑
        ApplicationManager.getApplication().invokeLater(() -> {
            JFrame frame = new JFrame(); // Dummy frame to center dialog
//            @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();

            List<Map<String, String>> connections = new ArrayList<>();
            DatabaseConnectionFinder connectionFinder = this.project.getService(DatabaseConnectionFinder.class);
            if (connectionFinder != null) {
                connections.addAll(connectionFinder.findDatabaseConnections(project));
            }
            SelectDatabaseConnectionDialog dialog = new SelectDatabaseConnectionDialog(frame, connections);
            dialog.setVisible(true);

            // Get the selected connection info after the dialog is closed
            Map<String, String> selectedConnectionInfo = dialog.getSelectedConnectionInfo();

            if (selectedConnectionInfo != null) {

                String tableName = Messages.showInputDialog("请输入表名", "操作提示", Messages.getQuestionIcon());
                this.tableName = tableName;

                String jdbcUrl = selectedConnectionInfo.get("url");
                String username = selectedConnectionInfo.get("username");
                String password = selectedConnectionInfo.get("password");

                // Call the method to get table metadata
                List<Map<String, Object>> tableMetadata = DatabaseMetadataUtil.getTableMetadata(jdbcUrl, username, password, tableName);

                // Update fields with new data
                fields.clear();
                fields.addAll(tableMetadata);

                StringBuilder sqlBuilder = new StringBuilder("select ");
                for (int i = 0; i < fields.size(); i++) {
                    sqlBuilder.append("a.")
                              .append(fields.get(i).get("name"))
                              .append(",");
                }
                sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
                sqlBuilder.append(" \nfrom ")
                          .append(tableName)
                          .append(" a");
                this.sql = sqlBuilder.toString();

                // Optionally, update your UI component with the new fields data
                updateTableModel();
            }
        });

//        updateTableModel();
    }

    private void fetchFieldsFromTableSQL() {
        ApplicationManager.getApplication().invokeLater(() -> {
            // IDEA 插件开发 获取弹窗输入的SQL语句
            String input = Messages.showInputDialog(
                    project,
                    "Enter your create table DDL sql:",
                    "从DDL提取字段信息",
                    Messages.getQuestionIcon()
            );
            // 从SQL语句中提取表信息
            if (input != null && !input.isEmpty()) {
                this.tableName = TableParser.extractTableName(input);
                List<Map<String, Object>> fieldsFromSQL = TableParser.parseCreateTable(input);
                fields.clear();
                fields.addAll(fieldsFromSQL);

                StringBuilder sqlBuilder = new StringBuilder("select ");
                for (int i = 0; i < fields.size(); i++) {
                    sqlBuilder.append("a.")
                              .append(fields.get(i).get("name"))
                              .append(",");
                }
                sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
                sqlBuilder.append(" \nfrom ")
                          .append(tableName)
                          .append(" a");
                this.sql = sqlBuilder.toString();

                updateTableModel();
            } else {
                MyPluginMessages.showError("操作失败", "请输入有效的 SQL 语句（暂只支持解析MySQL DDL）。", project);
            }
        });
    }

    private void fetchFieldsFromQuerySQL() {
        ApplicationManager.getApplication().invokeLater(() -> {
            // IDEA 插件开发 获取弹窗输入的SQL语句
            String input = Messages.showInputDialog(
                    project,
                    "Enter your create table DQL sql:",
                    "从DQL提取字段信息",
                    Messages.getQuestionIcon(),
                    "select a.code, a.name from sys_user a",
                    new InputValidatorEx() {
                        @Override
                        public @NlsContexts.DetailedDescription @Nullable String getErrorText(@NonNls String inputString) {
                            if (StringUtils.isBlank(inputString) || !inputString.toLowerCase().contains("select")) {
                                return "请输入有效的 SQL 语句";
                            }
                            return null;
                        }
                    }
            );
            // 从SQL语句中提取表信息
            if (input != null && !input.isEmpty()) {
                this.tableName = TableParser.extractTableNameFromDQL(input);
                this.sql = input;
                List<Map<String, Object>> fieldsFromSQL = TableParser.parseDQL(input);
                fields.clear();
                fields.addAll(fieldsFromSQL);
                updateTableModel();
            } else {
                MyPluginMessages.showError("操作失败", "请输入有效的 SQL 语句", project);
            }
        });
    }

    private void updateTableModel() {
        tableModel.setRowCount(0);
        for (Map<String, Object> field : fields) {
            if (field == null) {
                continue;
            }

            if (StringUtils.isBlank((String) field.get("name"))) {
                continue;
            }

            List<String> addQueryFields = List.of("code", "name", "factoryid", "workshopid", "factoryname");
            field.computeIfAbsent("isQueryField", (k) -> {
                if (field.get("name") != null){
                    String name = field.get("name").toString();
                    if (addQueryFields.contains(name)) {
                        return true;
                    }
                    if (name.endsWith("code") || name.endsWith("name")) {
                        return true;
                    }
                }
                return false;
            });
            field.putIfAbsent("isColumnField", true);
            List<String> notDialogFields = List.of("maintainer", "maintaintime", "creator", "createtime", "updater", "updatetime");
            field.computeIfAbsent("isDialogField", (k) -> {
                if (field.get("name") != null){
                    String name = field.get("name").toString();
                    if (notDialogFields.contains(name)) {
                        return false;
                    }
                    if (name.endsWith("flag")) {
                        return false;
                    }
                }
                return true;
            });
            field.computeIfAbsent("isEditHidden", (k) -> {
                if (field.get("name") != null){
                    String name = field.get("name").toString();
                    if (name.endsWith("id") || name.endsWith("flag")) {
                        return true;
                    }
                }
                return false;
            });
            field.computeIfAbsent("easyuiClass", (k) -> {
                if (field.get("type") != null && field.get("name") != null) {
                    String type = field.get("type").toString();
                    String name = field.get("name").toString();
                    if (type.equals("int") && name.endsWith("id")) {
                        return "";
                    } else if (type.equals("string")) {
                        return "easyui-textbox";
                    } else if (type.equals("date")) {
                        return "easyui-datebox";
                    } else if (type.equals("datetime")) {
                        return "easyui-datetimebox";
                    } else if (type.equals("number") || type.equals("int")) {
                        return "easyui-numberbox";
                    } else {
                        return "";
                    }
                }
                return "";
            });

            Object[] rowData = new Object[COLUMNS_NAME.length];
            // 使用 COLUMNS_NAME 数组来初始化 rowData
            for (int i = 0; i < COLUMNS_NAME.length; i++) {
                rowData[i] = field.get(COLUMNS_NAME[i]);
            }
            tableModel.addRow(rowData);
        }
    }

    private void showEditFieldDialog(Map<String, Object> field, int rowIndex) {
        Field fieldModel = Field.Companion.createDefault();
        if (field != null) {
            fieldModel.setName((String) field.get("name"));
            fieldModel.setType((String) field.get("type"));
            fieldModel.setLength(field.get("length") != null ? Integer.parseInt(field.get("length").toString()) : 0);
            fieldModel.setComment((String) field.get("comment"));
            fieldModel.setRequired(field.get("isRequired") != null && "true".equals(field.get("isRequired").toString()));
            fieldModel.setQueryField(field.get("isQueryField") != null && "true".equals(field.get("isQueryField").toString()));
            fieldModel.setDialogField(field.get("isDialogField") != null && "true".equals(field.get("isDialogField").toString()));
        }

        KotlinFieldEditorDialog.Companion.showDialog(fieldModel, updatedField -> {
            Map<String, Object> updatedFieldMap = Map.of(
                    "name", updatedField.getName(),
                    "type", updatedField.getType(),
                    "length", updatedField.getLength(),
                    "comment", updatedField.getComment(),
                    "isRequired", updatedField.isRequired(),
                    "isQueryField", updatedField.isQueryField(),
                    "isDialogField", updatedField.isDialogField()
            );
            updatedFieldMap = new HashMap<>(updatedFieldMap);
            if (rowIndex == -1) {
                fields.add(updatedFieldMap);
            } else {
                fields.set(rowIndex, updatedFieldMap);
            }
            updateTableModel();
            return null;
        });
    }

    private void generateDataModel() {
        // 生成dataModel的逻辑
        CodeGenerateService service = project.getService(CodeGenerateService.class);

        // 检查 fields name、type必须有值
        if (this.fields.stream().anyMatch(field -> StringUtils.isBlank((String) field.get("name")) || StringUtils.isBlank((String) field.get("type")))) {
            MyPluginMessages.showWarning("无法继续生成", "字段名或类型为空", project);
            return;
        }

        if (dataMaintenanceRadioButton.isSelected() && !this.fields.isEmpty() && StringUtils.isBlank(this.tableName)){
            this.tableName = Messages.showInputDialog(
                    project,
                    "请输入表名：",
                    "代码生成",
                    Messages.getQuestionIcon()
            );
        }

        if (StringUtils.isBlank(this.tableName) || this.module == null || this.fields.isEmpty()) {
            MyPluginMessages.showWarning("无法继续生成", "模型数据为空", project);
            return;
        }

        String moduleName = MyPropertiesUtil.getSimpleModuleName(this.module);
        String folder = StringUtil.capitalize(moduleName);
        String modelName = Messages.showInputDialog(
                project,
                "请输入GridName（不用以Grid结尾）：",
                "代码生成",
                Messages.getQuestionIcon(),
                "",
                new InputValidatorEx() {
                    @Override
                    public @NlsContexts.DetailedDescription @Nullable String getErrorText(@NonNls String inputString) {
                        if (inputString.contains(" ")) {
                            return "GridName不能包含空格";
                        }
                        if (inputString.length() > 64) {
                            return "GridName不能超过64个字符";
                        }
                        return StringUtils.isBlank(inputString) ? "GridName不能为空" : null;
                    }
                }
        );
        String fileName = Messages.showInputDialog(
                project,
                "请输入页面名称：",
                "代码生成",
                Messages.getQuestionIcon(),
                modelName,
                new InputValidatorEx() {
                    @Override
                    public @NlsContexts.DetailedDescription @Nullable String getErrorText(@NonNls String inputString) {
                        if (inputString.contains(" ")) {
                            return "不能包含空格";
                        }
                        if (inputString.length() > 64) {
                            return "不能超过64个字符";
                        }
                        return StringUtils.isBlank(inputString) ? "页面名称不能为空" : null;
                    }
                }
        );
//        service.generateModelByFields(this.module, folder, modelName, this.tableName, this.fields);
        /*if(mocCheckBox.isSelected()){
            service.generateMocFile(this.module, folder, modelName, this.tableName, this.fields);
        }
        if(layoutCheckBox.isSelected()){
            service.generateLayoutFile(this.module, folder, modelName, this.tableName, this.fields);
        }*/

        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("tableName", tableName);
        paramsMap.put("sql", sql);
        paramsMap.put("mocCheckBox", mocCheckBox.isSelected());
        paramsMap.put("layoutCheckBox", layoutCheckBox.isSelected());
        paramsMap.put("htmlCheckBox", htmlCheckBox.isSelected());
        paramsMap.put("controllerCheckBox", controllerCheckBox.isSelected());
        paramsMap.put("serviceCheckBox", serviceCheckBox.isSelected());
        paramsMap.put("daoCheckBox", daoCheckBox.isSelected());
        paramsMap.put("myBatisMapperCheckBox", myBatisMapperCheckBox.isSelected());
        paramsMap.put("dataMaintenanceRadioButton", dataMaintenanceRadioButton.isSelected());
        paramsMap.put("dataQueryRadioButton", dataQueryRadioButton.isSelected());

        if (dataQueryRadioButton.isSelected()) {
            service.generateBaseQueryTypeFile(this.module, folder, modelName, fileName, this.sql, this.fields, paramsMap);
        }

        if (mocCheckBox.isSelected()) {
            service.generateMocFile(this.module, folder, modelName, this.tableName, this.fields);
        }

        // 关闭窗口
//        frame.dispose();
    }

    public void show() {
//        frame.pack();
        // 窗口居中
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // 自定义 TableCellRenderer
    static class CustomTableCellRenderer extends DefaultTableCellRenderer {
        private static final JBColor EVEN_ROW_COLOR = new JBColor(Gray._245, Gray._40);
        private static final JBColor ODD_ROW_COLOR = new JBColor(Color.WHITE, Gray._50);
        private static final JBColor SELECTED_CELL_BACKGROUND = new JBColor(new Color(184, 207, 229), new Color(90, 150, 230));
        private static final JBColor SELECTED_CELL_FOREGROUND = new JBColor(Color.BLACK, Color.WHITE);
        private static final JBColor TRUE_TEXT_COLOR = new JBColor(new Color(0, 128, 0), new Color(0, 150, 0));
        private static final JBColor FALSE_TEXT_COLOR = new JBColor(Color.RED, new Color(255, 100, 100));
        private static final JBColor DEFAULT_TEXT_COLOR = new JBColor(Color.BLACK, Color.LIGHT_GRAY);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Set background and foreground colors
            if (isSelected) {
                cellComponent.setBackground(SELECTED_CELL_BACKGROUND);
                cellComponent.setForeground(SELECTED_CELL_FOREGROUND);
            } else {
                if (row % 2 == 0) {
                    cellComponent.setBackground(EVEN_ROW_COLOR);
                } else {
                    cellComponent.setBackground(ODD_ROW_COLOR);
                }
                // Determine foreground color based on cell value
                String cellValue = value != null ? value.toString() : "";
                if ("true".equals(cellValue)) {
                    cellComponent.setForeground(TRUE_TEXT_COLOR);
                } else if ("false".equals(cellValue)) {
                    cellComponent.setForeground(FALSE_TEXT_COLOR);
                } else {
                    cellComponent.setForeground(DEFAULT_TEXT_COLOR);
                }
            }

            // Set the font for better appearance
            cellComponent.setFont(cellComponent.getFont().deriveFont(Font.PLAIN));

            return cellComponent;
        }
    }

}
