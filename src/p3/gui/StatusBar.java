package p3.gui;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Barra de estado inferior con dos zonas:
 *  - Mensaje a la izquierda (estado general)
 *  - Mensaje contextual a la derecha (información de apoyo)
 */
public class StatusBar extends JPanel {

    private final JLabel leftLabel  = new JLabel("Listo.");
    private final JLabel rightLabel = new JLabel("");

    public StatusBar() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG_PANEL);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.BORDER));

        styleLabel(leftLabel);
        styleLabel(rightLabel);

        add(leftLabel, BorderLayout.WEST);
        add(rightLabel, BorderLayout.EAST);
    }

    /** Aplica formato estándar de la aplicación a las etiquetas. */
    private void styleLabel(JLabel label) {
        label.setForeground(UiTheme.FG_TEXT_DIM);
        label.setFont(UiTheme.F_10);
    }

    /** Muestra un mensaje principal (lado izquierdo). */
    public void setMessage(String msg) {
        leftLabel.setText(msg != null ? msg : "");
    }

    /** Muestra información auxiliar (lado derecho). */
    public void setRight(String msg) {
        rightLabel.setText(msg != null ? msg : "");
    }
}
