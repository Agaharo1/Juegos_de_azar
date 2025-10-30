package tp2.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import tp2.logic.Deck;
import tp2.logic.RangeParser;
import tp2.logic.RankingProvider;
import tp2.logic.EquityCalculator;
import tp2.logic.PokerStoveEquityCalculator;

import tp2.model.GameState;
import tp2.model.Hand;

/**
 * Ventana principal.
 * - Dibuja la mesa y el board.
 * - Coloca 6 PlayerPanel con botón "Editar" por jugador.
 * - Permite editar Board cuando Random Board está OFF.
 * - Mantiene coherente GameState + Deck y recalcula equity.
 */
public class PokerEquityGUI extends JFrame {

    // Layout principal
    private JPanel mainPanel;
    private JPanel tablePanel;
    private JPanel controlPanel;

    // Jugadores
    private List<PlayerPanel> playerPanels;

    // Fase y mazo
    private Phase phase = Phase.PREFLOP;
    private Deck deck;

    // Cálculo de equity (PokerStove con fallback a Monte Carlo)
    private final EquityCalculator calc = new PokerStoveEquityCalculator();

    // Estado del juego
    private final GameState state = new GameState();

    // Controles inferiores
    private JButton btnDeal, btnFlop, btnTurn, btnRiver, btnReset, btnComprobar, btnEditBoard;
    private HeroPanel heroPanel;

    // Barra estado
    private StatusBar statusBar;

    // Controlador
    private final Controller controller = new Controller();
    
    // Colores para el feedback de rango
    private static final Color RANGE_IN_COLOR  = new Color(60, 180, 75); 
    private static final Color RANGE_OUT_COLOR = new Color(180, 60, 60); 
    private final Color NEUTRAL_COLOR = UiTheme.BG_INPUT; 

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

    // =========================
    //   Construcción de UI
    // =========================
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
    

    private JPanel createTablePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int centerX = w / 2, centerY = h / 2;

                // Mesa
                int ellipseW = (int)(w * 0.7);
                int ellipseH = (int)(h * 0.6);
                g2.setColor(UiTheme.BG_CARD);
                g2.fillOval(centerX - ellipseW / 2, centerY - ellipseH / 2, ellipseW, ellipseH);
                g2.setColor(UiTheme.TABLE_STROKE);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(centerX - ellipseW / 2, centerY - ellipseH / 2, ellipseW, ellipseH);

                // Nº de cartas visibles según fase
                int show = switch (phase) {
                    case FLOP  -> 3;
                    case TURN  -> 4;
                    case RIVER -> 5;
                    default    -> 0;
                };

