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
        txtEM = new JTextField("30"); // Ejemplo del PDF
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
            
            double em = Double.parseDouble(txtEM.getText().trim());

            // LLAMADA A LA LÓGICA (Issue #36)
            double avgOuts = TurnDecisionLogic.calculateAverageOuts(hero, range, board);
            
            lblOuts.setText(String.format("Media Outs: %.2f", avgOuts));

            // Decisión: Call si Media > EM (Nota: el enunciado dice "si la media obtenida es mayor al EM... hero hará Call" [cite: 290, 302])
            // Pero ojo: EM suele ser porcentaje (30%) y outs es número (4). 
            // El ejemplo del PDF [cite: 302] compara outs directas vs un % de EM convertido a outs? 
            // No, dice: "EM 30%... media 35.71 outs... debería hacer Call".
            // CUIDADO: 35.71 outs es imposible (max 44 o 46). 
            // El ejemplo del PDF [cite: 302] dice: "Media = 35.71 outs" (probablemente se refiere a equity % o outs ponderadas).
            // Sin embargo, para la práctica, asumiremos que si el usuario mete EM como %, debemos convertir las outs a %.
            // Equity aprox = (Outs * 2) + 2 (regla 4 y 2) o simplemente (Outs / 44 cartas) * 100.
            
            double equityEstimada = (avgOuts / 44.0) * 100; 
            
            // Ajustamos la etiqueta para mostrar ambos datos
            lblOuts.setText(String.format("Outs: %.2f (Eq: %.1f%%)", avgOuts, equityEstimada));
            
            if (equityEstimada >= em) {
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
