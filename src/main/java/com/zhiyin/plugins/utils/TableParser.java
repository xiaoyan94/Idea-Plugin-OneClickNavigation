package com.zhiyin.plugins.utils;

import com.zhiyin.plugins.translator.TranslateException;
import com.zhiyin.plugins.translator.baidu.BaiduTranslator;
import com.zhiyin.plugins.translator.youdao.YouDaoTranslate;

import java.util.*;
import java.util.regex.*;

public class TableParser {

    // Method to extract table name from CREATE TABLE statement
    public static String extractTableName(String createTableSQL) {
        // Regular expression to match table name after CREATE TABLE keyword
        @SuppressWarnings("RegExpRedundantClassElement")
        String regex = "\\bCREATE\\s+TABLE\\s+`?([\\w\\d_]+)`?\\s*\\(";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(createTableSQL);

        if (matcher.find()) {
            return matcher.group(1); // Group 1 contains the table name
        } else {
            return null; // Return null if no table name is found
        }
    }

    public static List<Map<String, Object>> parseCreateTable(String createTableSql) {
        List<Map<String, Object>> columns = new ArrayList<>();

        // Regular expression to match columns
        String columnRegex = "`(\\w+)`\\s+(\\w+)(\\((\\d+)(,(\\d+))?\\))?";
        Pattern columnPattern = Pattern.compile(columnRegex);
        Matcher columnMatcher = columnPattern.matcher(createTableSql);

        // Regular expression to match comments
        String commentRegex = "`(\\w+)`.*?COMMENT\\s+'(.*?)'";
        Pattern commentPattern = Pattern.compile(commentRegex);
        Matcher commentMatcher = commentPattern.matcher(createTableSql);

        // Regular expression to match nullable information
        String nullableRegex = "`(\\w+)`\\s+(\\w+)([^,]+)";
        Pattern nullablePattern = Pattern.compile(nullableRegex);
        Matcher nullableMatcher = nullablePattern.matcher(createTableSql);

        Map<String, String> comments = new HashMap<>();
        while (commentMatcher.find()) {
            comments.put(commentMatcher.group(1), commentMatcher.group(2));
        }

        Map<String, Boolean> nullables = new HashMap<>();
        while (nullableMatcher.find()) {
            String columnName = nullableMatcher.group(1);
            String nullable = nullableMatcher.group(3);
            boolean isNullable = !(nullable.contains("NOT NULL") || nullable.contains("AUTO_INCREMENT"));
            nullables.put(columnName, isNullable);
        }

        while (columnMatcher.find()) {
            Map<String, Object> columnInfo = new HashMap<>();
            String columnName = columnMatcher.group(1);
            columnInfo.put("name", columnName == null ? "" : columnName.toLowerCase());
            columnInfo.put("type", getType(columnMatcher.group(2)));

            if (columnMatcher.group(4) != null) {
                columnInfo.put("length", columnMatcher.group(4));
            } else {
                columnInfo.put("length", "");
            }

            columnInfo.put("nullable", nullables.getOrDefault(columnName, true).toString());
            columnInfo.put("isRequired", "true".equals(columnInfo.get("nullable")) ? "false" : "true");
            columnInfo.put("comment", comments.getOrDefault(columnName, ""));

            columns.add(columnInfo);
        }

        return columns;
    }

    public static String getType(String type){
        if(type == null){
            return "";
        }
        switch (type.toLowerCase()){
            case "varchar":
                return "string";
            case "decimal":
                return "number";
            default:
                return type;
        }
    }

