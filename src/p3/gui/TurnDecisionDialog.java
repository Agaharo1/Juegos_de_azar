package p3.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import p3.logic.TurnDecisionLogic;
import p3.model.Hand;

public class TurnDecisionDialog extends JDialog {

    private final JTextField txtHero;
    private final JTextField txtVillainRange;
    private final JTextField txtBoard;
    private final JTextField txtEM;

    private final JLabel lblOuts;
    private final JLabel lblDecision;

    private final CardsViewPanel cardsView;

    /**
     * Constructor antiguo: modo totalmente manual (desde cero).
     * Lo mantengo por compatibilidad.
     */
    public TurnDecisionDialog(Frame owner) {
        this(owner, null, null);
    }

    /**
     * Nuevo constructor:
     * - heroHandCode: ej. "AhKh" o null si quieres modo manual desde cero
     * - board4: lista de 4 strings de carta (ej. ["Ad","Td","6c","Js"]), o null
     */
    public TurnDecisionDialog(Frame owner, String heroHandCode, List<String> board4) {
    	
        super(owner, "Decisión en el Turn: mano vs rango", false); // no modal
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UiTheme.BG_DARK);

        // ----------- PANEL FORMULARIO (NORTE) -----------
        UIManager.put("Label.foreground", Color.WHITE);
        
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.setBackground(UiTheme.BG_PANEL);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;

        // Hero hand
        gbc.gridx = 0;
        form.add(new JLabel("Hero Hand (ej: AhKh):"), gbc);
        gbc.gridx = 1;
        txtHero = new JTextField(heroHandCode != null ? heroHandCode : "");
        form.add(txtHero, gbc);

        // Villain range
        gbc.gridy++;
        gbc.gridx = 0;
        form.add(new JLabel("Villain Range (ej: AA,QQ+,AKs):"), gbc);
        gbc.gridx = 1;
        txtVillainRange = new JTextField("AA,QQ");
        form.add(txtVillainRange, gbc);

        // Board (4 cartas)
        gbc.gridy++;
        gbc.gridx = 0;
        form.add(new JLabel("Board Turn (4 cartas, ej: AdTd6cJs):"), gbc);
        gbc.gridx = 1;
        if (board4 != null && board4.size() == 4) {
            txtBoard = new JTextField(String.join("", board4));
        } else {
            txtBoard = new JTextField("");
        }
        form.add(txtBoard, gbc);

        // EM
        gbc.gridy++;
        gbc.gridx = 0;
        form.add(new JLabel("Equity mínima (EM) %:"), gbc);
        gbc.gridx = 1;
        txtEM = new JTextField("30"); // valor por defecto razonable
        form.add(txtEM, gbc);

        // Botón calcular
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton btnCalc = new JButton("Calcular outs y decisión");
        btnCalc.setFont(UiTheme.F_13B);
        btnCalc.addActionListener(e -> calculate());
        form.add(btnCalc, gbc);

        add(form, BorderLayout.NORTH);

        // ----------- PANEL CENTRAL (VISTA CARTAS + RESULTADOS) -----------
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(UiTheme.BG_DARK);
        center.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        cardsView = new CardsViewPanel();
        cardsView.setPreferredSize(new Dimension(600, 260));
        center.add(cardsView, BorderLayout.CENTER);

        JPanel results = new JPanel(new GridLayout(2, 1, 4, 4));
        results.setBackground(UiTheme.BG_PANEL);
        results.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblOuts = new JLabel("Outs: - (Eq: -)", JLabel.CENTER);
        lblOuts.setFont(UiTheme.F_13B);
        lblOuts.setForeground(Color.WHITE);

        lblDecision = new JLabel("Decisión: -", JLabel.CENTER);
        lblDecision.setFont(UiTheme.F_18B);
        lblDecision.setForeground(Color.WHITE);

        results.add(lblOuts);
        results.add(lblDecision);

        center.add(results, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        // Si venimos precargados de la mesa, pintamos la vista inicial
        updateCardsView();

        setMinimumSize(new Dimension(900, 650));
        setPreferredSize(new Dimension(1000, 700));
        pack();
        setLocationRelativeTo(owner);

    }

