package tp2.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Locale;

/**
 * Muestra la información de un jugador en la mesa.
 * Incluye: su nombre, sus 2 cartas (con imagen) y su equity (porcentaje).
 * Si es el héroe, se marca con un borde y un color especial.
 */
public class PlayerPanel extends JPanel {
    // Nombre que se muestra en la parte superior (ej: "Player 1")
    private final String playerName;

    // Indica si este panel es el del héroe (nuestro jugador principal)
    private final boolean isHero;

    // Código de las dos cartas juntas, por ejemplo "AhKd"
    private String cards = "";

    // Etiqueta donde se mostrará el porcentaje de equity (ej: "31.4%")
    private JLabel equityField;

    // Panel interno que se encarga de dibujar las imágenes de las cartas
    private CardsPanel cardsPanel;

    /**
     * Crea el panel del jugador con su nombre y si es héroe o no.
     * Llama a un método interno para construir toda la interfaz.
     */
    public PlayerPanel(String name, boolean isHero) {
        this.playerName = name;
        this.isHero = isHero;
        initializeComponents();
    }

    /**
     * Construye la estructura visual:
     * - Arriba: nombre del jugador.
     * - Centro: sus cartas (dibujadas como imágenes).
     * - Abajo: un "chip" con el porcentaje de equity.
     */
    private void initializeComponents() {
        setLayout(new BorderLayout(2, 2));
        setBackground(UiTheme.BG_CARD);

        // Borde más llamativo si es el héroe; más discreto si no lo es
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isHero ? UiTheme.HERO_BORDER : UiTheme.BORDER, 3),
                new EmptyBorder(5, 5, 5, 5)
        ));

        // ---- Parte superior: nombre del jugador
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        topPanel.setBackground(getBackground());

        JLabel nameLabel = new JLabel(isHero ? playerName + " (YOU)" : playerName);
        nameLabel.setFont(isHero ? UiTheme.F_10B : UiTheme.F_10);
        nameLabel.setForeground(isHero ? UiTheme.HERO_BORDER : UiTheme.FG_TEXT);

        topPanel.add(nameLabel);
        add(topPanel, BorderLayout.NORTH);

        // ---- Parte central: cartas
        cardsPanel = new CardsPanel();
        add(cardsPanel, BorderLayout.CENTER);

        // ---- Parte inferior: chip con la equity
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bottomPanel.setBackground(UiTheme.BG_CARD);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Fondo del "chip" donde va el porcentaje
        JPanel equityPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Suaviza los bordes para que se vea mejor
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Relleno del chip (azulado)
                g2.setColor(UiTheme.CHIP_FILL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Borde del chip
                g2.setColor(UiTheme.CHIP_STROK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        equityPanel.setLayout(new BorderLayout());
        equityPanel.setPreferredSize(new Dimension(120, 30));
        equityPanel.setOpaque(false); // deja ver el fondo del panel

        // Texto del porcentaje centrado dentro del chip
        equityField = new JLabel("0.0%", JLabel.CENTER);
        equityField.setForeground(Color.WHITE);
        equityField.setFont(UiTheme.F_18B);
        equityField.setOpaque(false);

        equityPanel.add(equityField, BorderLayout.CENTER);
        bottomPanel.add(equityPanel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Cambia las cartas del jugador.
     * El texto debe venir como 4 caracteres, por ejemplo "AhKd".
     * Al cambiar las cartas, se repinta el panel para ver las imágenes.
     */
    public void setCards(String cards) {
        this.cards = cards;
        cardsPanel.setCards(cards);
        repaint();
    }

    /** Devuelve las cartas actuales como texto (por ejemplo "AhKd"). */
    public String getCards() {
        return cards;
    }

    /**
     * Cambia el texto del porcentaje que se muestra en el chip.
     * Se imprime con un decimal, por ejemplo "31.4%".
     */
    public void setEquity(double pct) {
        equityField.setText(String.format(Locale.ROOT, "%.1f%%", pct));
    }

    /** Devuelve el nombre del jugador (lo usa el calculador para identificar). */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Deja el panel como al inicio: sin cartas y con equity a 0.0%.
     * Útil cuando se pulsa "Reset".
     */
    public void reset() {
        cards = "";
        cardsPanel.setCards("");
        equityField.setText("0.0%");
    }

    /**
     * Panel interno que solo se encarga de dibujar las 2 cartas del jugador.
     * Si hay imágenes disponibles (en la carpeta de recursos), las carga y dibuja.
     */
    class CardsPanel extends JPanel {
        private String cards = "";

        /** Cambia las cartas a dibujar y repinta el panel. */
        public void setCards(String cards) {
            this.cards = cards;
            setBackground(UiTheme.BG_CARD);
            repaint();
        }

        /**
         * Dibuja las cartas si el texto tiene al menos 4 caracteres.
         * Divide el texto en 2 partes: "Ah" y "Kd", busca cada imagen y la coloca.
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Solo dibuja si hay al menos dos cartas (4 caracteres)
            if (cards != null && cards.length() >= 4) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Corta el texto en dos: primera y segunda carta (2 caracteres cada una)
                String card1 = cards.substring(0, 2);
                String card2 = cards.substring(2, 4);

                // Pide las imágenes al cargador de cartas (usa caché para ir rápido)
                Image img1 = CardImages.get(card1);
                Image img2 = CardImages.get(card2);

                // Calcula tamaños y posiciones para que se vean bien solapadas
                int w = (int) (getWidth() * 0.45);   // ancho de cada carta
                int h = (int) (getHeight() * 0.85);  // alto de cada carta
                int overlap = (int) (w * 1.1);       // cuánto se solapa la segunda carta sobre la primera
                int y = (getHeight() - h) / 2;       // centrado vertical

                // Dibuja la primera carta (si hay imagen)
                if (img1 != null) g2.drawImage(img1, 5, y, w, h, this);

                // Dibuja la segunda carta un poco a la derecha para que se solapen
                if (img2 != null) g2.drawImage(img2, 5 + overlap, y, w, h, this);
            }
        }
    }
}
