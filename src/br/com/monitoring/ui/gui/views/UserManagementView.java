package br.com.monitoring.ui.gui.views;

import br.com.monitoring.facade.MonitoringFacade;
import br.com.monitoring.ui.gui.components.ModernButton;
import br.com.monitoring.ui.gui.components.ModernTextField;
import br.com.monitoring.ui.gui.style.Theme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class UserManagementView extends JPanel {
    private MonitoringFacade facade;

    // Inputs
    private ModernTextField nameField;
    private ModernTextField cpfField;
    private ModernTextField addressField;
    private ModernTextField limitField;
    private javax.swing.JComboBox<br.com.monitoring.service.MeterDiscoveryService.DetectedMeter> meterSelector;

    public UserManagementView(MonitoringFacade facade) {
        this.facade = facade;
        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(Theme.BACKGROUND);
        setBorder(Theme.createPaddingBorder(30, 40, 30, 40));

        // Header
        JLabel title = new JLabel("Create User");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.FOREGROUND);
        add(title, BorderLayout.NORTH);

        // Form (Center) - Using a more compact layout
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        // Main container with padding
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(Theme.BACKGROUND);

        // Form fields in a 2-column grid
        JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        fieldsPanel.setBackground(Theme.BACKGROUND);
        fieldsPanel.setBorder(Theme.createCompoundBorder(20, 20, 20, 20));

        nameField = new ModernTextField();
        cpfField = new ModernTextField();
        addressField = new ModernTextField();
        limitField = new ModernTextField();
        meterSelector = new javax.swing.JComboBox<>();
        meterSelector.setFont(Theme.FONT_REGULAR);

        // First column
        addLabeledField(fieldsPanel, "Name", nameField);
        addLabeledField(fieldsPanel, "CPF", cpfField);
        addLabeledField(fieldsPanel, "Address", addressField);
        addLabeledField(fieldsPanel, "Limit (m³)", limitField);

        // Second column
        // Meter Selector Panel
        JPanel meterPanel = new JPanel(new BorderLayout(5, 5));
        meterPanel.setBackground(Theme.BACKGROUND);
        JLabel lblMeter = new JLabel("Assign Meter");
        lblMeter.setFont(Theme.FONT_SMALL);
        lblMeter.setForeground(Theme.MUTED_FG);
        meterPanel.add(lblMeter, BorderLayout.NORTH);
        meterPanel.add(meterSelector, BorderLayout.CENTER);
        fieldsPanel.add(meterPanel);

        // Refresh button for meters
        JPanel refreshPanel = new JPanel(new BorderLayout(5, 5));
        refreshPanel.setBackground(Theme.BACKGROUND);
        ModernButton btnRefresh = new ModernButton("Refresh Meters", true);
        btnRefresh.addActionListener(e -> refreshMeterList());
        refreshPanel.add(new JLabel(" "), BorderLayout.NORTH); // Spacer
        refreshPanel.add(btnRefresh, BorderLayout.CENTER);
        fieldsPanel.add(refreshPanel);

        // Empty spaces to fill grid
        fieldsPanel.add(new JPanel());
        fieldsPanel.add(new JPanel());

        mainPanel.add(fieldsPanel, BorderLayout.CENTER);

        // Register button at bottom
        JPanel buttonPanel = new JPanel(new java.awt.FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Theme.BACKGROUND);
        ModernButton btnAdd = new ModernButton("Register User");
        btnAdd.setPreferredSize(new java.awt.Dimension(200, 40));
        btnAdd.addActionListener(e -> registerUser());
        buttonPanel.add(btnAdd);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        refreshMeterList();

        return mainPanel;
    }

    private void refreshMeterList() {
        meterSelector.removeAllItems();
        java.util.List<br.com.monitoring.service.MeterDiscoveryService.DetectedMeter> meters = facade.discoverMeters();
        for (br.com.monitoring.service.MeterDiscoveryService.DetectedMeter m : meters) {
            meterSelector.addItem(m);
        }
    }

    private void addLabeledField(JPanel panel, String labelText, ModernTextField field) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Theme.BACKGROUND);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.MUTED_FG);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        panel.add(p);
    }

    private void registerUser() {
        try {
            String name = nameField.getText();
            String cpf = cpfField.getText();
            String address = addressField.getText();
            double limit = Double.parseDouble(limitField.getText());

            facade.registerUser(name, cpf, address, limit);

            // Register Meter if selected
            br.com.monitoring.service.MeterDiscoveryService.DetectedMeter selectedMeter = (br.com.monitoring.service.MeterDiscoveryService.DetectedMeter) meterSelector
                    .getSelectedItem();

            if (selectedMeter != null) {
                facade.registerMeter(selectedMeter.getSuggestedId(), selectedMeter.getPath(), cpf);
            }

            // Clear fields
            nameField.setText("");
            cpfField.setText("");
            addressField.setText("");
            limitField.setText("");
            refreshMeterList(); // Refresh to maybe remove used ones? (Though req didn't specify unique, usually
                                // meters are unique)

            javax.swing.JOptionPane.showMessageDialog(this, "User & Meter Registered!");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
