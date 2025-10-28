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
import tp2.logic.EquityCalculator;
import tp2.logic.PokerStoveEquityCalculator;

import tp2.model.GameState;
import tp2.model.Hand;

/**
 * Ventana principal de la aplicación.
 * Aquí se ve la mesa de póker, los 6 jugadores y los botones para controlar la partida.
 *
 * Esta clase:
 *  - Dibuja la mesa y el tablero (flop/turn/river).
 *  - Crea 6 paneles de jugador y los coloca alrededor.
 *  - Muestra un panel para que el usuario elija el rango del héroe.
 *  - Reparte las cartas y avanza por las calles del póker.
 *  - Llama al calculador para actualizar el porcentaje de victoria (equity).
 */
public class PokerEquityGUI extends JFrame {

    // Paneles principales de la ventana
    private JPanel mainPanel;    // Contiene todo
    private JPanel tablePanel;   // La mesa de póker y los 6 jugadores
    private JPanel controlPanel; // El panel de abajo con controles y botones

    // Lista con los 6 jugadores que se pintan alrededor de la mesa
    // (el héroe es el de posición 4, empezando en 0)
    private List<PlayerPanel> playerPanels;

    // Fase de la mano: PREFLOP, FLOP, TURN o RIVER
    private Phase phase = Phase.PREFLOP;

    // Baraja usada en la mano actual (se crea al pulsar "Deal")
    private Deck deck;

    // Calculador "real" que usa simulaciones para estimar los porcentajes
	private final EquityCalculator calc = new PokerStoveEquityCalculator();
	// Si quisieras forzar Monte Carlo puro:  new RealEquityCalculator();


    // Objeto que guarda el estado de la mano: cartas de cada jugador, tablero y fase
    private final GameState state = new GameState();

    // Botones de control de la parte inferior
    private JButton btnDeal, btnFlop, btnTurn, btnRiver, btnReset, btnComprobar;

    // Panel del héroe con los controles de rango y aleatoriedad
    private HeroPanel heroPanel;

    // Barra de estado (mensajes informativos)
    private StatusBar statusBar;

    // Controlador que escucha los botones y usa los métodos de esta clase
    private final Controller controller = new Controller();

    /**
     * Crea la ventana principal: ajusta tamaño, título, colores
     * y construye todos los paneles necesarios.
     */
    public PokerEquityGUI() {
        setTitle("Poker Equity Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setResizable(true);
        setBackground(UiTheme.BG_DARK);

        initializeComponents(); // construye la interfaz
        setVisible(true);       // muestra la ventana
    }

    /**
     * Monta la estructura principal:
     *  - Arriba: barra de estado
     *  - Centro: mesa y jugadores
     *  - Abajo: controles del héroe y botones
     */
    private void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(UiTheme.BG_DARK);

        // Panel central con la mesa + jugadores
        tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // Panel inferior con controles y botones
        controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        // Barra de estado superior
        statusBar = new StatusBar();
        mainPanel.add(statusBar, BorderLayout.NORTH);

        add(mainPanel);
    }

