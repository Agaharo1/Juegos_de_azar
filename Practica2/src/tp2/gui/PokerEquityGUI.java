package tp2.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Map;

import tp2.logic.Deck;
import tp2.logic.RangeParser;
import tp2.logic.EquityCalculator;
import tp2.model.GameState;
import tp2.model.Hand;

public class PokerEquityGUI extends JFrame {
    private JPanel mainPanel;
    private JPanel tablePanel;
    private JPanel controlPanel;

    private List<PlayerPanel> playerPanels;

    private Phase phase = Phase.PREFLOP;

    private Deck deck; // baraja de la mano actual
    private final EquityCalculator equityCalculator = new EquityCalculator();

    // Modelo
    private final GameState state = new GameState();

    // Botones
    private JButton btnDeal, btnFlop, btnTurn, btnRiver, btnReset, btnComprobar;

    // Controlador (único punto de entrada de eventos)
    private final Controller controller = new Controller();

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

                int ellipseW = (int)(w * 0.7);
                int ellipseH = (int)(h * 0.6);
                g2.setColor(UiTheme.BG_CARD);
                g2.fillOval(centerX - ellipseW / 2, centerY - ellipseH / 2, ellipseW, ellipseH);
                g2.setColor(UiTheme.TABLE_STROKE);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(centerX - ellipseW / 2, centerY - ellipseH / 2, ellipseW, ellipseH);

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

