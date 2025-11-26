package p3.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import p3.logic.TurnDecisionLogic;
import p3.model.Hand;

public class TurnDecisionDialog extends JDialog {

    private JTextField txtHero, txtVillainRange, txtBoard, txtEM;
    private JLabel lblOuts, lblDecision;
    
    public TurnDecisionDialog(Frame owner) {
        super(owner, "Decisión Turn: Hero vs Villano", false); // No modal para poder ver la mesa
        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        form.add(new JLabel("Hero Hand (ej: AhKh):"));
        txtHero = new JTextField("AhKh");
        form.add(txtHero);

        form.add(new JLabel("Villain Range (ej: QQ+,AKs):"));
        txtVillainRange = new JTextField("AA,QQ");
        form.add(txtVillainRange);

        form.add(new JLabel("Board (4 cartas, ej: AdTd6cJs):"));
        txtBoard = new JTextField("AdTd6cJs");
        form.add(txtBoard);
        
        form.add(new JLabel("Equity Mínimo (EM) %:"));
        txtEM = new JTextField("30"); // Ejemplo típico del PDF
        form.add(txtEM);

        JButton btnCalc = new JButton("Calcular Outs y Decisión");
        btnCalc.addActionListener(e -> calculate());
        
        form.add(btnCalc);

        add(form, BorderLayout.NORTH);

        JPanel results = new JPanel(new GridLayout(2, 1));
        results.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        lblOuts = new JLabel("Media Outs: -");
        lblOuts.setFont(UiTheme.F_13B);
        lblDecision = new JLabel("Decisión: -");
        lblDecision.setFont(UiTheme.F_18B);
        
        results.add(lblOuts);
        results.add(lblDecision);
        
        add(results, BorderLayout.CENTER);
    }

    private void calculate() {
        try {
            Hand hero = Hand.fromString(txtHero.getText().trim());
            String range = txtVillainRange.getText().trim();
            
            String bText = txtBoard.getText().trim();
            if (bText.length() != 8) {
                JOptionPane.showMessageDialog(this, "El board debe tener 4 cartas (8 caracteres).");
                return;
            }
            List<String> board = new ArrayList<>();
            board.add(bText.substring(0, 2));
            board.add(bText.substring(2, 4));
            board.add(bText.substring(4, 6));
            board.add(bText.substring(6, 8));
            
            double emPct = Double.parseDouble(txtEM.getText().trim());

            // 1) Media de outs contra el rango del villano
            double avgOuts = TurnDecisionLogic.calculateAverageOuts(hero, range, board);

            // 2) Convertir EM% en "outs objetivo" (44 cartas posibles en river)
            double minOuts = (emPct / 100.0) * 44.0;

            // 3) Equity aproximada a partir de las outs (para mostrar al usuario)
            double equityEstimada = (avgOuts / 44.0) * 100.0;

            lblOuts.setText(String.format(
                    "Outs media: %.2f  |  Eq≈ %.1f%% (EM %.1f%% → %.2f outs)",
                    avgOuts, equityEstimada, emPct, minOuts));

            // Decisión: CALL si la media de outs supera el equivalente a EM%
            if (avgOuts >= minOuts) {
                lblDecision.setText("CALL");
                lblDecision.setForeground(Color.GREEN.darker());
            } else {
                lblDecision.setText("FOLD");
                lblDecision.setForeground(Color.RED.darker());
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en datos: " + ex.getMessage());
        }
    }
}
