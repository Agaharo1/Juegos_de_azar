package tp2.gui;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel {
    private final JLabel left = new JLabel("Listo.");
    private final JLabel right = new JLabel("");

    public StatusBar() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG_PANEL);
        setBorder(BorderFactory.createMatteBorder(1,0,0,0, UiTheme.BORDER));

        left.setForeground(UiTheme.FG_TEXT_DIM);
        left.setFont(UiTheme.F_10);

        right.setForeground(UiTheme.FG_TEXT_DIM);
        right.setFont(UiTheme.F_10);

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);
    }

    public void setMessage(String msg) { left.setText(msg); }
    public void setRight(String msg)   { right.setText(msg); }
}