                // Board
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
            @Override public void componentResized(ComponentEvent e) { positionPlayers(); }
        });

        playerPanels = new ArrayList<>();
        String[] names = {"Player 1", "Player 2", "Player 3", "Player 4", "Player 5", "Player 6"};
        for (int i = 0; i < 6; i++) {
            PlayerPanel pp = new PlayerPanel(names[i], i == 4);
            final int seat = i;
            pp.setOnEditHand(() -> abrirEditorMano(seat));
            pp.setOnClearHand(() -> quitarMano(seat));
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

        heroPanel = new HeroPanel();
        heroPanel.setRangeFeedbackColor(NEUTRAL_COLOR);
        heroPanel.addRandomBoardListener(e -> updateButtonsState());
        panel.add(heroPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(UiTheme.BG_PANEL);

        btnDeal       = createStyledButton("Deal");
        btnFlop       = createStyledButton("Flop");
        btnTurn       = createStyledButton("Turn");
        btnRiver      = createStyledButton("River");
        btnReset      = createStyledButton("Reset");
        btnComprobar  = createStyledButton("Comprobar rango");
        btnEditBoard = createStyledButton("Editar Board");
        btnEditBoard.setActionCommand("EDIT_BOARD");
        btnEditBoard.addActionListener(controller);
        buttonPanel.add(btnEditBoard);
        


        btnDeal.setActionCommand("DEAL");            btnDeal.addActionListener(controller);
        btnFlop.setActionCommand("FLOP");            btnFlop.addActionListener(controller);
        btnTurn.setActionCommand("TURN");            btnTurn.addActionListener(controller);
        btnRiver.setActionCommand("RIVER");          btnRiver.addActionListener(controller);
        btnReset.setActionCommand("RESET");          btnReset.addActionListener(controller);
        btnComprobar.setActionCommand("COMPROBAR");  btnComprobar.addActionListener(controller);
        btnEditBoard.setActionCommand("EDIT_BOARD"); btnEditBoard.addActionListener(controller);

        buttonPanel.add(btnDeal);
        buttonPanel.add(btnFlop);
        buttonPanel.add(btnTurn);
        buttonPanel.add(btnRiver);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnComprobar);
        buttonPanel.add(btnEditBoard);

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
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(UiTheme.ACCENT_PRIMARY_HOV); }
            @Override public void mouseExited (MouseEvent e) { btn.setBackground(UiTheme.ACCENT_PRIMARY);    }
        });
        return btn;
    }

    private void updateButtonsState() {
        if (btnDeal != null)  btnDeal.setEnabled(true);
        if (btnReset != null) btnReset.setEnabled(true);

        boolean hasDeck = (deck != null);
        if (btnFlop  != null) btnFlop.setEnabled (hasDeck && phase == Phase.PREFLOP);
        if (btnTurn  != null) btnTurn.setEnabled (hasDeck && phase == Phase.FLOP);
        if (btnRiver != null) btnRiver.setEnabled(hasDeck && phase == Phase.TURN);

        // Habilita "Editar Board" cuando hay mazo y Random Board está OFF
        if (btnEditBoard != null) btnEditBoard.setEnabled(hasDeck && !heroPanel.isRandomBoard());
    }


    // =========================
    //   Equity y utilidades
    // =========================
    private void updateEquities() {
        state.ensurePlayersCount(playerPanels.size());

        List<String> jugadores = new ArrayList<>(playerPanels.size());
        for (PlayerPanel pp : playerPanels) jugadores.add(pp.getPlayerName());

        List<Hand> manos = state.getPlayers();
        List<String> board = state.getBoard().visible();

        int trials = switch (phase) {
            case PREFLOP -> 5000;
            case FLOP    -> 15000;
            case TURN    -> 30000;
            case RIVER   -> 1;
        };

        String seedKey = String.join("-", jugadores) + "|" + manos + "|" + board + "|" + phase;
        long seed = seedKey.hashCode();

        Map<String, Double> equities = calc.calcularEquity(jugadores, manos, board, trials, seed);

        for (int i = 0; i < playerPanels.size(); i++) {
            PlayerPanel pp = playerPanels.get(i);
            String name = jugadores.get(i);
            Double eq = equities.getOrDefault(name, 0.0);
            pp.setEquity(eq);
        }
    }

    private void syncDeckAfterChange() {
        if (deck != null) {
            deck.removeCards(state.allUsedCards());
            statusBar.setRight("Mazo restante: " + deck.remaining());
        }
    }

    private Hand stateGetPlayerHand(int i) {
        try { return state.getPlayers().get(i); }
        catch (Exception e) { return null; }
    }

    private HandEditorDialog.ValidationResult validarManoContraEstado(String text, int seat) {
        String t = (text == null) ? "" : text.replaceAll("\\s+", "");
        if (t.length() != 4) {
            return HandEditorDialog.ValidationResult.error("Usa 4 caracteres: AhKd, 7c7d, etc.");
        }
        final Hand hand;
        try {
            hand = Hand.fromString(t);
        } catch (IllegalArgumentException ex) {
            return HandEditorDialog.ValidationResult.error(ex.getMessage());
        }

        // Evitar colisiones (excluye la mano previa del propio asiento)
        Set<String> usadas = new HashSet<>(state.allUsedCards());
        Hand previa = stateGetPlayerHand(seat);
        if (previa != null) {
            usadas.remove(previa.card1());
            usadas.remove(previa.card2());
        }
        if (usadas.contains(hand.card1()) || usadas.contains(hand.card2())) {
            return HandEditorDialog.ValidationResult.error("Carta ya usada en mesa u otro jugador.");
        }
        return HandEditorDialog.ValidationResult.ok(hand);
    }

    // =========================
    //   Lógica de Rango y Feedback
    // =========================

    // --- Helper: Convertir mano concreta (AhKd) a notación abreviada (AKo, AA, AJs) ---
    private String handToShorthand(Hand hand) {
        if (hand == null) return null;
        
        char r1 = hand.card1().charAt(0);
        char s1 = hand.card1().charAt(1);
        char r2 = hand.card2().charAt(0);
        char s2 = hand.card2().charAt(1);

        String ranks = "23456789TJQKA";
        int v1 = ranks.indexOf(r1);
        int v2 = ranks.indexOf(r2);
        
        // Obtener los rangos ordenados (high card primero)
        char highR = (v1 >= v2) ? r1 : r2;
        char lowR  = (v1 >= v2) ? r2 : r1;
        
        if (r1 == r2) { // Pareja
            return "" + r1 + r2; 
        }

        boolean suited = (s1 == s2);
        return "" + highR + lowR + (suited ? "s" : "o"); 
    }
    
    private Boolean isHandInRange(Hand hand) {
        if (hand == null) {
            return null; 
        }
        
        String handShorthand = handToShorthand(hand);
        if (handShorthand == null) return false;

        if (heroPanel.isTextualSelected()) {
            // Modo Rango Textual
            String rangeText = heroPanel.getTextualRange();
            List<String> rangeList = RangeParser.parse(rangeText); 
            return rangeList.contains(handShorthand);
            
        } else if (heroPanel.isPercentageSelected()) {
            
            int pct = heroPanel.getPercentage();
            boolean[][][] mask = RankingProvider.getMaskForPercent(pct / 100.0);
            
            
            char r1 = handShorthand.charAt(0);
            char r2 = handShorthand.charAt(1);
            boolean suited = handShorthand.endsWith("s");
            
            String ranks = "23456789TJQKA";
            int i1 = ranks.indexOf(r1);
            int i2 = ranks.indexOf(r2);
            
            if (i1 < 0 || i2 < 0) return false;

            int highIdx = Math.max(i1, i2);
            int lowIdx  = Math.min(i1, i2);
            int layer = suited ? 1 : 0;
            
            
            if (r1 == r2) {
                 return mask[i1][i1][0]; 
            }
            
            return mask[highIdx][lowIdx][layer];
        }
        
        return null; 
    }
    
    
    private void refreshRangeFeedback() {
        Hand heroHand = stateGetPlayerHand(4); 
        Boolean inRange = isHandInRange(heroHand);
        
        // 1. Actualiza el campo de texto en HeroPanel
        if (inRange == null) {
            heroPanel.setRangeFeedbackColor(NEUTRAL_COLOR);
        } else if (inRange) {
            heroPanel.setRangeFeedbackColor(RANGE_IN_COLOR);
        } else {
            heroPanel.setRangeFeedbackColor(RANGE_OUT_COLOR);
        }

        // 2. Actualiza el borde en PlayerPanel
        playerPanels.get(4).setRangeFeedback(inRange);
    }
    // =========================
    //   Edición de manos
    // =========================
    private void abrirEditorMano(int seat) {
        if (deck == null) {
            JOptionPane.showMessageDialog(this, "Primero reparte cartas (Deal).", "Sin mazo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Hand actual = stateGetPlayerHand(seat);
        String initial = (actual == null) ? "" : actual.toString();

        HandEditorDialog dlg = new HandEditorDialog(
                this,
                "Editar mano (Jugador " + (seat + 1) + ")",
                initial,
                input -> validarManoContraEstado(input, seat),
                hand -> {
                    state.setPlayerHand(seat, hand);
                    playerPanels.get(seat).setCards(hand.toString());
                    syncDeckAfterChange();
                    updateEquities();
                    if (seat == 4) refreshRangeFeedback();
                    tablePanel.repaint();
                    statusBar.setMessage("Mano fijada en jugador " + (seat + 1));
                },
                () -> {
                    state.setPlayerHand(seat, null);
                    playerPanels.get(seat).setCards("");
                    syncDeckAfterChange();
                    updateEquities();
                    if (seat == 4) refreshRangeFeedback();
                    tablePanel.repaint();
                    statusBar.setMessage("Mano quitada en jugador " + (seat + 1));
                }
        );
        dlg.setVisible(true);
    }

    private void quitarMano(int seat) {
        state.setPlayerHand(seat, null);
        playerPanels.get(seat).setCards("");
        syncDeckAfterChange();
        updateEquities();
        if (seat == 4) refreshRangeFeedback();
        tablePanel.repaint();
        statusBar.setMessage("Mano quitada en jugador " + (seat + 1));
    }
    

    // =========================
    //   Controlador de botones
    // =========================
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
                case "EDIT_BOARD" -> onEditarBoard();
            }
        }

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
                int pct = heroPanel.getPercentage();
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

                PlayerPanel hero = playerPanels.get(4);
                hero.setCards(cartasConcretas);
                state.setPlayerHand(4, Hand.fromString(cartasConcretas));

                if (deck != null) deck.removeCards(state.allUsedCards());

                updateEquities();
                refreshRangeFeedback();
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

        private String generarCartasConcretasDesdeNotacion(String notacion) {
            String[] palos = {"h", "d", "c", "s"};
            Random rand = new Random();
            String n = notacion.toUpperCase(Locale.ROOT);
            String base = n.replaceAll("[SO]$", "");
            if (base.length() != 2) throw new IllegalArgumentException("Notación inválida: " + notacion);
            char r1 = base.charAt(0);
            char r2 = base.charAt(1);

            Set<String> used = new HashSet<>(state.allUsedCards());

            for (int intentos = 0; intentos < 100; intentos++) {
                String c1, c2;
                if (n.endsWith("S")) {
                    String p = palos[rand.nextInt(4)];
                    c1 = "" + r1 + p;
                    c2 = "" + r2 + p;
                } else if (n.endsWith("O")) {
                    String p1 = palos[rand.nextInt(4)], p2;
                    do { p2 = palos[rand.nextInt(4)]; } while (p1.equals(p2));
                    c1 = "" + r1 + p1;
                    c2 = "" + r2 + p2;
                } else {
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

        private void repartirCartas() {
            deck = new Deck();
            state.reset();
            state.ensurePlayersCount(playerPanels.size());
            deck.removeCards(state.allUsedCards());

            for (int i = 0; i < playerPanels.size(); i++) {
                PlayerPanel pp = playerPanels.get(i);

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
            
            refreshRangeFeedback();

            statusBar.setMessage("Cartas repartidas. Fase: PREFLOP");
            statusBar.setRight("Mazo restante: " + deck.remaining());
        }

        private void mostrarFlop() {
            if (deck == null) return;

            if (!heroPanel.isRandomBoard()) {
                onEditarBoard(); // abre el diálogo manual
                return;
            }
            if (phase == Phase.PREFLOP) {
                deck.removeCards(state.allUsedCards());
                String c1 = drawUnique(), c2 = drawUnique(), c3 = drawUnique();
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

        private void mostrarTurn() {
            if (deck == null) return;

            if (!heroPanel.isRandomBoard()) {
                onEditarBoard();
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

        private void mostrarRiver() {
            if (deck == null) return;

            if (!heroPanel.isRandomBoard()) {
                onEditarBoard();
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

        private void onEditarBoard() {
            if (deck == null) {
                JOptionPane.showMessageDialog(PokerEquityGUI.this,
                        "No hay mazo activo. Pulsa 'Deal' primero.",
                        "Sin mazo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (heroPanel.isRandomBoard()) {
                JOptionPane.showMessageDialog(PokerEquityGUI.this,
                        "Desactiva 'Random Board' para editar manualmente.",
                        "No permitido", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            BoardEditorDialog dlg = new BoardEditorDialog(PokerEquityGUI.this, state, deck);
            dlg.setVisible(true);

            // Si guardó, BoardEditorDialog ya ajustó la fase y el mazo
            if (dlg.isSaved()) {
                phase = state.getPhase();
                tablePanel.repaint();
                updateButtonsState();
                updateEquities();
                statusBar.setMessage("Board editado manualmente.");
                statusBar.setRight("Mazo restante: " + deck.remaining());
            }
        }

        private String drawUnique() {
            Set<String> used = new HashSet<>(state.allUsedCards());
            String c;
            int guard = 0;
            do {
                c = deck.draw();
                if (++guard > 200) throw new IllegalStateException("No hay cartas únicas disponibles");
            } while (used.contains(c));
            return c;
        }
        
    } // Fin de la clase Controller

    // =========================
    //   Main
    // =========================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(PokerEquityGUI::new);
    }
}