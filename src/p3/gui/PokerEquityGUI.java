package p3.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import p3.logic.Deck;
import p3.logic.EquityCalculator;
import p3.logic.HandUtils;
import p3.logic.PokerStoveEquityCalculator;
import p3.logic.RangeParser;
import p3.logic.RankingProvider;
import p3.model.GameState;
import p3.model.Hand;

public class PokerEquityGUI extends JFrame {

    private JPanel mainPanel;
    private JPanel tablePanel;
    private JPanel controlPanel;
    private List<PlayerPanel> playerPanels;
    private Phase phase = Phase.PREFLOP;
    private Deck deck;
    private final EquityCalculator calc = new PokerStoveEquityCalculator();
    private Map<String, Double> cachedEquities = null;
    private final GameState state = new GameState();
    private JButton btnDeal, btnFlop, btnTurn, btnRiver, btnReset, btnComprobar, btnEditBoard;
    private HeroPanel heroPanel;
    private StatusBar statusBar;
    private final Controller controller = new Controller();
    private static final int UTG_INDEX = 3; 

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
                int ellipseW = (int)(w * 0.7);
                int ellipseH = (int)(h * 0.6);
                g2.setColor(UiTheme.BG_CARD);
                g2.fillOval(centerX - ellipseW / 2, centerY - ellipseH / 2, ellipseW, ellipseH);
                g2.setColor(UiTheme.TABLE_STROKE);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(centerX - ellipseW / 2, centerY - ellipseH / 2, ellipseW, ellipseH);

                int show = switch (phase) { case FLOP -> 3; case TURN -> 4; case RIVER -> 5; default -> 0; };
                int cardW = 95, cardH = 150, spacing = 30;
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
        String[] names = {"Button", "Small Blind", "Big Blind", "UTG", "Highjack", "Cut-off"};
        
