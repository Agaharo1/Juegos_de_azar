package tp2.gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.*;
import tp2.model.Hand;

public class HandEditorDialog extends JDialog {

    private final JTextField tf = new JTextField(6);
    private final JLabel error = new JLabel(" ");
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnQuitar = new JButton("Quitar mano");
    private final JButton btnCancelar = new JButton("Cancelar");

    private final Function<String, ValidationResult> validator;
    private final Consumer<Hand> onSave;
    private final Runnable onClear;

    public static class ValidationResult {
        public final boolean ok;
        public final Hand hand;
        public final String message;
        private ValidationResult(boolean ok, Hand hand, String message) {
            this.ok = ok; this.hand = hand; this.message = message;
        }
        public static ValidationResult ok(Hand h){ return new ValidationResult(true, h, null); }
        public static ValidationResult error(String m){ return new ValidationResult(false, null, m); }
    }

    public HandEditorDialog(
            Window owner,
            String titulo,
            String initialText,
            Function<String, ValidationResult> validator,
            Consumer<Hand> onSave,
            Runnable onClear) {
        super(owner, titulo, ModalityType.APPLICATION_MODAL);
        this.validator = validator;
        this.onSave = onSave;
        this.onClear = onClear;

        JPanel content = new JPanel(new BorderLayout(10,10));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Mano (AhKd): "));
        tf.setText(initialText == null ? "" : initialText);
        top.add(tf);
        content.add(top, BorderLayout.NORTH);

        error.setForeground(new Color(180,0,0));
        content.add(error, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnQuitar);
        buttons.add(btnCancelar);
        buttons.add(btnGuardar);
        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().setDefaultButton(btnGuardar);
        setSize(360, 150);
        setLocationRelativeTo(owner);

        btnGuardar.addActionListener(e -> onClickGuardar());
        btnQuitar.addActionListener(e -> onClickQuitar());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void onClickGuardar() {
        ValidationResult vr = validator.apply(tf.getText().trim());
        if (!vr.ok) {
            error.setText(vr.message);
            return;
        }
        onSave.accept(vr.hand);
        dispose();
    }

    private void onClickQuitar() {
        onClear.run();
        dispose();
    }
}
