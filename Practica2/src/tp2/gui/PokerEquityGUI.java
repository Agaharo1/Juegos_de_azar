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
import tp2.logic.RankingProvider;
import tp2.logic.RealEquityCalculator;
import tp2.model.GameState;
import tp2.model.Hand;

/**
 * Ventana principal de la app: mesa + paneles de jugadores + barra inferior de control.
 * Mantiene el estado de la mano (GameState), un mazo (Deck) y usa RealEquityCalculator
 * para recalcular equities en cada calle.
 */
public class PokerEquityGUI extends JFrame {

    // Paneles principales
    private JPanel mainPanel;
    private JPanel tablePanel;
    private JPanel controlPanel;

    // 6 jugadores alrededor de la mesa (el héroe es el índice 4)
    private List<PlayerPanel> playerPanels;

    // Fase actual de la mano
    private Phase phase = Phase.PREFLOP;

    // Mazo actual (se crea al pulsar Deal)
    private Deck deck;

    // Calculador real (Monte Carlo)
    private final RealEquityCalculator realCalc = new RealEquityCalculator();

    // Modelo con manos/board/fase
    private final GameState state = new GameState();

    // Botones de control
    private JButton btnDeal, btnFlop, btnTurn, btnRiver, btnReset, btnComprobar;

    // Vista con controles del héroe (rango textual/porcentaje, aleatorio, etc.)
    private HeroPanel heroPanel;

    // Barra de estado
    private StatusBar statusBar;

    // Controlador central de eventos
    private final Controller controller = new Controller();

    /** Crea la ventana principal y arma la UI. */
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

    /** Construye paneles, barra de estado y engancha listeners. */
    private void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(UiTheme.BG_DARK);

        tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        statusBar = new StatusBar();
        mainPanel.add(statusBar, BorderLayout.NORTH);

