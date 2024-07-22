package com.zhiyin.plugins.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseMetadataUtil {

    // Method to get table metadata information
    public static List<Map<String, Object>> getTableMetadata(
            String jdbcUrl, String username, String password, String tableName) {
        List<Map<String, Object>> tableMetadata = new ArrayList<>();
        Connection connection = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish a connection to the database
            connection = DriverManager.getConnection(jdbcUrl, username, password);

            String createTableSQL = getCreateTableSQL(connection, tableName);

            tableMetadata = TableParser.parseCreateTable(createTableSQL);

        } catch (SQLException e) {
            System.out.println("Error retrieving table metadata: " + e.getMessage());
//            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Error loading JDBC driver: " + e.getMessage());
        } finally {
            // Close the connection
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.out.println("Error closing connection: " + e.getMessage());
//                    e.printStackTrace();
                }
            }
        }

        return tableMetadata;
    }

    // Method to execute SHOW CREATE TABLE and return the SQL statement
    public static String getCreateTableSQL(Connection connection, String tableName) throws SQLException {
        String sql = "SHOW CREATE TABLE " + tableName;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getString(2); // The SQL statement is in the second column
            } else {
                throw new SQLException("No create table SQL found for table: " + tableName);
            }
        }
    }

    /**
     * MySQL 类型转换 字符串的一律返回 string 整数类型的一律返回int 时间类型的返回 datetime
     */
    private static String getType(String columnType) {
        switch (columnType) {
            case "INT":
            case "INTEGER":
            case "TINYINT":
            case "SMALLINT":
            case "MEDIUMINT":
            case "BIGINT":
            case "BIT":
            case "SERIAL":
                return "int";
            case "DATE":
            case "DATETIME":
            case "TIMESTAMP":
            case "TIME":
                return "datetime";
            case "DECIMAL":
                return "number";
            default:
                return "string";
        }
    }

    // getAllDatabaseConnectionsMetaData, params connection info by DatabaseConnectionFinder class
    public static List<Map<String, Object>> getAllDatabaseConnectionsMetaData(List<Map<String, String>> databaseConnections, String tableName) {
        List<Map<String, Object>> tableMetadata = new ArrayList<>();
        for (Map<String, String> connectionInfo : databaseConnections) {
            String jdbcUrl = connectionInfo.get("url");
            String username = connectionInfo.get("username");
            String password = connectionInfo.get("password");
            List<Map<String, Object>> metadata = getTableMetadata(jdbcUrl, username, password, tableName);
            tableMetadata.addAll(metadata);
        }
        return tableMetadata;
    }

    public static void main(String[] args) {
        // Example usage
        String jdbcUrl = "jdbc:mysql://192.168.116.42:3306/test";
        String username = "root";
        String password = "root";
        String tableName = "your_table_name";

        List<Map<String, Object>> metadata = getTableMetadata(jdbcUrl, username, password, tableName);

        for (Map<String, Object> column : metadata) {
            System.out.println("Column Info: " + column);
        }
    }
}
