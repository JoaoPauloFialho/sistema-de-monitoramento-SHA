package br.com.monitoring.ui.gui.style;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

public class Theme {
    // Colors (inspired by Shadcn default/zinc theme)
    public static final Color BACKGROUND = new Color(255, 255, 255);
    public static final Color FOREGROUND = new Color(9, 9, 11);
    public static final Color CARD_BG = new Color(255, 255, 255);
    public static final Color BORDER_COLOR = new Color(228, 228, 231);

    public static final Color PRIMARY = new Color(24, 24, 27); // Zinc 950
    public static final Color PRIMARY_FG = new Color(250, 250, 250); // Zinc 50

    public static final Color SECONDARY = new Color(244, 244, 245); // Zinc 100
    public static final Color SECONDARY_FG = new Color(24, 24, 27); // Zinc 950

    public static final Color MUTED = new Color(244, 244, 245);
    public static final Color MUTED_FG = new Color(113, 113, 122);

    public static final Color ACCENT = new Color(244, 244, 245);
    public static final Color ACCENT_FG = new Color(24, 24, 27);

    public static final Color DESTRUCTIVE = new Color(239, 68, 68);
    public static final Color DESTRUCTIVE_FG = new Color(250, 250, 250);

    // Fonts
    // Using generic sans-serif since we can't easily rely on user having Inter
    // installed
    public static final Font FONT_REGULAR = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_MEDIUM = new Font("SansSerif", Font.BOLD, 14); // Swing doesn't have MEDIUM, mapping
                                                                                 // to BOLD usually
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);

    // Borders
    public static Border createBorder() {
        return BorderFactory.createLineBorder(BORDER_COLOR, 1);
    }

    public static Border createPaddingBorder(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    public static Border createCompoundBorder(int top, int left, int bottom, int right) {
        return BorderFactory.createCompoundBorder(
                createBorder(),
                createPaddingBorder(top, left, bottom, right));
    }
}
