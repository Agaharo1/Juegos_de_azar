package GUI;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import poker.RangeParser; //Añadido por ahora para que funcione el RRangeParser, no se si está bien


public class PokerEquityGUI extends JFrame {
    private JPanel mainPanel;
    private JPanel tablePanel;
    private JPanel controlPanel;

    private List<PlayerPanel> playerPanels;

    private String[] boardCards = {"", "", "", "", ""};
    private int boardPhase = 0; // 0=Preflop, 1=Flop, 2=Turn, 3=River

    public PokerEquityGUI() {
        setTitle("Poker Equity Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setResizable(true);
        setBackground(new Color(20, 30, 45));

        initializeComponents();
        setVisible(true);
    }

    private void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(20, 30, 45));

        tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int centerX = w / 2;
                int centerY = h / 2;

                // Dibujar mesa de poker
                int ellipseW = (int)(w * 0.7);
                int ellipseH = (int)(h * 0.6);
                g2.setColor(new Color(34, 60, 85));
                g2.fillOval(centerX - ellipseW / 2, centerY - ellipseH / 2, ellipseW, ellipseH);
                g2.setColor(new Color(100, 130, 160));
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(centerX - ellipseW / 2, centerY - ellipseH / 2, ellipseW, ellipseH);

                // Dibujar cartas del board
                int show = switch (boardPhase) {
                    case 1 -> 3;
                    case 2 -> 4;
                    case 3 -> 5;
                    default -> 0;
                };
                
                int cardW = 95;
                int cardH = 140;
                int spacing = 30;
                
                int totalWidth = show * cardW + (show - 1) * spacing;
                int startX = centerX - totalWidth / 2;
                int y = centerY - cardH / 2;

