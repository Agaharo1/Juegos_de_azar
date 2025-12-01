package p3.gui;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.*;

import p3.model.Hand;

/**
 * Diálogo genérico para editar una mano textual.
 * Recibe callbacks para validar, guardar o limpiar la mano.
 */
public class HandEditorDialog extends JDialog {

    // ------------ Resultado de validación ------------

    public static class ValidationResult {
        public final boolean ok;
        public final Hand hand;
        public final String message;

        private ValidationResult(boolean ok, Hand hand, String message) {
            this.ok = ok;
            this.hand = hand;
            this.message = message;
        }

        public static ValidationResult ok(Hand h)      { return new ValidationResult(true, h, null); }
        public static ValidationResult error(String m) { return new ValidationResult(false, null, m); }
    }

    // ------------ Atributos ------------

    private final JTextField tf = new JTextField(6);
    private final JLabel errorLabel = new JLabel(" ");

    private final Function<String, ValidationResult> validator;
    private final Consumer<Hand> onSave;
    private final Runnable onClear;

    // ------------ Constructor ------------

    public HandEditorDialog(
            Window owner,
            String title,
            String initialText,
            Function<String, ValidationResult> validator,
            Consumer<Hand> onSave,
            Runnable onClear
    ) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.validator = validator;
        this.onSave = onSave;
        this.onClear = onClear;

        buildUI(initialText);
        setSize(360, 160);
        setLocationRelativeTo(owner);
    }

    // ------------ Construcción UI ------------

    private void buildUI(String initial) {
        JPanel content = new JPanel(new BorderLayout(10, 10));

        // ----- Zona superior -----
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(new JLabel("Introduce mano (ej: AhKd):"));

        tf.setText(initial == null ? "" : initial);
        tf.setFont(new Font("Consolas", Font.PLAIN, 14));
        top.add(tf);

        content.add(top, BorderLayout.NORTH);

        // ----- Zona error -----
        errorLabel.setForeground(new Color(200, 40, 40));
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        content.add(errorLabel, BorderLayout.CENTER);

        // ----- Botones -----
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnSave   = new JButton("Guardar");
        JButton btnClear  = new JButton("Quitar mano");
        JButton btnCancel = new JButton("Cancelar");

        btnSave.addActionListener(e -> handleSave());
        btnClear.addActionListener(e -> handleClear());
        btnCancel.addActionListener(e -> dispose());

        btns.add(btnClear);
        btns.add(btnCancel);
        btns.add(btnSave);

        content.add(btns, BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().setDefaultButton(btnSave);
    }

    // ------------ Acciones ------------

    private void handleSave() {
        String raw = safeText(tf);

        ValidationResult vr = (validator != null)
                ? validator.apply(raw)
                : validateFallback(raw);

        if (!vr.ok) {
            errorLabel.setText(vr.message != null ? vr.message : "Entrada inválida.");
            return;
        }

        onSave.accept(vr.hand);
        dispose();
    }

    private void handleClear() {
        onClear.run();
        dispose();
    }

    // ------------ Validación mínima por defecto ------------

    private static ValidationResult validateFallback(String input) {
        String t = input.replaceAll("\\s+", "");
        if (t.length() != 4)
            return ValidationResult.error("Usa 4 caracteres: AhKd, 7c7d…");

        try {
            Hand h = Hand.fromString(t);
            return ValidationResult.ok(h);
        } catch (IllegalArgumentException ex) {
            return ValidationResult.error(ex.getMessage());
        }
    }

    // ------------ Utilidades ------------

    private static String safeText(JTextField tf) {
        return tf.getText() == null ? "" : tf.getText().trim();
    }
}