    /**
     * Crea el panel de la mesa.
     * Se dibuja un óvalo que simula la mesa y, en el centro, las cartas del tablero
     * según la fase (0, 3, 4 o 5 cartas).
     * También crea los paneles de los jugadores y los coloca alrededor.
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int centerX = w / 2, centerY = h / 2;

                // Dibuja la mesa como un óvalo
                int ellipseW = (int)(w * 0.7);
                int ellipseH = (int)(h * 0.6);
                g2.setColor(UiTheme.BG_CARD);
                g2.fillOval(centerX - ellipseW / 2, centerY - ellipseH / 2, ellipseW, ellipseH);
                g2.setColor(UiTheme.TABLE_STROKE);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(centerX - ellipseW / 2, centerY - ellipseH / 2, ellipseW, ellipseH);

                // Según la fase, muestra 0, 3, 4 o 5 cartas en el centro
                int show = switch (phase) {
                    case FLOP  -> 3;
                    case TURN  -> 4;
                    case RIVER -> 5;
                    default    -> 0;
                };

                // Dibuja las cartas del tablero en el centro
                int cardW = 95, cardH = 140, spacing = 30;
                int totalWidth = show * cardW + (show - 1) * spacing;
                int startX = centerX - totalWidth / 2;
                int y = centerY - cardH / 2;

                String[] board = state.getBoard().raw(); // devuelve 5 huecos (algunos vacíos)
                for (int i = 0; i < show; i++) {
                    drawCard(g2, startX + i * (cardW + spacing), y, board[i], cardW, cardH);
                }
            }
        };
        panel.setLayout(null);
        panel.setBackground(UiTheme.BG_DARK);

        createPlayerPanels(panel); // crea y añade los 6 jugadores
        return panel;
    }

    /**
     * Dibuja una carta en una posición concreta.
     * Si existe la imagen PNG de esa carta, la dibuja; si no, dibuja un rectángulo simple.
     */
    private void drawCard(Graphics2D g, int x, int y, String code, int w, int h) {
        // Sombra y base
        g.setColor(new Color(50, 50, 50));
        g.fillRect(x + 2, y + 2, w, h);
        g.setColor(new Color(240, 240, 240));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(100, 100, 100));
        g.drawRect(x, y, w, h);

