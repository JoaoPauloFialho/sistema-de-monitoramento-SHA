package br.com.monitoring.ui.gui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class SwingMain {
    public static void main(String[] args) {
        // Try to set system look and feel for window decorations
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
