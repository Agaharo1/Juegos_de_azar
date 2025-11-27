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

    private JTextField flop1, flop2, flop3, turn, river;
    private JLabel errorLabel;
    private boolean saved = false;

    private final Phase phase;   // fase actual al abrir el editor

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

        // ----------- FLOP  -----------
        fields.add(new JLabel("Flop 1:"));
        flop1 = new JTextField(raw[0]);
        flop1.setEditable(phase == Phase.PREFLOP);   // editable solo antes del flop
        fields.add(flop1);

        fields.add(new JLabel("Flop 2:"));
        flop2 = new JTextField(raw[1]);
        flop2.setEditable(phase == Phase.PREFLOP);
        fields.add(flop2);

        fields.add(new JLabel("Flop 3:"));
        flop3 = new JTextField(raw[2]);
        flop3.setEditable(phase == Phase.PREFLOP);
        fields.add(flop3);

        // ----------- TURN  -----------
        fields.add(new JLabel("Turn:"));
        turn = new JTextField(raw[3]);
        turn.setEditable(phase == Phase.FLOP);       // editable solo cuando pulsas TURN
        fields.add(turn);

        // ----------- RIVER -----------
        fields.add(new JLabel("River:"));
        river = new JTextField(raw[4]);
        river.setEditable(phase == Phase.TURN);      // editable solo cuando pulsas RIVER
        fields.add(river);

        // —— ERROR —— 
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        fields.add(errorLabel);

        add(fields, BorderLayout.CENTER);

        // —— BOTONES ——
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Guardar");
        JButton btnCancel = new JButton("Cancelar");

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        buttons.add(btnCancel);
        buttons.add(btnSave);
        add(buttons, BorderLayout.SOUTH);
    }

    private void onSave() {

        Board b = state.getBoard();

        // ========== VALIDA SOLO CAMPOS EDITABLES ==========

        // FLOP
        if (phase == Phase.PREFLOP) {
            if (!validThree(flop1.getText(), flop2.getText(), flop3.getText()))
                return;

            b.setFlop(flop1.getText().trim(), flop2.getText().trim(), flop3.getText().trim());
            state.setPhase(Phase.FLOP);
        }

        // TURN
        if (phase == Phase.FLOP) {
            String c = turn.getText().trim();
            if (!validOne(c)) return;

            b.setTurn(c);
            state.setPhase(Phase.TURN);
        }

        // RIVER
        if (phase == Phase.TURN) {
            String c = river.getText().trim();
            if (!validOne(c)) return;

            b.setRiver(c);
            state.setPhase(Phase.RIVER);
        }

        deck.removeCards(state.allUsedCards());
        saved = true;
        dispose();
    }

    private boolean validThree(String c1, String c2, String c3) {
        if (!validOne(c1) || !validOne(c2) || !validOne(c3)) return false;

        Set<String> s = new HashSet<>(Arrays.asList(c1, c2, c3));
        if (s.size() != 3) {
            showError("Flop no puede tener duplicados");
            return false;
        }

        return true;
    }

    private boolean validOne(String c) {
        if (c == null || c.isBlank()) {
            showError("Campo vacío");
            return false;
        }
        if (!CardValidator.isValidCode(c)) {
            showError("Formato inválido: " + c);
            return false;
        }

        // Evitar duplicar cartas ya usadas (excepto las del mismo board)
        Set<String> used = new HashSet<>(state.allUsedCards());
        used.removeAll(state.getBoard().visible());

        if (used.contains(c)) {
            showError("Carta ya usada: " + c);
            return false;
        }

        return true;
    }

    private void showError(String msg) { errorLabel.setText(msg); }

    public boolean isSaved() { return saved; }
}