        add(mainPanel);
    }

    /** Panel central que dibuja la mesa y (en el centro) el board de la fase actual. */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int centerX = w / 2, centerY = h / 2;

                // Óvalo de mesa
                int ellipseW = (int)(w * 0.7);
                int ellipseH = (int)(h * 0.6);
                g2.setColor(UiTheme.BG_CARD);
                g2.fillOval(centerX - ellipseW / 2, centerY - ellipseH / 2, ellipseW, ellipseH);
                g2.setColor(UiTheme.TABLE_STROKE);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(centerX - ellipseW / 2, centerY - ellipseH / 2, ellipseW, ellipseH);

                // Número de cartas visibles en el board según fase
                int show = switch (phase) {
                    case FLOP  -> 3;
                    case TURN  -> 4;
                    case RIVER -> 5;
                    default    -> 0;
                };

                // Dibuja el board
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

    /** Dibuja una carta rectangular (o su imagen PNG si existe). */
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

    /** Crea los 6 PlayerPanel y los posiciona alrededor del óvalo. */
    private void createPlayerPanels(JPanel tablePanel) {
        tablePanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                positionPlayers();
            }
        });

        playerPanels = new ArrayList<>();
        String[] names = {"Player 1", "Player 2", "Player 3", "Player 4", "Player 5 (YOU)", "Player 6"};
        for (int i = 0; i < 6; i++) {
            PlayerPanel pp = new PlayerPanel(names[i], i == 4);
            playerPanels.add(pp);
            tablePanel.add(pp);
        }
    }

    /** Reposiciona los PlayerPanel en una elipse al redimensionar. */
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

    /** Construye el panel inferior con el HeroPanel y los botones. */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(15, 10));
        panel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER, 2),
                new EmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(UiTheme.BG_PANEL);

        heroPanel = new HeroPanel();
        panel.add(heroPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(UiTheme.BG_PANEL);

        btnDeal  = createStyledButton("Deal");
        btnFlop  = createStyledButton("Flop");
        btnTurn  = createStyledButton("Turn");
        btnRiver = createStyledButton("River");
        btnReset = createStyledButton("Reset");
        btnComprobar = createStyledButton("Comprobar rango");

        // Mapea acciones al controlador
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

    /** Crea un botón estilizado consistente con el tema. */
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
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(UiTheme.ACCENT_PRIMARY_HOV); }
            @Override public void mouseExited (MouseEvent e) { btn.setBackground(UiTheme.ACCENT_PRIMARY);    }
        });
        return btn;
    }

    /** Habilita/inhabilita botones según exista mazo y fase actual. */
    private void updateButtonsState() {
        if (btnDeal != null)  btnDeal.setEnabled(true);
        if (btnReset != null) btnReset.setEnabled(true);

        boolean hasDeck = (deck != null);
        if (btnFlop  != null) btnFlop.setEnabled (hasDeck && phase == Phase.PREFLOP);
        if (btnTurn  != null) btnTurn.setEnabled (hasDeck && phase == Phase.FLOP);
        if (btnRiver != null) btnRiver.setEnabled(hasDeck && phase == Phase.TURN);
    }

    /**
     * Recalcula y muestra equities con RealEquityCalculator.
     * IMPORTANTE: antes de pedir manos, garantizamos que GameState tenga 6 posiciones
     * para alinear sizes con la lista de nombres (6 PlayerPanel).
     */
    private void updateEquities() {
        // Alinear tamaños (evita "names y hands deben tener misma longitud")
        state.ensurePlayersCount(playerPanels.size());

        // Nombres y manos actuales
        List<String> jugadores = new ArrayList<>(playerPanels.size());
        for (PlayerPanel pp : playerPanels) jugadores.add(pp.getPlayerName());

        List<Hand> manos = state.getPlayers();     // puede contener nulls
        List<String> board = state.getBoard().visible();

        // Nº de simulaciones según fase
        int trials = switch (phase) {
            case PREFLOP -> 5000;
            case FLOP    -> 15000;
            case TURN    -> 30000;
            case RIVER   -> 1; // determinista si todo está completo
        };

        // Semilla reproducible por estado visual
        String seedKey = String.join("-", jugadores) + "|" + manos + "|" + board + "|" + phase;
        long seed = seedKey.hashCode();

        Map<String, Double> equities = realCalc.calcularEquity(jugadores, manos, board, trials, seed);

        for (int i = 0; i < playerPanels.size(); i++) {
            PlayerPanel pp = playerPanels.get(i);
            String name = jugadores.get(i);
            Double eq = equities.getOrDefault(name, 0.0);
            pp.setEquity(eq);
        }
    }

    // =====================
    // CONTROLADOR (MVC)
    // =====================

    /**
     * Controlador único de acciones de botones.
     * Encadena la lógica de repartir, avanzar calles, reset y aplicar rango.
     */
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

        /**
         * Aplica un rango al héroe:
         * - Si está seleccionado "Textual", se valida y usa el texto del campo.
         * - Si está "Percentage", se toma el top por % del RankingProvider.
         * Luego elige aleatoriamente una combo concreta compatible con la notación
         * y la fija como mano del héroe (índice 4), recalculando equities.
         */
        private void onComprobarRango() {
            String rango;

            if (heroPanel.isTextualSelected()) {
                rango = heroPanel.getTextualRange();
                if (rango == null || rango.isBlank()) {
                    JOptionPane.showMessageDialog(PokerEquityGUI.this,
                            "Introduce un rango (ej: AA,KK,AKs,AQo).",
                            "Falta rango", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!RangeParser.isBasicFormat(rango)) {
                    JOptionPane.showMessageDialog(PokerEquityGUI.this,
                            "Formato no válido. Ej: AA,KK,AKs,AQo",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                int pct = heroPanel.getPercentage(); // 1..100
                List<String> top = RankingProvider.getTopByPercent(pct / 100.0);
                if (top.isEmpty()) {
                    JOptionPane.showMessageDialog(PokerEquityGUI.this,
                            "Porcentaje demasiado bajo.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                rango = String.join(",", top);
            }

            try {
                List<String> manos = tp2.logic.RangeParser.parse(rango);

                Random rand = new Random();
                String manoElegida = manos.get(rand.nextInt(manos.size()));
                String cartasConcretas = generarCartasConcretasDesdeNotacion(manoElegida);

                // Héroe (índice 4)
                PlayerPanel hero = playerPanels.get(4);
                hero.setCards(cartasConcretas);
                state.setPlayerHand(4, Hand.fromString(cartasConcretas));

                if (deck != null) deck.removeCards(state.allUsedCards());

                updateEquities();
                statusBar.setMessage("Héroe fijado desde rango.");
                statusBar.setRight("Mazo restante: " + (deck != null ? deck.remaining() : 0));

                JOptionPane.showMessageDialog(PokerEquityGUI.this,
                        "Héroe: " + manoElegida + " \u2192 " + cartasConcretas,
                        "Rango aplicado",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(PokerEquityGUI.this,
                        "Error al analizar/aplicar rango: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        /**
         * Genera dos cartas concretas a partir de notación de rango (AA, AKs, AKo, TT, etc.),
         * evitando duplicados respecto a state.allUsedCards().
         * Usa 'S' para suited y 'O' para offsuit (RangeParser normaliza a mayúsculas).
         */
        private String generarCartasConcretasDesdeNotacion(String notacion) {
            String[] palos = {"h", "d", "c", "s"};
            Random rand = new Random();
            String n = notacion.toUpperCase(Locale.ROOT);

            String base = n.replaceAll("[SO]$", "");
            if (base.length() != 2) {
                throw new IllegalArgumentException("Notación inválida: " + notacion);
            }
            char r1 = base.charAt(0);
            char r2 = base.charAt(1);

            Set<String> used = new HashSet<>(state.allUsedCards());

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
                } else { // pareja (o sin sufijo)
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

        /** Reparte 2 cartas a cada jugador (según Random Cards) y resetea fase a PREFLOP. */
        private void repartirCartas() {
            deck = new Deck();
            state.reset();
            state.ensurePlayersCount(playerPanels.size()); // garantiza 6 slots
            deck.removeCards(state.allUsedCards());

            for (int i = 0; i < playerPanels.size(); i++) {
                PlayerPanel pp = playerPanels.get(i);

                // Si el héroe tiene Random Cards desactivado, lo dejamos vacío
                if (i == 4 && !heroPanel.isRandomCards()) {
                    pp.setCards("");
                    state.setPlayerHand(i, null);
                    continue;
                }

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

            statusBar.setMessage("Cartas repartidas. Fase: PREFLOP");
            statusBar.setRight("Mazo restante: " + deck.remaining());
        }

        /** Saca flop aleatorio si Random Board está activo y estamos en PREFLOP. */
        private void mostrarFlop() {
            if (deck == null) return;
            if (!heroPanel.isRandomBoard()) {
                JOptionPane.showMessageDialog(PokerEquityGUI.this,
                        "Random Board desactivado. (UI para board manual pendiente)",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
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

                statusBar.setMessage("Mostrando FLOP");
                statusBar.setRight("Mazo restante: " + deck.remaining());
            }
        }

        /** Saca turn aleatorio si Random Board está activo y estamos en FLOP. */
        private void mostrarTurn() {
            if (deck == null) return;
            if (!heroPanel.isRandomBoard()) {
                JOptionPane.showMessageDialog(PokerEquityGUI.this,
                        "Random Board desactivado. (UI para board manual pendiente)",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (phase == Phase.FLOP) {
                deck.removeCards(state.allUsedCards());

                String c4 = drawUnique();
                state.getBoard().setTurn(c4);

                phase = Phase.TURN;
                state.setPhase(phase);
                tablePanel.repaint();
                updateButtonsState();
                updateEquities();

                statusBar.setMessage("Mostrando TURN");
                statusBar.setRight("Mazo restante: " + deck.remaining());
            }
        }

        /** Saca river aleatorio si Random Board está activo y estamos en TURN. */
        private void mostrarRiver() {
            if (deck == null) return;
            if (!heroPanel.isRandomBoard()) {
                JOptionPane.showMessageDialog(PokerEquityGUI.this,
                        "Random Board desactivado. (UI para board manual pendiente)",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (phase == Phase.TURN) {
                deck.removeCards(state.allUsedCards());

                String c5 = drawUnique();
                state.getBoard().setRiver(c5);

                phase = Phase.RIVER;
                state.setPhase(phase);
                tablePanel.repaint();
                updateButtonsState();
                updateEquities();

                statusBar.setMessage("Mostrando RIVER");
                statusBar.setRight("Mazo restante: " + deck.remaining());
            }
        }

        /** Limpia estado, cartas y botones a la fase inicial. */
        private void reset() {
            phase = Phase.PREFLOP;
            state.reset();
            state.ensurePlayersCount(playerPanels.size());
            deck = null;

            tablePanel.repaint();
            for (PlayerPanel pp : playerPanels) pp.reset();

            updateButtonsState();

            statusBar.setMessage("Reiniciado.");
            statusBar.setRight("");
        }

        /** Roba del mazo garantizando no repetir ninguna de las ya usadas en GameState. */
        private String drawUnique() {
            Set<String> used = new HashSet<>(state.allUsedCards());
            String c;
            do { c = deck.draw(); } while (used.contains(c));
            return c;
        }
    }

    /** Punto de entrada. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(PokerEquityGUI::new);
    }
}