    private void calculate() {
        try {
            String heroText = txtHero.getText().trim();
            String boardText = txtBoard.getText().trim();
            String rangeText = txtVillainRange.getText().trim();
            String emText = txtEM.getText().trim().replace("%", "");

            if (heroText.length() != 4) {
                JOptionPane.showMessageDialog(this,
                        "La mano del Hero debe tener exactamente 4 caracteres (ej: AhKh).",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (boardText.length() != 8) {
                JOptionPane.showMessageDialog(this,
                        "El board en el Turn debe tener exactamente 4 cartas (8 caracteres).",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (rangeText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Introduce un rango para el villano (ej: AA,QQ+,AKs).",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double em = Double.parseDouble(emText);

            Hand hero = Hand.fromString(heroText);

            List<String> board = new ArrayList<>(4);
            board.add(boardText.substring(0, 2));
            board.add(boardText.substring(2, 4));
            board.add(boardText.substring(4, 6));
            board.add(boardText.substring(6, 8));

            // Llamada a la lógica mejorada
            TurnDecisionLogic.TurnDecisionResult res =
                    TurnDecisionLogic.evaluateDecision(hero, rangeText, board, em);

            // Actualizar resultados
            lblOuts.setText(String.format("Outs: %.2f (Eq: %.1f%%)", res.avgOuts, res.equityPercent));

            if (res.call) {
                lblDecision.setText("Decisión: CALL");
                lblDecision.setForeground(new Color(0, 180, 0));
            } else {
                lblDecision.setText("Decisión: FOLD");
                lblDecision.setForeground(new Color(200, 40, 40));
            }

            // Actualizar panel gráfico
            updateCardsView();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error en los datos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCardsView() {
        cardsView.setHeroCards(txtHero.getText().trim());
        cardsView.setBoard(txtBoard.getText().trim());
        cardsView.repaint();
    }

    // =========================
    //  Panel gráfico interno
    // =========================
    private static class CardsViewPanel extends JPanel {

        private String heroCards = "";
        private String boardCards = "";

        public CardsViewPanel() {
            setBackground(UiTheme.BG_CARD);
        }

        public void setHeroCards(String heroCards) {
            this.heroCards = (heroCards == null) ? "" : heroCards;
        }

        public void setBoard(String boardCards) {
            this.boardCards = (boardCards == null) ? "" : boardCards;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Tamaños proporcionados
            int cardW = (int)(w * 0.10);
            int cardH = (int)(h * 0.40);
            int spacing = (int)(cardW * 0.15);

            // --- BOARD ---
            if (boardCards != null && boardCards.length() >= 8) {
                int num = 4;
                int totalWidth = num * cardW + (num - 1) * spacing;
                int x = (w - totalWidth) / 2;
                int y = 20;

                for (int i = 0; i < num; i++) {
                    String c = boardCards.substring(i * 2, i * 2 + 2);
                    Image img = CardImages.get(c);
                    if (img != null) {
                        g2.drawImage(img, x + i * (cardW + spacing), y, cardW, cardH, this);
                    }
                }
            }

            // --- HERO (izquierda) ---
            if (heroCards != null && heroCards.length() >= 4) {
                String c1 = heroCards.substring(0, 2);
                String c2 = heroCards.substring(2, 4);

                Image img1 = CardImages.get(c1);
                Image img2 = CardImages.get(c2);

                int baseY = h - cardH - 40;
                int baseX = 40;

                if (img1 != null) g2.drawImage(img1, baseX, baseY, cardW, cardH, this);
                if (img2 != null) g2.drawImage(img2, baseX + (int)(cardW * 0.7), baseY, cardW, cardH, this);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                g2.drawString("HERO", baseX, baseY - 10);
            }

            // --- VILLAIN (derecha) ---
            Image back = CardImages.get("black_joker");
            if (back != null) {
                int baseY = h - cardH - 40;
                int baseX = w - cardW - (int)(cardW * 1.7);

                g2.drawImage(back, baseX, baseY, cardW, cardH, this);
                g2.drawImage(back, baseX + (int)(cardW * 0.7), baseY, cardW, cardH, this);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                g2.drawString("VILLAIN", baseX, baseY - 10);
            }
        }

    }
}
