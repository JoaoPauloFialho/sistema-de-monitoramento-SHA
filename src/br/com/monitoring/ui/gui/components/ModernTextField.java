package br.com.monitoring.ui.gui.components;

import br.com.monitoring.ui.gui.style.Theme;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JTextField;

public class ModernTextField extends JTextField {
    public ModernTextField() {
        super();
        setup();
    }

    public ModernTextField(String text) {
        super(text);
        setup();
    }

    private void setup() {
        setFont(Theme.FONT_REGULAR);
        setForeground(Theme.FOREGROUND);
        setBackground(Theme.BACKGROUND);
        setCaretColor(Theme.FOREGROUND);

        // Padding inside the text field
        setBorder(BorderFactory.createCompoundBorder(
                Theme.createBorder(),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // We could add focus ring effects here if needed
    }
}
