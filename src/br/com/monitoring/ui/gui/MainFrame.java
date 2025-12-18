package br.com.monitoring.ui.gui;

import br.com.monitoring.facade.MonitoringFacade;
import br.com.monitoring.ui.gui.components.ModernButton;
import br.com.monitoring.ui.gui.style.Theme;
import br.com.monitoring.ui.gui.views.DashboardView;
import br.com.monitoring.ui.gui.views.UserManagementView;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainFrame extends JFrame {
    private MonitoringFacade facade;
    private JPanel mainContent;
    private CardLayout cardLayout;

    // Views
    private DashboardView dashboardView;
    private UserManagementView userView;

    public MainFrame() {
        this.facade = new MonitoringFacade();
        // Monitoring starts manually via button, not automatically
        initUI();
    }

    private void initUI() {
        setTitle("Water Meter Monitoring - Admin");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout());

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(10, 1, 0, 10)); // Vertical list
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBackground(Theme.SECONDARY);
        sidebar.setBorder(Theme.createPaddingBorder(20, 10, 20, 10));

        addSidebarButton(sidebar, "Dashboard", "DASHBOARD");
        addSidebarButton(sidebar, "Create", "CREATE");
        // addSidebarButton(sidebar, "Monitoring", "MONITORING");
        addSidebarButton(sidebar, "Exit", "EXIT");

        add(sidebar, BorderLayout.WEST);

        // Main Content Area
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(Theme.BACKGROUND);

        // Initialize Views
        dashboardView = new DashboardView(facade);
        userView = new UserManagementView(facade);

        // Set parent frame for alert system
        facade.setAlertSystemParentFrame(this);

        mainContent.add(dashboardView, "DASHBOARD");
        mainContent.add(userView, "CREATE");

        add(mainContent, BorderLayout.CENTER);
    }

    private void addSidebarButton(JPanel sidebar, String text, String actionCommand) {
        ModernButton btn = new ModernButton(text, true); // Secondary style for sidebar
        btn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btn.addActionListener(e -> {
            if ("EXIT".equals(actionCommand)) {
                System.exit(0);
            } else {
                cardLayout.show(mainContent, actionCommand);
                // Refresh data if needed when switching
                if ("DASHBOARD".equals(actionCommand))
                    dashboardView.refreshStats();
            }
        });
        sidebar.add(btn);
    }
}
