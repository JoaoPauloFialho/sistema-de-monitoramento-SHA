package br.com.monitoring.ui.gui.components;

import br.com.monitoring.ui.gui.style.Theme;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

public class ModernButton extends JButton {
    private Color normalBg;
    private Color hoverBg;
    private Color normalFg;

    private boolean isHovered = false;

    public ModernButton(String text) {
        this(text, false);
    }

    public ModernButton(String text, boolean secondary) {
        super(text);

        if (secondary) {
            this.normalBg = Theme.SECONDARY;
            this.hoverBg = new Color(228, 228, 231); // Slightly darker zinc 200
            this.normalFg = Theme.SECONDARY_FG;
        } else {
            this.normalBg = Theme.PRIMARY;
            this.hoverBg = new Color(39, 39, 42); // Zinc 900 (slightly lighter than 950)
            this.normalFg = Theme.PRIMARY_FG;
        }

        setFont(Theme.FONT_MEDIUM);
        setForeground(normalFg);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        setBorder(Theme.createPaddingBorder(8, 16, 8, 16));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        if (isHovered) {
            g2.setColor(hoverBg);
        } else {
            g2.setColor(normalBg);
        }

        // Rounded corners radius 6
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

        // Text
        FontMetrics fm = g2.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(getText())) / 2;
        int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

        g2.setColor(getForeground());
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}
