package p3.gui;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import p3.logic.Deck;
import p3.model.Board;
import p3.model.CardValidator;
import p3.model.GameState;

public class BoardEditorDialog extends JDialog {

    private final GameState state;
    private final Deck deck;
    private final Phase phase;

    private JTextField flop1, flop2, flop3, turn, river;
    private JLabel errorLabel;
    private boolean saved = false;

    public BoardEditorDialog(Frame owner, GameState state, Deck deck) {
        super(owner, "Editar Board", true);
        this.state = state;
        this.deck = deck;
        this.phase = state.getPhase();
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(400, 250);
        setLocationRelativeTo(getOwner());

        JPanel fields = new JPanel(new GridLayout(6, 2, 8, 8));
        fields.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Board board = state.getBoard();
        String[] raw = board.raw();

        flop1 = createField(fields, "Flop 1:", raw[0], phase == Phase.PREFLOP);
        flop2 = createField(fields, "Flop 2:", raw[1], phase == Phase.PREFLOP);
        flop3 = createField(fields, "Flop 3:", raw[2], phase == Phase.PREFLOP);
        turn  = createField(fields, "Turn:",  raw[3], phase == Phase.FLOP);
        river = createField(fields, "River:", raw[4], phase == Phase.TURN);

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        fields.add(errorLabel);

        add(fields, BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);
    }

    private JTextField createField(JPanel panel, String label, String value, boolean editable) {
        panel.add(new JLabel(label));
        JTextField field = new JTextField(value);
        field.setEditable(editable);
        panel.add(field);
        return field;
    }

    private JPanel createButtons() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Guardar");
        JButton btnCancel = new JButton("Cancelar");

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        buttons.add(btnCancel);
        buttons.add(btnSave);
        return buttons;
    }

    private void onSave() {
        Board b = state.getBoard();

        switch (phase) {
            case PREFLOP -> {
                if (!validateFlop()) return;
                b.setFlop(flop1.getText().trim(), flop2.getText().trim(), flop3.getText().trim());
                state.setPhase(Phase.FLOP);
            }
            case FLOP -> {
                if (!validateTurn()) return;
                b.setTurn(turn.getText().trim());
                state.setPhase(Phase.TURN);
            }
            case TURN -> {
                if (!validateRiver()) return;
                b.setRiver(river.getText().trim());
                state.setPhase(Phase.RIVER);
            }
            default -> { /* Nada editable */ }
        }

        deck.removeCards(state.allUsedCards());
        saved = true;
        dispose();
    }

    // ----------------------- VALIDACIONES -----------------------

    private boolean validateFlop() {
        String c1 = flop1.getText().trim();
        String c2 = flop2.getText().trim();
        String c3 = flop3.getText().trim();

        if (!validateSingle(c1) || !validateSingle(c2) || !validateSingle(c3))
            return false;

        if (new HashSet<>(Arrays.asList(c1, c2, c3)).size() != 3) {
            showError("Flop no puede tener duplicados");
            return false;
        }
        return true;
    }

    private boolean validateTurn() {
        return validateSingle(turn.getText().trim());
    }

    private boolean validateRiver() {
        return validateSingle(river.getText().trim());
    }

    private boolean validateSingle(String code) {
        if (code == null || code.isBlank()) {
            showError("Campo vacío");
            return false;
        }
        if (!CardValidator.isValidCode(code)) {
            showError("Formato inválido: " + code);
            return false;
        }

        // Evitar duplicados con otras cartas
        Set<String> used = new HashSet<>(state.allUsedCards());
        used.removeAll(state.getBoard().visible());  // permite editar las mismas del board actual

        if (used.contains(code)) {
            showError("Carta ya usada: " + code);
            return false;
        }
        return true;
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    public boolean isSaved() {
        return saved;
    }
}