    /**
     * 解析DQL
     * @param createTableSql
     * @return
     */
    public static List<Map<String, Object>> parseDQL(String sql) {
        List<Map<String, Object>> columns = new ArrayList<>();

        // 提取SELECT和FROM之间的部分
        Pattern selectPattern = Pattern.compile("select\\s+(.+?)\\s+from",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher selectMatcher = selectPattern.matcher(sql);

        if (!selectMatcher.find()) {
            throw new IllegalArgumentException("Invalid SQL: Cannot find SELECT clause");
        }

        String selectClause = selectMatcher.group(1);

        // 分割字段（考虑函数中的逗号）
        List<String> fieldExpressions = splitFields(selectClause);

        // YouDaoTranslate translator = new YouDaoTranslate();
        BaiduTranslator translator = new BaiduTranslator();

        for (String expr : fieldExpressions) {
            expr = expr.trim();

            // 检查是否有AS关键字（大小写不敏感）
            Pattern asPattern = Pattern.compile("(.+?)\\s+as\\s+([\\w]+)$",
                    Pattern.CASE_INSENSITIVE);
            Matcher asMatcher = asPattern.matcher(expr);

            String field;
            String alias;

            if (asMatcher.find()) {
                // 有AS的情况
                field = asMatcher.group(1).trim();
                alias = asMatcher.group(2).trim();
            } else {
                // 没有AS的情况，检查是否有简单别名
                Pattern simpleAliasPattern = Pattern.compile("(.+?)\\s+([\\w]+)$");
                Matcher simpleAliasMatcher = simpleAliasPattern.matcher(expr);

                if (simpleAliasMatcher.find()) {
                    field = simpleAliasMatcher.group(1).trim();
                    alias = simpleAliasMatcher.group(2).trim();
                } else {
                    // 没有别名，a.field --> 只保留field
                    field = expr.replaceAll("`", "").replaceAll("^\\s*\\w+\\.", "").trim();
                    alias = null;
                }
            }

            Map<String, Object> columnInfo = new HashMap<>();
            columnInfo.put("name", alias == null ? field : alias);
            columnInfo.put("alias", alias);
            columnInfo.put("type", "string");
            if (field != null && field.contains("date")) {
                columnInfo.put("type", "date");
            }
            if (field != null && field.contains("time")) {
                columnInfo.put("type", "datetime");
            }
            columnInfo.put("isRequired", "true");
            columnInfo.put("isQueryField" , "true");
            columnInfo.put("isDialogField" , "false");
            try {
                // columnInfo.put("comment", translator.translate(columnInfo.get("name").toString()));
                // TODO 默认中文配置
                // columnInfo.put("comment", translator.translate(columnInfo.get("name").toString(), "en", "zh"));
                columnInfo.put("comment", columnInfo.get("name"));
            } catch (Exception e) {
                columnInfo.put("comment", columnInfo.get("name"));
                e.printStackTrace();
            }
            columnInfo.put("nullable", "true");
            columnInfo.put("length", "120");
            columns.add(columnInfo);
        }

        return columns;
    }

    public static String extractTableNameFromDQL(String dql) {
        Pattern pattern = Pattern.compile("from\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(dql);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "null";
        }
    }

    private static List<String> splitFields(String selectClause) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        int parenthesesCount = 0;

        for (char c : selectClause.toCharArray()) {
            if (c == '(') {
                parenthesesCount++;
            } else if (c == ')') {
                parenthesesCount--;
            }

            if (c == ',' && parenthesesCount == 0) {
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        // 添加最后一个字段
        if (currentField.length() > 0) {
            fields.add(currentField.toString().trim());
        }

        return fields;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static void main(String[] args) {
        String createTableSql = "CREATE TABLE `biz_product` (\n" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
                "  `code` varchar(128) NOT NULL COMMENT '产品编码',\n" +
                "  `name` varchar(512) DEFAULT NULL COMMENT '产品名称',\n" +
                "  `model` varchar(512) DEFAULT NULL COMMENT '产品机型',\n" +
                "  `colour` varchar(32) DEFAULT NULL COMMENT '颜色',\n" +
                "  `note` varchar(255) DEFAULT NULL COMMENT '备注',\n" +
                "  `maintainer` varchar(64) DEFAULT NULL COMMENT '维护人',\n" +
                "  `maintaintime` datetime DEFAULT NULL,\n" +
                "  `safetystock` varchar(64) DEFAULT NULL COMMENT '安全库存量',\n" +
                "  `factoryid` int(11) DEFAULT NULL COMMENT '所属工厂ID',\n" +
                "  `producttype` varchar(32) DEFAULT NULL,\n" +
                "  `groupingid` int(11) DEFAULT '-1' COMMENT '分组管理：分组ID',\n" +
                "  `defaultwarehouse` varchar(64) DEFAULT NULL,\n" +
                "  `defaultstorage` varchar(64) DEFAULT NULL,\n" +
                "  `issync` int(11) DEFAULT '0',\n" +
                "  `price` decimal(19,4) DEFAULT NULL,\n" +
                "  `saleprice` decimal(10,2) DEFAULT NULL COMMENT '销售单价',\n" +
                "  `processingprice` decimal(19,4) DEFAULT NULL,\n" +
                "  `packrulequantity` varchar(32) DEFAULT NULL COMMENT '包规数量',\n" +
                "  `minstock` varchar(16) DEFAULT NULL COMMENT '最低库存',\n" +
                "  `maxstock` varchar(16) DEFAULT NULL COMMENT '最高库存',\n" +
                "  `property` varchar(32) DEFAULT NULL COMMENT '物料属性',\n" +
                "  `weight` decimal(10,4) DEFAULT NULL COMMENT '产品重量',\n" +
                "  `weightunit` varchar(32) DEFAULT NULL COMMENT '单重单位',\n" +
                "  `unit` varchar(32) DEFAULT NULL COMMENT '基础单位',\n" +
                "  `pickingmethod` varchar(32) DEFAULT NULL COMMENT '领料方式',\n" +
                "  `isvirtual` int(11) DEFAULT '0' COMMENT '是否虚拟物料：是：1 否：0',\n" +
                "  `shelflife` int(11) DEFAULT NULL COMMENT '保质期',\n" +
                "  `purchaseadvtime` int(11) DEFAULT NULL COMMENT '采购提前期',\n" +
                "  `deliveryadvtime` int(11) DEFAULT NULL COMMENT '隔离期',\n" +
                "  `timeunit` varchar(32) DEFAULT NULL COMMENT '时间单位',\n" +
                "  `colourid` int(11) DEFAULT NULL COMMENT '关联颜色ID',\n" +
                "  `laborused` decimal(11,3) DEFAULT NULL COMMENT '人力资源',\n" +
                "  `delflag` int(2) DEFAULT '0' COMMENT '删除标识',\n" +
                "  PRIMARY KEY (`id`) USING BTREE,\n" +
                "  KEY `index_biz_product` (`factoryid`,`code`) USING BTREE,\n" +
                "  KEY `index_product_type_1` (`producttype`) USING BTREE,\n" +
                "  KEY `index_product_type` (`factoryid`,`producttype`,`property`,`weightunit`) USING BTREE,\n" +
                "  KEY `idx_product_groupingid` (`factoryid`,`groupingid`) COMMENT '分组管理：分组ID'\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=781740 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='产品表'";

        List<Map<String, Object>> columns = parseCreateTable(createTableSql);
        for (Map<String, Object> column : columns) {
            System.out.println(column);
        }

        System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        String sql = "select 'sotMaterialReciept' as operatetype, " +
                "date(a.maintaintime) as date, " +
                "substring_index(substring_index(maintainer, '(', -1), ')', 1) as warehousemanager, " +
                "count(1) as times " +
                "from biz_wms_material_receipt_barcode a " +
                "where a.maintaintime > '2024-11-01 00:00:00' " +
                "and a.maintaintime < '2024-11-01 23:59:59' " +
                "group by 'sotMaterialReciept', date(a.maintaintime), a.maintainer";

        List<Map<String, Object>> parsedDQL = parseDQL(sql);
        for (Map<String, Object> map : parsedDQL) {
            System.out.println(map);
        }
    }
}
