package com.zhiyin.plugins.actions;

import com.intellij.ide.actions.RevealFileAction;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.zhiyin.plugins.notification.MyPluginMessages;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.StringUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenerateExcelHeaderAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null || file == null || !file.getName().endsWith(".xml")) {
            return;
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        XmlFile xmlFile;
        if (psiFile instanceof XmlFile) {
            xmlFile = (XmlFile) psiFile;
        } else {
            return;
        }

        XmlTag rootTag = xmlFile.getRootTag();
        if (rootTag == null) return;

        XmlTag tableTag = Arrays.stream(rootTag.getSubTags())
                .filter(tag -> "table".equals(tag.getName()))
                .findFirst().orElse(null);

        if (tableTag == null) {
            MyPluginMessages.showError("未找到 table 标签", "请检查 XML 文件是否正确", project);
            return;
        }

        // 添加代码补全建议项
        boolean needTransUnicode = !MyPropertiesUtil.isNative2AsciiForPropertiesFiles();

        List<ColumnMeta> columns = new ArrayList<>();
        for (XmlTag columnTag : tableTag.findSubTags("column")) {
            String colStr = columnTag.getAttributeValue("col");
            if (colStr == null) continue;

            try {
                int col = Integer.parseInt(colStr);
                String name = columnTag.getAttributeValue("name");
                String desc = columnTag.getAttributeValue("description");
                String i18n = columnTag.getAttributeValue("i18n");
                List<Property> properties = MyPropertiesUtil.findModuleI18nProperties(project, e.getRequiredData(PlatformDataKeys.MODULE), i18n);
                String header = desc != null ? desc : name;
                String headerEn = header;
                String headerCht = header;
                String headerVn = header;
                if (!properties.isEmpty()) {
                    if (needTransUnicode) {
                        if (properties.size() > 3) {
                            headerVn = StringUtil.unicodeToString(properties.get(3).getValue());
                        }
                        if (properties.size() > 2) {
                            headerEn = StringUtil.unicodeToString(properties.get(2).getValue());
                        }
                        if (properties.size() > 1) {
                            headerCht = StringUtil.unicodeToString(properties.get(1).getValue());
                        }
                        header = StringUtil.unicodeToString(properties.get(0).getValue());
                    } else {
                        if (properties.size() > 3) {
                            headerVn = properties.get(3).getValue();
                        }
                        if (properties.size() > 2) {
                            headerEn = properties.get(2).getValue();
                        }
                        if (properties.size() > 1) {
                            headerCht = properties.get(1).getValue();
                        }
                        header = properties.get(0).getValue();
                    }
                }

                String requiredAttr = columnTag.getAttributeValue("required");
                boolean required = "true".equalsIgnoreCase(requiredAttr);

                if (header != null) {
                    columns.add(new ColumnMeta(col, header, required, headerEn, headerCht, headerVn));
                }
            } catch (NumberFormatException ex) {
                MyPluginMessages.showError("Col转数字异常", ex.getMessage(), project);
                // ex.printStackTrace();
            }
        }

        columns.sort(Comparator.comparingInt(ColumnMeta::getCol));
        List<String> headers = columns.stream().map(ColumnMeta::getHeader).collect(Collectors.toList());

        Map<String, Function<ColumnMeta, String>> langMap = new LinkedHashMap<>();
        langMap.put("zh_CN", ColumnMeta::getHeader);
        langMap.put("zh_TW", ColumnMeta::getHeaderCht);
        langMap.put("en_US", ColumnMeta::getHeaderEn);
        langMap.put("vi_VN", ColumnMeta::getHeaderVn);

        String simpleModuleName = MyPropertiesUtil.getSimpleModuleName(e.getRequiredData(PlatformDataKeys.MODULE));
        String basePath = Messages.showInputDialog("[Optional] 请输入输出 Excel 路径前缀（以文件夹名称结尾）：",
                "导出 Excel", Messages.getQuestionIcon(),
                "D:\\\\OneClickNavigation\\\\WorkDirTemp\\\\" + StringUtil.capitalize(simpleModuleName), null);

        if (basePath == null) return;

        String fileNameWithoutExtension = file.getNameWithoutExtension();

        if (fileNameWithoutExtension.endsWith("Mapper")) {
            // 移除 Mapper
            fileNameWithoutExtension = fileNameWithoutExtension.substring(0, fileNameWithoutExtension.length() - "Mapper".length());
        }

        if (fileNameWithoutExtension.startsWith("Import")) {
            // 移除 Import
            fileNameWithoutExtension = fileNameWithoutExtension.substring("Import".length());
        }

        // 如果以 Imp 开头且 第四个字符为大写字母，则移除开头的 Imp
        if (fileNameWithoutExtension.startsWith("Imp") && Character.isUpperCase(fileNameWithoutExtension.charAt(3))) {
            fileNameWithoutExtension = fileNameWithoutExtension.substring("Imp".length());
        }

        fileNameWithoutExtension = fileNameWithoutExtension + "Template";

        for (Map.Entry<String, Function<ColumnMeta, String>> entry : langMap.entrySet()) {
            String langSuffix = entry.getKey();
            Function<ColumnMeta, String> extractor = entry.getValue();

            // 导出 Excel
            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("Sheet1");
                Row headerRow = sheet.createRow(0);

                // 样式缓存：红色字体样式 / 默认字体样式
                CellStyle defaultHeaderStyle = wb.createCellStyle();
                Font defaultFont = wb.createFont();
                defaultFont.setBold(true);
                defaultFont.setColor(IndexedColors.BLACK.getIndex()); // 白色字体
                defaultHeaderStyle.setFont(defaultFont);
                // 设置背景颜色 #8EA9DB
                defaultHeaderStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex()); // 比较接近 #8EA9DB
                defaultHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                // 设置边框样式
                defaultHeaderStyle.setBorderBottom(BorderStyle.THIN);
                defaultHeaderStyle.setBorderLeft(BorderStyle.THIN);
                defaultHeaderStyle.setBorderRight(BorderStyle.THIN);
                defaultHeaderStyle.setBorderTop(BorderStyle.THIN);

                CellStyle requiredHeaderStyle = wb.createCellStyle();
                Font redFont = wb.createFont();
                redFont.setBold(true);
                redFont.setColor(IndexedColors.RED.getIndex()); // 红色字体
                requiredHeaderStyle.setFont(redFont);
                requiredHeaderStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
                requiredHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                requiredHeaderStyle.setBorderBottom(BorderStyle.THIN);
                requiredHeaderStyle.setBorderLeft(BorderStyle.THIN);
                requiredHeaderStyle.setBorderRight(BorderStyle.THIN);
                requiredHeaderStyle.setBorderTop(BorderStyle.THIN);

                for (int i = 0; i < headers.size(); i++) {
                    ColumnMeta col = columns.get(i);
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(extractor.apply(col));

                    if (col.isRequired()) {
                        cell.setCellStyle(requiredHeaderStyle);
                    } else {
                        cell.setCellStyle(defaultHeaderStyle);
                    }

                    sheet.setColumnWidth(i, 20 * 256);
                }

                String outputPath = basePath + "\\" + langSuffix + "\\" + fileNameWithoutExtension + ".xlsx";
                File outFile = new File(outputPath);
                File parentDir = outFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    if (!parentDir.mkdirs()) {
                        Messages.showErrorDialog(project, "无法创建目录: " + parentDir.getAbsolutePath(), "错误");
                        return;
                    }
                }

                try (FileOutputStream out = new FileOutputStream(outFile)) {
                    wb.write(out);
                }

            } catch (Exception ex) {
                Messages.showErrorDialog(project, "导出 " + langSuffix + " 文件失败：" + ex.getMessage(), "错误");
            }
        }

        // 自动打开文件所在目录
        VirtualFile excelFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(basePath);
        if (excelFile != null) {
            RevealFileAction.openFile(excelFile.toNioPath());
        }

        Messages.showInfoMessage("Excel 所有语言版本导出成功", "成功");
    }

    @Override
    public void update(AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        e.getPresentation().setEnabledAndVisible(file != null && file.getName().endsWith(".xml"));
    }

    // private record ColumnMeta(int col, String header) {}

    public static class ColumnMeta {
        private final int col;
        private final String header;
        private final boolean required;
        private final String headerEn;
        private final String headerCht;
        private final String headerVn;


        public ColumnMeta(int col, String header, boolean required, String headerEn, String headerCht, String headerVn) {
            this.col = col;
            this.header = header;
            this.required = required;
            this.headerEn = headerEn;
            this.headerCht = headerCht;
            this.headerVn = headerVn;
        }

        public int getCol() {
            return col;
        }

        public String getHeader() {
            return header;
        }

        public String getHeaderCht() {
            return headerCht;
        }

        public String getHeaderEn() {
            return headerEn;
        }

        public String getHeaderVn() {
            return headerVn;
        }

        public boolean isRequired() {
            return required;
        }
    }
}