        // Si hay una carta concreta (por ejemplo "Ah"), intenta cargar su imagen
        if (code != null && !code.isEmpty()) {
            Image img = CardImages.get(code);
            if (img != null) g.drawImage(img, x, y, w, h, this);
        }
    }

    /**
     * Crea los 6 paneles de jugador, los añade a la mesa y
     * se asegura de recolocarlos cuando la ventana cambie de tamaño.
     */
    private void createPlayerPanels(JPanel tablePanel) {
        // Recoloca a los jugadores si la ventana cambia de tamaño
        tablePanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                positionPlayers();
            }
        });

        playerPanels = new ArrayList<>();
        String[] names = {"Player 1", "Player 2", "Player 3", "Player 4", "Player 5", "Player 6"};
        for (int i = 0; i < 6; i++) {
            // El jugador de índice 4 es el héroe (lo marcamos como tal)
            PlayerPanel pp = new PlayerPanel(names[i], i == 4);
            playerPanels.add(pp);
            tablePanel.add(pp);
        }
    }

    /**
     * Coloca a los 6 jugadores alrededor del óvalo (como si estuvieran sentados).
     * Este método se llama al iniciar y cada vez que se cambia el tamaño de la ventana.
     */
    private void positionPlayers() {
        int w = tablePanel.getWidth(), h = tablePanel.getHeight();
        if (w == 0 || h == 0) return;

        int centerX = w / 2, centerY = h / 2;
        int rx = (int)(w * 0.40), ry = (int)(h * 0.35); // radios del "círculo" imaginario

        int panelW = 160, panelH = 200;
        double offset = Math.PI * 0.5; // para que el primero quede arriba

        for (int i = 0; i < playerPanels.size(); i++) {
            double ang = offset + (2 * Math.PI * i / playerPanels.size());
            int x = (int)(centerX + rx * Math.cos(ang)) - panelW / 2;
            int y = (int)(centerY + ry * Math.sin(ang)) - panelH / 2;
            playerPanels.get(i).setBounds(x, y, panelW, panelH);
        }
    }

    /**
     * Crea el panel inferior que incluye:
     *  - El panel del héroe con opciones de rango y aleatoriedad.
     *  - Los botones de Deal/Flop/Turn/River/Reset/Comprobar.
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(15, 10));
        panel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER, 2),
                new EmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(UiTheme.BG_PANEL);

        // Controles del héroe en el centro
        heroPanel = new HeroPanel();
        panel.add(heroPanel, BorderLayout.CENTER);

        // Botones a la derecha
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(UiTheme.BG_PANEL);

        btnDeal  = createStyledButton("Deal");
        btnFlop  = createStyledButton("Flop");
        btnTurn  = createStyledButton("Turn");
        btnRiver = createStyledButton("River");
        btnReset = createStyledButton("Reset");
        btnComprobar = createStyledButton("Comprobar rango");

        // Conectamos los botones con el controlador
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

        updateButtonsState(); // activa o desactiva según fase
        panel.add(buttonPanel, BorderLayout.EAST);
        return panel;
    }

    /**
     * Crea un botón con el mismo estilo para toda la app:
     * colores, tamaño, letra y efecto al pasar el ratón por encima.
     */
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

    /**
     * Activa o desactiva los botones según haya mazo y según la fase.
     * Por ejemplo, "Flop" solo está disponible en PREFLOP, etc.
     */
    private void updateButtonsState() {
        if (btnDeal != null)  btnDeal.setEnabled(true);
        if (btnReset != null) btnReset.setEnabled(true);

        boolean hasDeck = (deck != null);
        if (btnFlop  != null) btnFlop.setEnabled (hasDeck && phase == Phase.PREFLOP);
        if (btnTurn  != null) btnTurn.setEnabled (hasDeck && phase == Phase.FLOP);
        if (btnRiver != null) btnRiver.setEnabled(hasDeck && phase == Phase.TURN);
    }

    /**
     * Vuelve a calcular el porcentaje de cada jugador y lo pinta en su panel.
     * Usa un número de simulaciones distinto según la fase (más cartas = más precisión).
     * Antes de calcular, se asegura de que haya 6 jugadores en el estado.
     */
    private void updateEquities() {
        // Asegura que GameState tenga 6 huecos para manos (una por jugador)
        state.ensurePlayersCount(playerPanels.size());

        // Nombres y manos para el calculador
        List<String> jugadores = new ArrayList<>(playerPanels.size());
        for (PlayerPanel pp : playerPanels) jugadores.add(pp.getPlayerName());

        List<Hand> manos = state.getPlayers();     // puede tener nulls
        List<String> board = state.getBoard().visible();

        // Número de simulaciones según la fase (ajusta si quieres)
        int trials = switch (phase) {
            case PREFLOP -> 5000;
            case FLOP    -> 15000;
            case TURN    -> 30000;
            case RIVER   -> 1; // si ya están todas las cartas, basta 1 evaluación exacta
        };

        // Semilla para que los resultados se repitan si el estado no cambia
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

    // =====================
    // CONTROLADOR (MVC)
    // =====================

    /**
     * Este controlador escucha los botones y llama a los métodos
     * que reparten cartas, avanzan la fase, reinician o aplican el rango del héroe.
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
         *  - Si eliges "Textual": usa tal cual lo que hay escrito (si es válido).
         *  - Si eliges "Percentage": coge el top N del ranking según el porcentaje.
         * Luego toma al azar una combinación concreta (por ejemplo, de "AKs" saca "AhKh")
         * que no choque con cartas ya usadas, y la pone como mano del héroe.
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
                // Convierte el rango a una lista de manos en notación (AA, AKs, AKo, ...)
                List<String> manos = tp2.logic.RangeParser.parse(rango);

                // Elige una de esas manos al azar y genera dos cartas concretas compatibles
                Random rand = new Random();
                String manoElegida = manos.get(rand.nextInt(manos.size()));
                String cartasConcretas = generarCartasConcretasDesdeNotacion(manoElegida);

                // Coloca la mano elegida al héroe (posición 4) y actualiza el estado
                PlayerPanel hero = playerPanels.get(4);
                hero.setCards(cartasConcretas);
                state.setPlayerHand(4, Hand.fromString(cartasConcretas));

                // Quita del mazo las cartas ya usadas para evitar repeticiones
                if (deck != null) deck.removeCards(state.allUsedCards());

                // Actualiza los porcentajes en pantalla
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
         * A partir de una notación como "AA", "AKs" o "AQo",
         * crea dos cartas reales (por ejemplo "AhAd" o "AhKh") que no estén repetidas.
         * - 'S' significa del mismo palo (suited).
         * - 'O' significa de palos distintos (offsuit).
         * - Si no hay letra al final, se toma como pareja (ej: "TT").
         */
        private String generarCartasConcretasDesdeNotacion(String notacion) {
            String[] palos = {"h", "d", "c", "s"};
            Random rand = new Random();
            String n = notacion.toUpperCase(Locale.ROOT);

            // Quitamos la 'S' o 'O' del final si existen para quedarnos con las letras de la mano (ej: "AK")
            String base = n.replaceAll("[SO]$", "");
            if (base.length() != 2) {
                throw new IllegalArgumentException("Notación inválida: " + notacion);
            }
            char r1 = base.charAt(0);
            char r2 = base.charAt(1);

            // Cartas que ya están en juego y no se pueden repetir
            Set<String> used = new HashSet<>(state.allUsedCards());

            // Intentamos varias veces por si al azar saliera un palo ya ocupado
            for (int intentos = 0; intentos < 100; intentos++) {
                String c1, c2;
                if (n.endsWith("S")) { // mismo palo
                    String p = palos[rand.nextInt(4)];
                    c1 = "" + r1 + p;
                    c2 = "" + r2 + p;
                } else if (n.endsWith("O")) { // palos distintos
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

                // Aceptamos si no son la misma carta y no están ya usadas
                if (!c1.equals(c2) && !used.contains(c1) && !used.contains(c2)) {
                    return c1 + c2;
                }
            }
            throw new IllegalStateException("No se pudo generar una combinación válida sin duplicados.");
        }

        /**
         * Reparte 2 cartas a cada jugador.
         * Si el héroe tiene desactivado "Random Cards", se deja su mano vacía para que la elija luego.
         * Reinicia la fase a PREFLOP y recalcula los porcentajes.
         */
        private void repartirCartas() {
            deck = new Deck();           // baraja nueva
            state.reset();               // limpia manos y tablero
            state.ensurePlayersCount(playerPanels.size()); // fijamos 6 huecos
            deck.removeCards(state.allUsedCards());        // por seguridad (vacío al inicio)

            for (int i = 0; i < playerPanels.size(); i++) {
                PlayerPanel pp = playerPanels.get(i);

                // Si el héroe no quiere cartas aleatorias, lo dejamos en blanco
                if (i == 4 && !heroPanel.isRandomCards()) {
                    pp.setCards("");
                    state.setPlayerHand(i, null);
                    continue;
                }

                // Roba dos cartas que no estén repetidas
                String c1 = drawUnique();
                String c2 = drawUnique();
                pp.setCards(c1 + c2);
                state.setPlayerHand(i, new Hand(c1, c2));
            }

            // Volvemos a la primera fase y refrescamos
            phase = Phase.PREFLOP;
            state.setPhase(phase);
            tablePanel.repaint();

            updateButtonsState();
            updateEquities();

            statusBar.setMessage("Cartas repartidas. Fase: PREFLOP");
            statusBar.setRight("Mazo restante: " + deck.remaining());
        }

        /**
         * Saca el flop (3 cartas) de forma aleatoria si está activado "Random Board"
         * y si estamos en PREFLOP. Después pasa la fase a FLOP y recalcula.
         */
        private void mostrarFlop() {
            if (deck == null) return;
            if (!heroPanel.isRandomBoard()) {
                JOptionPane.showMessageDialog(PokerEquityGUI.this,
                        "Random Board desactivado. (UI para board manual pendiente)",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (phase == Phase.PREFLOP) {
                deck.removeCards(state.allUsedCards()); // evita choques

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

        /**
         * Saca el turn (4ª carta) de forma aleatoria si está activado "Random Board"
         * y si estamos en FLOP. Después pasa la fase a TURN y recalcula.
         */
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

        /**
         * Saca el river (5ª carta) de forma aleatoria si está activado "Random Board"
         * y si estamos en TURN. Después pasa la fase a RIVER y recalcula.
         */
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

        /**
         * Reinicia todo como al principio:
         *  - Borra manos y tablero
         *  - Vuelve a PREFLOP
         *  - Limpia textos en los paneles
         */
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

        /**
         * Roba una carta de la baraja que NO esté ya en uso.
         * Si saca una repetida, lo intenta de nuevo hasta dar con una válida.
         */
        private String drawUnique() {
            Set<String> used = new HashSet<>(state.allUsedCards());
            String c;
            do { c = deck.draw(); } while (used.contains(c));
            return c;
        }
    }

    /**
     * Punto de entrada del programa.
     * Lanza la ventana en el hilo gráfico de forma segura.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(PokerEquityGUI::new);
    }
}