                String[] board = state.getBoard().raw();
                for (int i = 0; i < show; i++) {
                    drawCard(g2, startX + i * (cardW + spacing), y, board[i], cardW, cardH);
                }
            }
        };
        panel.setLayout(null);
        panel.setBackground(UiTheme.BG_DARK);

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

        if (code != null && !code.isEmpty()) {
            Image img = CardImages.get(code);
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
            PlayerPanel pp = new PlayerPanel(names[i], i == 4);
            playerPanels.add(pp);
            tablePanel.add(pp);
        }
    }

    private void positionPlayers() {
        int w = tablePanel.getWidth(), h = tablePanel.getHeight();
        if (w == 0 || h == 0) return;

        int centerX = w / 2, centerY = h / 2;
        int rx = (int)(w * 0.40), ry = (int)(h * 0.35);

        int panelW = 160, panelH = 200;
        double offset = Math.PI * 0.5;

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

        btnDeal  = createStyledButton("Deal");
        btnFlop  = createStyledButton("Flop");
        btnTurn  = createStyledButton("Turn");
        btnRiver = createStyledButton("River");
        btnReset = createStyledButton("Reset");
        btnComprobar = createStyledButton("Comprobar rango");

        // Uso de ActionCommand + un único controlador
        btnDeal.setActionCommand("DEAL");           btnDeal.addActionListener(controller);
        btnFlop.setActionCommand("FLOP");           btnFlop.addActionListener(controller);
        btnTurn.setActionCommand("TURN");           btnTurn.addActionListener(controller);
        btnRiver.setActionCommand("RIVER");         btnRiver.addActionListener(controller);
        btnReset.setActionCommand("RESET");         btnReset.addActionListener(controller);
        btnComprobar.setActionCommand("COMPROBAR"); btnComprobar.addActionListener(controller);

        buttonPanel.add(btnDeal);
        buttonPanel.add(btnFlop);
        buttonPanel.add(btnTurn);
        buttonPanel.add(btnRiver);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnComprobar);

        updateButtonsState();

        panel.add(buttonPanel, BorderLayout.EAST);
        return panel;
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
       VISTA: helpers que el controlador usa
       ====================== */

    private void updateButtonsState() {
        if (btnDeal != null)  btnDeal.setEnabled(true);
        if (btnReset != null) btnReset.setEnabled(true);

        boolean hasDeck = (deck != null);

        if (btnFlop != null)  btnFlop.setEnabled(hasDeck && phase == Phase.PREFLOP);
        if (btnTurn != null)  btnTurn.setEnabled(hasDeck && phase == Phase.FLOP);
        if (btnRiver != null) btnRiver.setEnabled(hasDeck && phase == Phase.TURN);
    }

    private void updateEquities() {
        List<String> jugadores = new ArrayList<>();
        for (PlayerPanel pp : playerPanels) {
            jugadores.add(pp.getPlayerName());
        }
        List<String> board = state.getBoard().visible();
        Map<String, Double> equities = equityCalculator.calcularEquity(jugadores, board);

        for (int i = 0; i < playerPanels.size(); i++) {
            PlayerPanel pp = playerPanels.get(i);
            String name = jugadores.get(i);
            Double eq = equities.getOrDefault(name, 0.0);
            pp.setEquity(eq);
        }
    }

    /** =====================
     *  CONTROLADOR (MVC)
     *  ===================== */
    private final class Controller implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            switch (cmd) {
                case "DEAL"       -> repartirCartas();
                case "FLOP"       -> mostrarFlop();
                case "TURN"       -> mostrarTurn();
                case "RIVER"      -> mostrarRiver();
                case "RESET"      -> reset();
                case "COMPROBAR"  -> onComprobarRango();
            }
        }

        private void onComprobarRango() {
            String rango = JOptionPane.showInputDialog(PokerEquityGUI.this,
                    "Introduce un rango (por ejemplo: AA,KK,AKs,AQo):",
                    "Comprobar rango", JOptionPane.PLAIN_MESSAGE);

            if (rango != null && !rango.isEmpty()) {
                if (!RangeParser.isBasicFormat(rango)) {
                    JOptionPane.showMessageDialog(PokerEquityGUI.this,
                            "Formato no válido. Ej: AA,KK,AKs,AQo",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    List<String> manos = RangeParser.parse(rango);

                    // Elegimos una mano del rango y la convertimos a cartas concretas sin duplicar
                    Random rand = new Random();
                    String manoElegida = manos.get(rand.nextInt(manos.size()));
                    String cartasConcretas = generarCartasConcretasDesdeNotacion(manoElegida);

                    // Asignar al héroe (índice 4), actualizar modelo y purgar mazo
                    PlayerPanel heroPanel = playerPanels.get(4);
                    heroPanel.setCards(cartasConcretas);
                    state.setPlayerHand(4, Hand.fromString(cartasConcretas));

                    if (deck != null) {
                        deck.removeCards(state.allUsedCards()); // evita que esas cartas salgan en el board
                    }

                    // Recalcular equities
                    updateEquities();

                    JOptionPane.showMessageDialog(PokerEquityGUI.this,
                            "Mano asignada al héroe: " + manoElegida + " (" + cartasConcretas + ")",
                            "Rango aplicado",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PokerEquityGUI.this,
                            "Error al analizar el rango: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        /**
         * Genera dos cartas concretas a partir de notación de rango (AA, AKs, AKo, TT, etc.),
         * evitando duplicados con respecto a state.allUsedCards().
         */
        private String generarCartasConcretasDesdeNotacion(String notacion) {
            String[] palos = {"h", "d", "c", "s"};
            Random rand = new Random();
            String n = notacion.toUpperCase(Locale.ROOT);

            // Elimina sufijo S/O para obtener los dos rangos base
            String base = n.replaceAll("[SO]$", "");
            if (base.length() != 2) {
                throw new IllegalArgumentException("Notación inválida: " + notacion);
            }
            char r1 = base.charAt(0);
            char r2 = base.charAt(1);

            // Cartas ya usadas (manos + board)
            Set<String> used = new HashSet<>(state.allUsedCards());

            // Intentamos varias combinaciones hasta encontrar una válida
            for (int intentos = 0; intentos < 100; intentos++) {
                String c1, c2;
                if (n.endsWith("S")) { // suited
                    String p = palos[rand.nextInt(4)];
                    c1 = "" + r1 + p;
                    c2 = "" + r2 + p;
                } else if (n.endsWith("O")) { // offsuit
                    String p1 = palos[rand.nextInt(4)], p2;
                    do { p2 = palos[rand.nextInt(4)]; } while (p1.equals(p2));
                    c1 = "" + r1 + p1;
                    c2 = "" + r2 + p2;
                } else { // pareja (sin sufijo)
                    String p1 = palos[rand.nextInt(4)], p2;
                    do { p2 = palos[rand.nextInt(4)]; } while (p1.equals(p2));
                    c1 = "" + r1 + p1;
                    c2 = "" + r2 + p2;
                }

                if (!c1.equals(c2) && !used.contains(c1) && !used.contains(c2)) {
                    return c1 + c2;
                }
            }
            throw new IllegalStateException("No se pudo generar una combinación válida sin duplicados.");
        }

        /* ======================
           LÓGICA CON DECK + PHASE + GAMESTATE
           ====================== */

        private void repartirCartas() {
            deck = new Deck();
            state.reset();
            deck.removeCards(state.allUsedCards());

            for (int i = 0; i < playerPanels.size(); i++) {
                PlayerPanel pp = playerPanels.get(i);
                String c1 = drawUnique();
                String c2 = drawUnique();
                pp.setCards(c1 + c2);
                state.setPlayerHand(i, new Hand(c1, c2));
            }

            phase = Phase.PREFLOP;
            state.setPhase(phase);
            tablePanel.repaint();

            updateButtonsState();
            updateEquities();
        }

        private void mostrarFlop() {
            if (deck == null) return;
            if (phase == Phase.PREFLOP) {
                deck.removeCards(state.allUsedCards());

                String c1 = drawUnique();
                String c2 = drawUnique();
                String c3 = drawUnique();
                state.getBoard().setFlop(c1, c2, c3);

                phase = Phase.FLOP;
                state.setPhase(phase);
                tablePanel.repaint();
                updateButtonsState();
                updateEquities();
            }
        }

        private void mostrarTurn() {
            if (deck == null) return;
            if (phase == Phase.FLOP) {
                deck.removeCards(state.allUsedCards());

                String c4 = drawUnique();
                state.getBoard().setTurn(c4);

                phase = Phase.TURN;
                state.setPhase(phase);
                tablePanel.repaint();
                updateButtonsState();
                updateEquities();
            }
        }

        private void mostrarRiver() {
            if (deck == null) return;
            if (phase == Phase.TURN) {
                deck.removeCards(state.allUsedCards());

                String c5 = drawUnique();
                state.getBoard().setRiver(c5);

                phase = Phase.RIVER;
                state.setPhase(phase);
                tablePanel.repaint();
                updateButtonsState();
                updateEquities();
            }
        }

        private void reset() {
            phase = Phase.PREFLOP;
            state.reset();
            deck = null;

            tablePanel.repaint();
            for (PlayerPanel pp : playerPanels) pp.reset();

            updateButtonsState();
        }

        /** Roba del mazo garantizando que no devuelva cartas ya usadas según GameState. */
        private String drawUnique() {
            Set<String> used = new HashSet<>(state.allUsedCards());
            String c;
            do { c = deck.draw(); } while (used.contains(c));
            return c;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PokerEquityGUI::new);
    }
}
