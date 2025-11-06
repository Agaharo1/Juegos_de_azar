package p3.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import p3.model.Hand;

/**
 * Diálogo para editar manualmente la mano de un jugador.
 * – No depende de GameState ni Deck (se inyecta lógica por callbacks).
 * – Valida vía callback (en tu GUI ya compruebas colisiones con mesa y otros jugadores).
 * – UI: input "AhKd", mensaje de error, botones Guardar / Quitar mano / Cancelar.
 */
public class HandEditorDialog extends JDialog {

    // ===== API de validación =====
    public static class ValidationResult {
        public final boolean ok;
        public final Hand hand;       // mano parseada si ok
        public final String message;  // error a mostrar si !ok

        private ValidationResult(boolean ok, Hand hand, String message) {
            this.ok = ok; this.hand = hand; this.message = message;
        }
        public static ValidationResult ok(Hand h)          { return new ValidationResult(true,  h, null); }
        public static ValidationResult error(String msg)   { return new ValidationResult(false, null, msg); }
    }

    private final JTextField tf = new JTextField(6);
    private final JLabel error = new JLabel(" ");

    private final Function<String, ValidationResult> validator; // la pones desde PokerEquityGUI
    private final Consumer<Hand> onSave;   // qué hacer al guardar (setPlayerHand, repaint, equities…)
    private final Runnable onClear;        // qué hacer al quitar mano

    public HandEditorDialog(
            Window owner,
            String titulo,
            String initialText,
            Function<String, ValidationResult> validator,
            Consumer<Hand> onSave,
            Runnable onClear
    ) {
        super(owner, titulo, ModalityType.APPLICATION_MODAL);
        this.validator = validator;
        this.onSave = onSave;
        this.onClear = onClear;

        buildUI(initialText);
        setSize(360, 160);
        setLocationRelativeTo(owner); // centrado sobre tu ventana principal
    }

    // ================= UI =================
    private void buildUI(String initialText) {
        JPanel content = new JPanel(new BorderLayout(10, 10));

        // Top: etiqueta + input
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JLabel lbl = new JLabel("Introduce mano (ej: AhKd):");
        tf.setText(initialText == null ? "" : initialText);
        tf.setFont(new Font("Consolas", Font.PLAIN, 14));
        tf.setColumns(6);
        top.add(lbl);
        top.add(tf);
        content.add(top, BorderLayout.NORTH);

        // Error
        error.setForeground(new Color(200, 40, 40));
        error.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        error.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        content.add(error, BorderLayout.CENTER);

        // Botones
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton bSave   = new JButton("Guardar");
        JButton bClear  = new JButton("Quitar mano");
        JButton bCancel = new JButton("Cancelar");

        bSave.addActionListener(e -> onClickGuardar());
        bClear.addActionListener(e -> onClickQuitar());
        bCancel.addActionListener(e -> dispose());

        btns.add(bClear);
        btns.add(bCancel);
        btns.add(bSave);

        content.add(btns, BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().setDefaultButton(bSave);
    }

    // ============== Lógica botones ==============
    private void onClickGuardar() {
        String raw = tf.getText() == null ? "" : tf.getText().trim();

        // Si no has pasado un validador, hacemos una validación mínima con Hand.fromString
        ValidationResult vr;
        if (validator != null) {
            vr = validator.apply(raw);
        } else {
            vr = validateWithHandFromString(raw);
        }

        if (!vr.ok) {
            error.setText(vr.message == null ? "Entrada inválida." : vr.message);
            return;
        }
        onSave.accept(vr.hand);
        dispose();
    }

    private void onClickQuitar() {
        onClear.run();
        dispose();
    }

    // ============== Validación mínima por defecto ==============
    private static ValidationResult validateWithHandFromString(String input) {
        String t = input.replaceAll("\\s+", "");
        if (t.length() != 4) return ValidationResult.error("Usa 4 caracteres: AhKd, 7c7d, …");

        try {
            Hand h = Hand.fromString(t); // en tu modelo valida formato y cartas iguales
            return ValidationResult.ok(h);
        } catch (IllegalArgumentException ex) {
            return ValidationResult.error(ex.getMessage());
        }
    }
}
