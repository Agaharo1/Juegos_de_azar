package tp2.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tp2.logic.Deck;
import tp2.logic.RangeParser;
import tp2.logic.EquityCalculator;

public class PokerEquityGUI extends JFrame {
    private JPanel mainPanel;
    private JPanel tablePanel;
    private JPanel controlPanel;

    private List<PlayerPanel> playerPanels;

    private String[] boardCards = {"", "", "", "", ""};
    private Phase phase = Phase.PREFLOP;

    private Deck deck; // baraja de la mano actual
    private final EquityCalculator equityCalculator = new EquityCalculator();

    // Botones
    private JButton btnDeal, btnFlop, btnTurn, btnRiver, btnReset, btnComprobar;

    public PokerEquityGUI() {
        setTitle("Poker Equity Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setResizable(true);
        setBackground(UiTheme.BG_DARK);

        initializeComponents();
        setVisible(true);
    }

    private void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(UiTheme.BG_DARK);

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

                // Mesa
                int ellipseW = (int)(w * 0.7);
                int ellipseH = (int)(h * 0.6);
                g2.setColor(UiTheme.BG_CARD);
                g2.fillOval(centerX - ellipseW / 2, centerY - ellipseH / 2, ellipseW, ellipseH);
                g2.setColor(UiTheme.TABLE_STROKE);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(centerX - ellipseW / 2, centerY - ellipseH / 2, ellipseW, ellipseH);

                // Cuántas cartas enseñar según la fase
                int show = switch (phase) {
                    case FLOP  -> 3;
                    case TURN  -> 4;
                    case RIVER -> 5;
                    default    -> 0;
                };

                int cardW = 95, cardH = 140, spacing = 30;
                int totalWidth = show * cardW + (show - 1) * spacing;
                int startX = centerX - totalWidth / 2;
                int y = centerY - cardH / 2;

                for (int i = 0; i < show; i++) {
                    drawCard(g2, startX + i * (cardW + spacing), y, boardCards[i], cardW, cardH);
                }
            }
        };
        panel.setLayout(null);
        panel.setBackground(UiTheme.BG_DARK);

        createPlayerPanels(panel);
        return panel;
    }

    private void drawCard(Graphics2D g, int x, int y, String code, int w, int h) {
        // sombra + base carta
        g.setColor(new Color(50, 50, 50));
        g.fillRect(x + 2, y + 2, w, h);
        g.setColor(new Color(240, 240, 240));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(100, 100, 100));
        g.drawRect(x, y, w, h);

        if (!code.isEmpty()) {
            Image img = CardImages.get(code); // caché
            if (img != null) g.drawImage(img, x, y, w, h, this);
        }
    }

    private void createPlayerPanels(JPanel tablePanel) {
        tablePanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                positionPlayers();
            }
        });

        playerPanels = new ArrayList<>();
        String[] names = {"Player 1", "Player 2", "Player 3", "Player 4", "Player 5", "Player 6"};
        for (int i = 0; i < 6; i++) {
            PlayerPanel pp = new PlayerPanel(names[i], i == 4); // el 5º es el héroe
            playerPanels.add(pp);
            tablePanel.add(pp);
        }
    }

    // Distribución uniforme por ángulo
    private void positionPlayers() {
        int w = tablePanel.getWidth(), h = tablePanel.getHeight();
        if (w == 0 || h == 0) return;

        int centerX = w / 2, centerY = h / 2;
        int rx = (int)(w * 0.40), ry = (int)(h * 0.35);

        int panelW = 160, panelH = 200;
        double offset = Math.PI * 0.5; // gira la distribución (empieza arriba)

        for (int i = 0; i < playerPanels.size(); i++) {
            double ang = offset + (2 * Math.PI * i / playerPanels.size());
            int x = (int)(centerX + rx * Math.cos(ang)) - panelW / 2;
            int y = (int)(centerY + ry * Math.sin(ang)) - panelH / 2;
            playerPanels.get(i).setBounds(x, y, panelW, panelH);
        }
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(15, 10));
        panel.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(UiTheme.BORDER, 2),
            new EmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(UiTheme.BG_PANEL);

        JPanel heroControlPanel = new HeroPanel();
        panel.add(heroControlPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(UiTheme.BG_PANEL);

        // Botones (referencias guardadas)
        btnDeal  = createStyledButton("Deal");
        btnFlop  = createStyledButton("Flop");
        btnTurn  = createStyledButton("Turn");
        btnRiver = createStyledButton("River");
        btnReset = createStyledButton("Reset");
        btnComprobar = createStyledButton("Comprobar rango");

        btnDeal.addActionListener(e -> repartirCartas());
        btnFlop.addActionListener(e -> mostrarFlop());
        btnTurn.addActionListener(e -> mostrarTurn());
        btnRiver.addActionListener(e -> mostrarRiver());
        btnReset.addActionListener(e -> reset());
        btnComprobar.addActionListener(e -> onComprobarRango());

        buttonPanel.add(btnDeal);
        buttonPanel.add(btnFlop);
        buttonPanel.add(btnTurn);
        buttonPanel.add(btnRiver);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnComprobar);

        // Estado inicial de botones
        updateButtonsState();

        panel.add(buttonPanel, BorderLayout.EAST);
        return panel;
    }

    // Mostrar diálogo y usar RangeParser con validación
    private void onComprobarRango() {
        String rango = JOptionPane.showInputDialog(this,
                "Introduce un rango (por ejemplo: AA,KK,AKs,AQo):",
                "Comprobar rango", JOptionPane.PLAIN_MESSAGE);

        if (rango != null && !rango.isEmpty()) {
            if (!RangeParser.isBasicFormat(rango)) {
                JOptionPane.showMessageDialog(this,
                        "Formato no válido. Ej: AA,KK,AKs,AQo",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
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
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UiTheme.F_13B);
        btn.setPreferredSize(new Dimension(100, 40));
        btn.setBackground(UiTheme.ACCENT_PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(UiTheme.ACCENT_PRIMARY_HOV); }
            @Override
            public void mouseExited(MouseEvent e)  { btn.setBackground(UiTheme.ACCENT_PRIMARY); }
        });

        return btn;
    }

    /* ======================
       LÓGICA CON DECK + PHASE + EQUITY
       ====================== */

    private void repartirCartas() {
        deck = new Deck(); // baraja nueva y mezclada

        for (PlayerPanel pp : playerPanels) {
            String c1 = deck.draw();
            String c2 = deck.draw();
            pp.setCards(c1 + c2);
        }
        boardCards = new String[]{"", "", "", "", ""};
        phase = Phase.PREFLOP; // al empezar la mano
        tablePanel.repaint();

        updateButtonsState();
        updateEquities(); // recalcular con nuevas hole cards (board vacío)
    }

    private void mostrarFlop() {
        if (deck == null) return;
        if (phase == Phase.PREFLOP) {
            boardCards[0] = deck.draw();
            boardCards[1] = deck.draw();
            boardCards[2] = deck.draw();
            phase = Phase.FLOP;
            tablePanel.repaint();
            updateButtonsState();
            updateEquities();
        }
    }

    private void mostrarTurn() {
        if (deck == null) return;
        if (phase == Phase.FLOP) {
            boardCards[3] = deck.draw();
            phase = Phase.TURN;
            tablePanel.repaint();
            updateButtonsState();
            updateEquities();
        }
    }

    private void mostrarRiver() {
        if (deck == null) return;
        if (phase == Phase.TURN) {
            boardCards[4] = deck.draw();
            phase = Phase.RIVER;
            tablePanel.repaint();
            updateButtonsState();
            updateEquities();
        }
    }

    private void reset() {
        phase = Phase.PREFLOP;
        deck = null; // descartar baraja actual
        boardCards = new String[]{"", "", "", "", ""};
        tablePanel.repaint();
        for (PlayerPanel pp : playerPanels) pp.reset();

        updateButtonsState();
    }

    // Habilita/inhabilita según fase (y si hay baraja)
    private void updateButtonsState() {
        if (btnDeal != null)  btnDeal.setEnabled(true);
        if (btnReset != null) btnReset.setEnabled(true);

        boolean hasDeck = (deck != null);

        if (btnFlop != null)  btnFlop.setEnabled(hasDeck && phase == Phase.PREFLOP);
        if (btnTurn != null)  btnTurn.setEnabled(hasDeck && phase == Phase.FLOP);
        if (btnRiver != null) btnRiver.setEnabled(hasDeck && phase == Phase.TURN);
    }

    // ========== NUEVO: Cálculo y refresco de equities ==========
    private void updateEquities() {
        // Lista de jugadores (por nombre) en el mismo orden que playerPanels
        List<String> jugadores = new ArrayList<>();
        for (PlayerPanel pp : playerPanels) {
            jugadores.add(pp.getPlayerName());
        }

        // Board visible actual como lista (solo posiciones no vacías)
        List<String> board = new ArrayList<>();
        for (String bc : boardCards) {
            if (bc != null && !bc.isEmpty()) board.add(bc);
        }

        // Calcular
        Map<String, Double> equities = equityCalculator.calcularEquity(jugadores, board);

        // Asignar a cada panel por orden
        for (int i = 0; i < playerPanels.size(); i++) {
            PlayerPanel pp = playerPanels.get(i);
            String name = jugadores.get(i);
            Double eq = equities.getOrDefault(name, 0.0);
            pp.setEquity(eq);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PokerEquityGUI::new);
    }
}
