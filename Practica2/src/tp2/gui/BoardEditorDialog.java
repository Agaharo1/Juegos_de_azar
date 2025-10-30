package tp2.gui;

import tp2.model.*;
import tp2.logic.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class BoardEditorDialog extends JDialog {
    private final GameState state;
    private final Deck deck;

    private JTextField flop1, flop2, flop3, turn, river;
    private JLabel errorLabel;
    private boolean saved = false;

    public BoardEditorDialog(Frame owner, GameState state, Deck deck) {
        super(owner, "Editar Board", true);
        this.state = state;
        this.deck = deck;
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(400, 300);
        setLocationRelativeTo(getOwner());

        JPanel fields = new JPanel(new GridLayout(6, 2, 8, 8));
        fields.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        fields.add(new JLabel("Flop 1:")); flop1 = new JTextField(); fields.add(flop1);
        fields.add(new JLabel("Flop 2:")); flop2 = new JTextField(); fields.add(flop2);
        fields.add(new JLabel("Flop 3:")); flop3 = new JTextField(); fields.add(flop3);
        fields.add(new JLabel("Turn:"));   turn  = new JTextField(); fields.add(turn);
        fields.add(new JLabel("River:"));  river = new JTextField(); fields.add(river);

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        fields.add(errorLabel);

        add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Guardar");
        JButton btnClear = new JButton("Limpiar");
        JButton btnCancel = new JButton("Cancelar");
        btnSave.addActionListener(e -> onSave());
        btnClear.addActionListener(e -> onClear());
        btnCancel.addActionListener(e -> dispose());
        buttons.add(btnClear);
        buttons.add(btnCancel);
        buttons.add(btnSave);
        add(buttons, BorderLayout.SOUTH);
    }

    private void onSave() {
        java.util.List<String> entered = new ArrayList<>();
        String[] inputs = {
            flop1.getText().trim(),
            flop2.getText().trim(),
            flop3.getText().trim(),
            turn.getText().trim(),
            river.getText().trim()
        };

        for (String c : inputs) {
            if (!c.isEmpty()) {
                if (!CardValidator.isValidCode(c)) { showError("Formato inválido: " + c); return; }
                if (entered.contains(c))          { showError("Cartas duplicadas: " + c); return; }
                entered.add(c);
            }
        }

        Set<String> used = new HashSet<>(state.allUsedCards());
        for (String c : entered) {
            if (used.contains(c)) { showError("Esa carta ya está en uso: " + c); return; }
        }

        Board board = state.getBoard();
        board.clear();
        if (entered.size() >= 3) board.setFlop(inputs[0], inputs[1], inputs[2]);
        if (entered.size() >= 4) board.setTurn(inputs[3]);
        if (entered.size() >= 5) board.setRiver(inputs[4]);

        int n = entered.size();
        if (n >= 5) state.setPhase(Phase.RIVER);
        else if (n == 4) state.setPhase(Phase.TURN);
        else if (n >= 3) state.setPhase(Phase.FLOP);
        else             state.setPhase(Phase.PREFLOP);

        if (deck != null) deck.removeCards(state.allUsedCards());

        saved = true;
        dispose();
    }

    private void onClear() {
        flop1.setText(""); flop2.setText(""); flop3.setText("");
        turn.setText(""); river.setText(""); errorLabel.setText(" ");
    }

    private void showError(String msg) { errorLabel.setText(msg); }
    public boolean isSaved() { return saved; }
}
