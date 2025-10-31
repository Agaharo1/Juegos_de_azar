package tp2.gui;

import java.awt.*;

public final class UiTheme {
    private UiTheme() {}

    // Colores base
    public static final Color BG_DARK   = new Color(20, 30, 45);
    public static final Color BG_PANEL  = new Color(30, 40, 55);
    public static final Color BG_CARD   = new Color(34, 60, 85);
    public static final Color BG_INPUT  = new Color(50, 60, 80);
    public static final Color BORDER    = new Color(75, 85, 99);
    public static final Color FG_TEXT   = new Color(220, 220, 220);
    public static final Color FG_TEXT_DIM = new Color(180, 180, 180);

    // Acentos y botones
    public static final Color ACCENT_PRIMARY     = new Color(59, 130, 246);
    public static final Color ACCENT_PRIMARY_HOV = new Color(37, 99, 235);
    public static final Color HERO_BORDER        = new Color(100, 200, 255);
    public static final Color TABLE_STROKE       = new Color(100, 130, 160);

    // Chip equity
    public static final Color CHIP_FILL  = new Color(34, 150, 255);
    public static final Color CHIP_STROK = new Color(20, 100, 200);

    // Fuentes
    public static final Font  F_9   = new Font("Segoe UI", Font.PLAIN, 9);
    public static final Font  F_10  = new Font("Segoe UI", Font.PLAIN, 10);
    public static final Font  F_10B = new Font("Segoe UI", Font.BOLD, 10);
    public static final Font  F_13B = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font  F_18B = new Font("Segoe UI", Font.BOLD, 18);
}
