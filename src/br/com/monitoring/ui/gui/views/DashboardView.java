package br.com.monitoring.ui.gui.views;

import br.com.monitoring.facade.MonitoringFacade;
import br.com.monitoring.model.User;
import br.com.monitoring.ui.gui.components.ModernButton;
import br.com.monitoring.ui.gui.style.Theme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class DashboardView extends JPanel {
    private MonitoringFacade facade;

    private JLabel lblTotalUsers;
    private DefaultTableModel usersTableModel;
    private JTable usersTable;
    private JLabel statusLabel;

    public DashboardView(MonitoringFacade facade) {
        this.facade = facade;
        setupUI();
        refreshStats(); // Initial load
    }

    private void setupUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(Theme.BACKGROUND);
        setBorder(Theme.createPaddingBorder(30, 40, 30, 40));

        // Header
        JLabel title = new JLabel("Dashboard");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.FOREGROUND);
        add(title, BorderLayout.NORTH);

        // Content - Total Users Card
        JPanel topPanel = new JPanel(new BorderLayout(20, 20));
        topPanel.setBackground(Theme.BACKGROUND);

        lblTotalUsers = createValueLabel("0");
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(Theme.BACKGROUND);
        cardPanel.add(createCard("Total Users", lblTotalUsers), BorderLayout.CENTER);
        topPanel.add(cardPanel, BorderLayout.NORTH);

        // Users List Table
        usersTableModel = new DefaultTableModel(new Object[] { "Name", "CPF", "Status", "Consumption", "Limit", "Actions" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only Actions column is editable (for buttons)
            }
        };
        usersTable = new JTable(usersTableModel);
        usersTable.setFillsViewportHeight(true);
        usersTable.setRowHeight(35);
        usersTable.setFont(Theme.FONT_REGULAR);
        usersTable.getTableHeader().setFont(Theme.FONT_MEDIUM);
        usersTable.setBackground(Theme.BACKGROUND);
        usersTable.setForeground(Theme.FOREGROUND);
        usersTable.getTableHeader().setBackground(Theme.SECONDARY);
        usersTable.getTableHeader().setForeground(Theme.FOREGROUND);
        
        // Add button renderer and editor for Actions column
        usersTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        usersTable.getColumn("Actions").setCellEditor(new ButtonEditor(new javax.swing.JCheckBox(), this));

        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setBackground(Theme.BACKGROUND);
        topPanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.CENTER);

        // Actions Panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionsPanel.setBackground(Theme.BACKGROUND);

        // Status label - initialize first
        statusLabel = new JLabel("Monitoring: STOPPED");
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.MUTED_FG);

        // Monitoring Control Buttons
        ModernButton btnStartMonitoring = new ModernButton("Start Monitoring");
        ModernButton btnStopMonitoring = new ModernButton("Stop Monitoring", true);
        btnStopMonitoring.setEnabled(false);

        btnStartMonitoring.addActionListener(e -> {
            facade.startMonitoring();
            btnStartMonitoring.setEnabled(false);
            btnStopMonitoring.setEnabled(true);
            statusLabel.setText("Monitoring: ACTIVE (checking every 1 seconds)");
        });

        btnStopMonitoring.addActionListener(e -> {
            facade.stopMonitoring();
            btnStartMonitoring.setEnabled(true);
            btnStopMonitoring.setEnabled(false);
            statusLabel.setText("Monitoring: STOPPED");
        });

        actionsPanel.add(btnStartMonitoring);
        actionsPanel.add(btnStopMonitoring);
        actionsPanel.add(statusLabel);

        // Email Toggle (RNF07)
        javax.swing.JToggleButton btnEmail = new javax.swing.JToggleButton("Email Notifications: ON");
        btnEmail.setSelected(true);
        btnEmail.setFont(Theme.FONT_MEDIUM);
        btnEmail.setFocusPainted(false);
        btnEmail.setBackground(Theme.BACKGROUND);
        btnEmail.addActionListener(e -> {
            boolean enabled = btnEmail.isSelected();
            btnEmail.setText("Email Notifications: " + (enabled ? "ON" : "OFF"));
            facade.setEmailNotificationsEnabled(enabled);
        });

        // Export Database Button
        ModernButton btnExport = new ModernButton("Export Database", true);
        btnExport.addActionListener(e -> exportDatabase());

        actionsPanel.add(btnEmail);
        actionsPanel.add(btnExport);

        add(actionsPanel, BorderLayout.SOUTH);

        // Start auto-refresh timer (every 2 seconds) - only refreshes UI, doesn't run monitoring
        startAutoRefresh();
    }

    // Public method to be called when view is shown
    public void refreshStats() {
        try {
            List<User> users = facade.listUsers();
            int totalUsers = users.size();

            lblTotalUsers.setText(String.valueOf(totalUsers));

            // Refresh users table
            usersTableModel.setRowCount(0);
            for (User user : users) {
                double consumption = facade.getCurrentConsumption(user);
                boolean limitExceeded = facade.isLimitExceeded(user);
                String status = limitExceeded ? "⚠️ LIMIT EXCEEDED" : "✓ OK";
                
                usersTableModel.addRow(new Object[] {
                    user.getName(),
                    user.getCpf(),
                    status,
                    String.format("%.2f m³", consumption),
                    String.format("%.2f m³", user.getConsumptionLimit()),
                    "Delete" // Placeholder for button
                });
            }
        } catch (Exception e) {
            System.err.println("Error refreshing stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteUser(String cpf) {
        int confirm = javax.swing.JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete user with CPF: " + cpf + "?",
            "Confirm Delete",
            javax.swing.JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                facade.deleteUser(cpf);
                refreshStats();
                javax.swing.JOptionPane.showMessageDialog(this, "User deleted successfully!");
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage());
            }
        }
    }

    private void exportDatabase() {
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Export Database");
        fileChooser.setSelectedFile(new java.io.File("monitoring_export_" + 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".db"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            try {
                String exportPath = fileChooser.getSelectedFile().getAbsolutePath();
                facade.exportDatabase(exportPath);
                javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Database exported successfully to:\n" + exportPath,
                    "Export Success",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Error exporting database: " + e.getMessage(),
                    "Export Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void startAutoRefresh() {
        javax.swing.Timer timer = new javax.swing.Timer(1500, e -> {
            refreshStats();
            // Update status label if monitoring is active
            if (facade.isMonitoringActive()) {
                statusLabel.setText("Monitoring: ACTIVE (checking every 1.5 seconds)");
            }
        });
        timer.start();
    }


    private JLabel createValueLabel(String initialValue) {
        JLabel value = new JLabel(initialValue);
        value.setFont(Theme.FONT_BOLD);
        value.setForeground(Theme.FOREGROUND);
        value.setFont(value.getFont().deriveFont(32f)); // Make it big
        return value;
    }

    private JPanel createCard(String titleText, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(Theme.createCompoundBorder(20, 20, 20, 20));

        JLabel title = new JLabel(titleText);
        title.setFont(Theme.FONT_MEDIUM);
        title.setForeground(Theme.MUTED_FG);

        card.add(title, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // Button Renderer for Actions column
    private class ButtonRenderer extends javax.swing.JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Delete");
            setFont(Theme.FONT_SMALL);
            setBackground(Theme.DESTRUCTIVE);
            setForeground(Theme.DESTRUCTIVE_FG);
            return this;
        }
    }

    // Button Editor for Actions column
    private class ButtonEditor extends javax.swing.DefaultCellEditor {
        protected ModernButton button;
        private String label;
        private boolean isPushed;
        private DashboardView parent;

        public ButtonEditor(javax.swing.JCheckBox checkBox, DashboardView parent) {
            super(checkBox);
            this.parent = parent;
            button = new ModernButton("Delete", true);
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int row = usersTable.getSelectedRow();
                if (row >= 0) {
                    String cpf = (String) usersTableModel.getValueAt(row, 1);
                    parent.deleteUser(cpf);
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}