        for (int i = 0; i < 6; i++) {
            PlayerPanel pp = new PlayerPanel(names[i], i == 4); 
            final int seat = i;
            pp.setOnEditHand(() -> abrirEditorMano(seat));
            pp.setOnClearHand(() -> quitarMano(seat));
            
            pp.setOnEditRange(() -> {
                String rangoActual = pp.getRangeInput();
                while (true) {
                    String nuevoRango = (String) JOptionPane.showInputDialog(this, "Introduce el Rango:", "Editar Rango", JOptionPane.PLAIN_MESSAGE, null, null, rangoActual);
                    if (nuevoRango == null) return;
                    String raw = nuevoRango.trim().toUpperCase(Locale.ROOT).replace('S', 's').replace('O', 'o');
                    if (raw.isEmpty()) { pp.setRangeText(""); handleInputChanged(); break; }
                    boolean esValido = false;
                    try {
                        double val = Double.parseDouble(raw.replace("%", "").replace(",", "."));
                        if (val > 0 && val <= 100) esValido = true;
                    } catch (NumberFormatException ignored) {}
                    if (!esValido && RangeParser.isBasicFormat(raw)) esValido = true;
                    if (esValido) { pp.setRangeText(raw); handleInputChanged(); break; }
                    else JOptionPane.showMessageDialog(this, "Rango inválido.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            pp.setOnEditEM(() -> {
                String emActual = pp.getEMInput();
                while (true) {
                    String nuevoEM = (String) JOptionPane.showInputDialog(this, "Introduce Equity Mínimo:", "Editar EM", JOptionPane.PLAIN_MESSAGE, null, null, emActual);
                    if (nuevoEM == null) return;
                    String raw = nuevoEM.trim().replace("%", "").replace(",", ".");
                    if (raw.isEmpty()) { pp.setEMText(""); handleInputChanged(); break; }
                    try {
                        double valor = Double.parseDouble(raw);
                        if (valor < 0 || valor > 100) JOptionPane.showMessageDialog(this, "Valor 0-100.", "Error", JOptionPane.WARNING_MESSAGE);
                        else {
                            if (valor == (long) valor) pp.setEMText(String.format("%d", (long) valor));
                            else pp.setEMText(String.valueOf(valor));
                            handleInputChanged(); break;
                        }
                    } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Solo números.", "Error", JOptionPane.ERROR_MESSAGE); }
                }
            });
            playerPanels.add(pp); tablePanel.add(pp);
        }
    }
    
    private void handleInputChanged() {
        if (checkAllInputsReady()) calculateEquities(); 
        else updatePanelColors(); 
    }

    private boolean checkAllInputsReady() {
        for (int i = 0; i < playerPanels.size(); i++) {
            Hand h = state.getPlayers().get(i);
            if (h != null) {
                PlayerPanel pp = playerPanels.get(i);
                String rg = pp.getRangeInput();
                String em = pp.getEMInput();
                if (rg == null || rg.trim().isEmpty() || em == null || em.trim().isEmpty()) return false;
            }
        }
        return true;
    }

    // ========================================================================
    //   ACTUALIZACIÓN VISUAL (CON MENSAJE DE ESPERA)
    // ========================================================================
    private void updatePanelColors() {
        boolean inputsReady = checkAllInputsReady();

        for (int i = 0; i < playerPanels.size(); i++) {
            PlayerPanel pp = playerPanels.get(i);
            Hand hand = state.getPlayers().get(i);
            
            // 1. Si el asiento está vacío
            if (hand == null) {
                pp.setEquity(0.0);
                pp.setRangeStatus(null); pp.setEMStatus(null);
                pp.setBackground(UiTheme.BG_CARD);
                continue;
            }

            // 2. Si faltan datos por introducir (RG o EM)
            if (!inputsReady) {
                // AQUI PONEMOS EL MENSAJE SOLICITADO
                // Usamos HTML para que haga salto de linea y se vea mejor
                pp.setEquityText("Esperando los RG y EM");
                
                pp.setRangeStatus(null); // Neutro
                pp.setEMStatus(null);    // Neutro
                pp.setBackground(UiTheme.BG_CARD);
                continue;
            }

            // 3. Si todo está listo (inputsReady = true) y tenemos cálculos
            if (cachedEquities != null) {
                String name = pp.getPlayerName();
                double equityJugador = cachedEquities.getOrDefault(name, 0.0);
                pp.setEquity(equityJugador); // Esto restaura la fuente grande y pone el %

                // Solo coloreamos UTG (Índice 3)
                if (i == UTG_INDEX) {
                    // --- Validar RANGO ---
                    String rangoRaw = pp.getRangeInput().trim();
                    Boolean rangeStatus = null; 
                    boolean enRango = true;
                    if (!rangoRaw.isEmpty()) {
                         try {
                            if (rangoRaw.endsWith("%")) enRango = RankingProvider.isInTopPercent(hand, Double.parseDouble(rangoRaw.replace("%", "")));
                            else if (rangoRaw.matches("\\d+")) enRango = RankingProvider.isInTopPercent(hand, Double.parseDouble(rangoRaw));
                            else enRango = RangeParser.parse(rangoRaw).contains(HandUtils.to169(hand).toUpperCase(Locale.ROOT));
                         } catch (Exception e) { enRango = false; }
                         rangeStatus = enRango;
                    }
                    pp.setRangeStatus(rangeStatus);

                    // --- Validar EM ---
                    String emRaw = pp.getEMInput().replace("%", "").replace(",", ".").trim();
                    Boolean emStatus = null;
                    boolean cumpleEM = true;
                    if (!emRaw.isEmpty()) {
                        try {
                            double em = Double.parseDouble(emRaw);
                            cumpleEM = (equityJugador >= em);
                            emStatus = cumpleEM;
                        } catch (Exception e) { emStatus = false; }
                    }
                    pp.setEMStatus(emStatus);
                } else {
                    // Resto de jugadores: Neutro
                    pp.setRangeStatus(null);
                    pp.setEMStatus(null);
                }
                pp.setBackground(UiTheme.BG_CARD);
            }
        }
    }

    private void positionPlayers() {
        int w = tablePanel.getWidth(), h = tablePanel.getHeight();
        if (w == 0 || h == 0) return;
        int centerX = w / 2, centerY = h / 2;
        int rx = (int)(w * 0.40), ry = (int)(h * 0.35);
        int panelW = 160, panelH = 260; 
        double offset = Math.PI * 0.5;
        for (int i = 0; i < playerPanels.size(); i++) {
            double ang = offset + (2 * Math.PI * i / playerPanels.size());
            playerPanels.get(i).setBounds((int)(centerX + rx * Math.cos(ang)) - panelW / 2, (int)(centerY + ry * Math.sin(ang)) - panelH / 2, panelW, panelH);
        }
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 10));
        panel.setBorder(new CompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER, 2), new EmptyBorder(15, 15, 15, 15)));
        panel.setBackground(UiTheme.BG_PANEL);
        heroPanel = new HeroPanel();
        heroPanel.addRandomBoardListener(e -> updateButtonsState());
        panel.add(heroPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(UiTheme.BG_PANEL);
        btnDeal = createStyledButton("Deal"); btnFlop = createStyledButton("Flop"); btnTurn = createStyledButton("Turn");
        btnRiver = createStyledButton("River"); btnReset = createStyledButton("Reset"); btnComprobar = createStyledButton("Comprobar"); btnEditBoard = createStyledButton("Editar Board");
        btnDeal.setActionCommand("DEAL"); btnFlop.setActionCommand("FLOP"); btnTurn.setActionCommand("TURN"); btnRiver.setActionCommand("RIVER");
        btnReset.setActionCommand("RESET"); btnComprobar.setActionCommand("COMPROBAR"); btnEditBoard.setActionCommand("EDIT_BOARD");
        ActionListener l = controller;
        btnDeal.addActionListener(l); btnFlop.addActionListener(l); btnTurn.addActionListener(l); btnRiver.addActionListener(l);
        btnReset.addActionListener(l); btnComprobar.addActionListener(l); btnEditBoard.addActionListener(l);
        buttonPanel.add(btnEditBoard); buttonPanel.add(btnDeal); buttonPanel.add(btnFlop); buttonPanel.add(btnTurn);
        buttonPanel.add(btnRiver); buttonPanel.add(btnReset); buttonPanel.add(btnComprobar);
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
        boolean hasDeck = (deck != null);
        if (btnDeal != null) btnDeal.setEnabled(true);
        if (btnReset != null) btnReset.setEnabled(true);
        if (btnFlop != null) btnFlop.setEnabled(hasDeck && phase == Phase.PREFLOP);
        if (btnTurn != null) btnTurn.setEnabled(hasDeck && phase == Phase.FLOP);
        if (btnRiver != null) btnRiver.setEnabled(hasDeck && phase == Phase.TURN);
        if (btnEditBoard != null) btnEditBoard.setEnabled(hasDeck && !heroPanel.isRandomBoard());
    }

    private void calculateEquities() {
        state.ensurePlayersCount(playerPanels.size());
        List<Hand> allHands = state.getPlayers();
        List<String> board = state.getBoard().visible();
        List<String> activeNames = new ArrayList<>();
        List<Hand> activeHands = new ArrayList<>();
        for (int i = 0; i < allHands.size(); i++) {
            if (allHands.get(i) != null) { activeHands.add(allHands.get(i)); activeNames.add(playerPanels.get(i).getPlayerName()); }
        }
        if (activeHands.size() < 2) { cachedEquities = null; updatePanelColors(); return; }
        int trials = switch (phase) { case PREFLOP -> 100000; case FLOP -> 200000; case TURN -> 300000; case RIVER -> 1; };
        String seedKey = String.join("-", activeNames) + "|" + activeHands + "|" + board + "|" + phase;
        cachedEquities = calc.calcularEquity(activeNames, activeHands, board, trials, seedKey.hashCode());
        updatePanelColors();
    }

    private void syncDeckAfterChange() {
        if (deck != null) {
            List<String> used = new ArrayList<>(state.allUsedCards());
            used.addAll(state.getFoldedCards());
            deck.removeCards(used);
            statusBar.setRight("Mazo restante: " + deck.remaining());
        }
    }

    private Hand stateGetPlayerHand(int i) { try { return state.getPlayers().get(i); } catch (Exception e) { return null; } }

    private HandEditorDialog.ValidationResult validarManoContraEstado(String text, int seat) {
        String t = (text == null) ? "" : text.replaceAll("\\s+", "");
        if (t.length() != 4) return HandEditorDialog.ValidationResult.error("Usa 4 caracteres.");
        final Hand hand;
        try { hand = Hand.fromString(t); } catch (IllegalArgumentException ex) { return HandEditorDialog.ValidationResult.error(ex.getMessage()); }
        Set<String> usadas = new HashSet<>(state.allUsedCards());
        Hand previa = stateGetPlayerHand(seat);
        if (previa != null) { usadas.remove(previa.card1()); usadas.remove(previa.card2()); }
        if (usadas.contains(hand.card1()) || usadas.contains(hand.card2())) return HandEditorDialog.ValidationResult.error("Carta usada.");
        return HandEditorDialog.ValidationResult.ok(hand);
    }

    private void abrirEditorMano(int seat) {
        if (deck == null) { JOptionPane.showMessageDialog(this, "Deal primero.", "Error", JOptionPane.WARNING_MESSAGE); return; }
        Hand actual = stateGetPlayerHand(seat);
        new HandEditorDialog(this, "Editar J" + (seat+1), (actual==null?"":actual.toString()),
            input -> validarManoContraEstado(input, seat),
            hand -> { state.setPlayerHand(seat, hand); playerPanels.get(seat).setCards(hand.toString()); syncDeckAfterChange(); handleInputChanged(); tablePanel.repaint(); },
            () -> { state.setPlayerHand(seat, null); playerPanels.get(seat).setCards(""); syncDeckAfterChange(); handleInputChanged(); tablePanel.repaint(); }
        ).setVisible(true);
    }

    private void quitarMano(int seat) {
        Hand hand = stateGetPlayerHand(seat);
        if (hand != null) state.addFoldedHand(hand);
        state.setPlayerHand(seat, null);
        playerPanels.get(seat).setCards("");
        syncDeckAfterChange();
        handleInputChanged();
        tablePanel.repaint();
        playerPanels.get(seat).setBackground(new Color(50, 50, 50));
    }

    private final class Controller implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case "DEAL" -> repartirCartas(); case "FLOP" -> mostrarFlop(); case "TURN" -> mostrarTurn();
                case "RIVER" -> mostrarRiver(); case "RESET" -> reset(); case "EDIT_BOARD" -> onEditarBoard();
            }
        }
        private String drawUnique() {
            Set<String> used = new HashSet<>(state.allUsedCards());
            String c; int g = 0;
            do { c = deck.draw(); if (++g > 200) throw new IllegalStateException("Sin cartas."); } while (used.contains(c));
            return c;
        }
        private void repartirCartas() {
            deck = new Deck(); state.reset(); deck.removeCards(state.allUsedCards());
            for (int i = 0; i < playerPanels.size(); i++) {
                PlayerPanel pp = playerPanels.get(i);
                if (i == 4 && !heroPanel.isRandomCards()) { pp.setCards(""); state.setPlayerHand(i, null); continue; }
                pp.setCards(drawUnique() + drawUnique()); state.setPlayerHand(i, new Hand(pp.getCards().substring(0,2), pp.getCards().substring(2,4)));
            }
            phase = Phase.PREFLOP; state.setPhase(phase); tablePanel.repaint(); updateButtonsState(); handleInputChanged();
            statusBar.setMessage("Repartido. Esperando RG/EM...");
        }
        private void mostrarFlop() { if (deck==null) return; if (!heroPanel.isRandomBoard()) { onEditarBoard(); return; } deck.removeCards(state.allUsedCards()); state.getBoard().setFlop(drawUnique(), drawUnique(), drawUnique()); phase = Phase.FLOP; state.setPhase(phase); tablePanel.repaint(); updateButtonsState(); handleInputChanged(); }
        private void mostrarTurn() { if (deck==null) return; if (!heroPanel.isRandomBoard()) { onEditarBoard(); return; } deck.removeCards(state.allUsedCards()); state.getBoard().setTurn(drawUnique()); phase = Phase.TURN; state.setPhase(phase); tablePanel.repaint(); updateButtonsState(); handleInputChanged(); }
        private void mostrarRiver() { if (deck==null) return; if (!heroPanel.isRandomBoard()) { onEditarBoard(); return; } deck.removeCards(state.allUsedCards()); state.getBoard().setRiver(drawUnique()); phase = Phase.RIVER; state.setPhase(phase); tablePanel.repaint(); updateButtonsState(); handleInputChanged(); }
        private void reset() { phase = Phase.PREFLOP; state.reset(); deck = null; cachedEquities = null; for (PlayerPanel pp : playerPanels) { pp.reset(); pp.setRangeStatus(null); pp.setEMStatus(null); } tablePanel.repaint(); updateButtonsState(); statusBar.setMessage("Reiniciado."); }
        private void onEditarBoard() { if (deck==null) return; BoardEditorDialog dlg = new BoardEditorDialog(PokerEquityGUI.this, state, deck); dlg.setVisible(true); if (dlg.isSaved()) { phase = state.getPhase(); tablePanel.repaint(); updateButtonsState(); handleInputChanged(); } }
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(PokerEquityGUI::new); }
}