                for (int i = 0; i < show; i++) {
                    drawCard(g2, startX + i * (cardW + spacing), y, boardCards[i], cardW, cardH);
                }
            }
        };
        panel.setLayout(null);
        panel.setBackground(new Color(20, 30, 45));
        
        createPlayerPanels(panel);
        return panel;
    }

    private void drawCard(Graphics2D g, int x, int y, String code, int w, int h) {
        g.setColor(new Color(50, 50, 50));
        g.fillRect(x + 2, y + 2, w, h);
        g.setColor(new Color(240, 240, 240));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(100, 100, 100));
        g.drawRect(x, y, w, h);
        if (!code.isEmpty()) {
            Image img = loadCardImage(code);
            if (img != null) g.drawImage(img, x, y, w, h, this);
        }
    }

    private Image loadCardImage(String code) {
        String path = "resources/cartas/" + code + ".png";
        java.io.File file = new java.io.File(path);
        if (file.exists()) {
            return new ImageIcon(path).getImage();
        } else {
            return null;
        }
    }

    private void createPlayerPanels(JPanel tablePanel) {
        tablePanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                positionPlayers();
            }
        });
        
        playerPanels = new ArrayList<>();
        String[] names = {"Player 1", "Player 2", "Player 3", "Player 4", "Player 5", "Player 6"};
        for (int i = 0; i < 6; i++) {
            PlayerPanel pp = new PlayerPanel(names[i], i == 4);
            playerPanels.add(pp);
            tablePanel.add(pp);
        }
    }

    private void positionPlayers() {
        int w = tablePanel.getWidth();
        int h = tablePanel.getHeight();
        if (w == 0 || h == 0) return;

        int centerX = w / 2;
        int centerY = h / 2;
        int radiusX = (int)(w * 0.40);
        int radiusY = (int)(h * 0.35);

        int panelW = 160;
        int panelH = 200;

        double[] angles = {
            Math.PI * 1.33, Math.PI * 1.65, Math.PI * -1.0,
            Math.PI * -1.65, Math.PI * 0.0, Math.PI * -1.33
        };

        for (int i = 0; i < playerPanels.size(); i++) {
            int x = (int)(centerX + radiusX * Math.cos(angles[i]));
            int y = (int)(centerY + radiusY * Math.sin(angles[i]));
            playerPanels.get(i).setBounds(x - panelW / 2, y - panelH / 2, panelW, panelH);
        }
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(15, 10));
        panel.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 116, 139), 2),
            new EmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(new Color(30, 40, 55));

        JPanel heroControlPanel = new HeroPanel();
        panel.add(heroControlPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(new Color(30, 40, 55));

        String[] btnLabels = {"Deal", "Flop", "Turn", "River", "Reset"};
        Runnable[] actions = {
            this::repartirCartas,
            this::mostrarFlop,
            this::mostrarTurn,
            this::mostrarRiver,
            this::reset
        };

        for (int i = 0; i < btnLabels.length; i++) {
            JButton btn = createStyledButton(btnLabels[i]);
            final int idx = i;
            btn.addActionListener(e -> actions[idx].run());
            buttonPanel.add(btn);
        }
        
     // === BOTÓN NUEVO: Comprobar rango ===
        JButton btnComprobar = createStyledButton("Comprobar rango");
        btnComprobar.addActionListener(e -> {
            String rango = JOptionPane.showInputDialog(this,
                    "Introduce un rango (por ejemplo: AA,KK,AKs):",
                    "Comprobar rango", JOptionPane.PLAIN_MESSAGE);

            if (rango != null && !rango.isEmpty()) {
                try {
                    // Aquí he creado la clase RangeParser para que funcione
                    List<String> manos = RangeParser.parse(rango);

                    JOptionPane.showMessageDialog(this,
                            "Rango introducido:\n" + manos,
                            "Resultado del RangeParser",
                            JOptionPane.INFORMATION_MESSAGE);

                    System.out.println("Rango introducido: " + manos);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error al analizar el rango: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttonPanel.add(btnComprobar);


        panel.add(buttonPanel, BorderLayout.EAST);
        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(100, 40));
        btn.setBackground(new Color(59, 130, 246));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(37, 99, 235)); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(new Color(59, 130, 246)); }
        });

        return btn;
    }

    private void repartirCartas() {
        List<String> usedCards = new ArrayList<>();
        for (PlayerPanel pp : playerPanels) {
            String card1, card2;
            do { card1 = generateRandomCard(); } while (usedCards.contains(card1));
            usedCards.add(card1);

            do { card2 = generateRandomCard(); } while (usedCards.contains(card2));
            usedCards.add(card2);

            pp.setCards(card1 + card2);
        }
        boardCards = new String[]{"", "", "", "", ""};
        boardPhase = 0;
        tablePanel.repaint();
    }

    private String generateRandomCard() {
        String[] ranks = {"A","K","Q","J","T","9","8","7","6","5","4","3","2"};
        String[] suits = {"h","d","c","s"};
        return ranks[(int)(Math.random()*ranks.length)] + suits[(int)(Math.random()*suits.length)];
    }

    private void mostrarFlop() {
        if (boardPhase == 0) {
            List<String> usedCards = getAllUsedCards();
            
            for (int i = 0; i < 3; i++) {
                String card;
                do {
                    card = generateRandomCard();
                } while (usedCards.contains(card));
                usedCards.add(card);
                boardCards[i] = card;
            }
            boardPhase = 1;
            tablePanel.repaint();
        }
    }

    private void mostrarTurn() {
        if (boardPhase == 1) {
            List<String> usedCards = getAllUsedCards();
            
            String card;
            do {
                card = generateRandomCard();
            } while (usedCards.contains(card));
            boardCards[3] = card;
            boardPhase = 2;
            tablePanel.repaint();
        }
    }

    private void mostrarRiver() {
        if (boardPhase == 2) {
            List<String> usedCards = getAllUsedCards();
            
            String card;
            do {
                card = generateRandomCard();
            } while (usedCards.contains(card));
            boardCards[4] = card;
            boardPhase = 3;
            tablePanel.repaint();
        }
    }

    private List<String> getAllUsedCards() {
        List<String> usedCards = new ArrayList<>();
        
        for (PlayerPanel pp : playerPanels) {
            String cards = pp.getCards();
            if (cards.length() >= 4) {
                usedCards.add(cards.substring(0, 2));
                usedCards.add(cards.substring(2, 4));
            }
        }
        
        for (String boardCard : boardCards) {
            if (!boardCard.isEmpty()) {
                usedCards.add(boardCard);
            }
        }
        
        return usedCards;
    }

    private void reset() {
        boardPhase = 0;
        boardCards = new String[]{"", "", "", "", ""};
        tablePanel.repaint();
        for (PlayerPanel pp : playerPanels) pp.reset();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PokerEquityGUI::new);
    }
}