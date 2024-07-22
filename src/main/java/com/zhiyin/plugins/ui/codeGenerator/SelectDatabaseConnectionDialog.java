package com.zhiyin.plugins.ui.codeGenerator;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class SelectDatabaseConnectionDialog extends JDialog {

    private Map<String, String> selectedConnectionInfo;
    private final JTable table;
    private final DefaultTableModel tableModel;

    public SelectDatabaseConnectionDialog(Frame parent, List<Map<String, String>> connectionInfoList) {
        super(parent, "Select Database Connection", true);

        // Create table model and JTable
        String[] columnNames = {"File Name", "URL", "Username", "Password"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JBTable(tableModel);
        // Populate table with connection info
        for (Map<String, String> connectionInfo : connectionInfoList) {
            tableModel.addRow(new Object[]{
                    connectionInfo.get("fileName"),
                    connectionInfo.get("url"),
                    connectionInfo.get("username"),
                    connectionInfo.get("password")
            });
        }

        // Create buttons
        JButton selectButton = new JButton("Select");
        JButton closeButton = new JButton("Close");

        selectButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                // Retrieve selected connection info
                String fileName = (String) tableModel.getValueAt(selectedRow, 0);
                String url = (String) tableModel.getValueAt(selectedRow, 1);
                String username = (String) tableModel.getValueAt(selectedRow, 2);
                String password = (String) tableModel.getValueAt(selectedRow, 3);

                // Find the corresponding connection info map
                for (Map<String, String> connectionInfo : connectionInfoList) {
                    if (connectionInfo.get("fileName").equals(fileName)
                            && connectionInfo.get("url").equals(url)
                            && connectionInfo.get("username").equals(username)
                            && connectionInfo.get("password").equals(password)) {
                        selectedConnectionInfo = connectionInfo;
                        break;
                    }
                }

                dispose(); // Close dialog and return selected connection info
            }
        });

        closeButton.addActionListener(e -> dispose());

        // Layout components
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(selectButton);
        buttonPanel.add(closeButton);

        JScrollPane scrollPane = new JBScrollPane(table);

        setLayout(new BorderLayout());
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        setSize(800, 600); // Set size to be large enough for the table
        setLocationRelativeTo(parent);
    }

    public Map<String, String> getSelectedConnectionInfo() {
        return selectedConnectionInfo;
    }
}
