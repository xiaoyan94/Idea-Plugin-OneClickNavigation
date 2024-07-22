package com.zhiyin.plugins.utils;

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
    }
}
