package tp2.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Locale;

public class PlayerPanel extends JPanel {
    private final String playerName;
    private final boolean isHero;
    private String cards = "";
    private JLabel equityField;
    private CardsPanel cardsPanel;

    public PlayerPanel(String name, boolean isHero) {
        this.playerName = name;
        this.isHero = isHero;
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(2, 2));
        setBackground(UiTheme.BG_CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isHero ? UiTheme.HERO_BORDER : UiTheme.BORDER, 3),
                new EmptyBorder(5, 5, 5, 5)
        ));

        // Panel superior: Nombre
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        topPanel.setBackground(getBackground());
        JLabel nameLabel = new JLabel(isHero ? playerName + " (YOU)" : playerName);
        nameLabel.setFont(isHero ? UiTheme.F_10B : UiTheme.F_10);
        nameLabel.setForeground(isHero ? UiTheme.HERO_BORDER : UiTheme.FG_TEXT);
        topPanel.add(nameLabel);
        add(topPanel, BorderLayout.NORTH);

        // Panel central: cartas
        cardsPanel = new CardsPanel();
        add(cardsPanel, BorderLayout.CENTER);

        // Panel inferior: Equity
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bottomPanel.setBackground(UiTheme.BG_CARD);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JPanel equityPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiTheme.CHIP_FILL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UiTheme.CHIP_STROK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            }
        };
        equityPanel.setLayout(new BorderLayout());
        equityPanel.setPreferredSize(new Dimension(120, 30));
        equityPanel.setOpaque(false);

        equityField = new JLabel("0.0%", JLabel.CENTER);
        equityField.setForeground(Color.WHITE);
        equityField.setFont(UiTheme.F_18B);
        equityField.setOpaque(false);

        equityPanel.add(equityField, BorderLayout.CENTER);
        bottomPanel.add(equityPanel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void setCards(String cards) {
        this.cards = cards;
        cardsPanel.setCards(cards);
        repaint();
    }

    public String getCards() {
        return cards;
    }

    /** NUEVO: establece el texto de equity con 1 decimal (e.g., 31.4%) */
    public void setEquity(double pct) {
        equityField.setText(String.format(Locale.ROOT, "%.1f%%", pct));
    }

    /** NUEVO: expone el nombre para el calculador */
    public String getPlayerName() {
        return playerName;
    }

    public void reset() {
        cards = "";
        cardsPanel.setCards("");
        equityField.setText("0.0%");
    }

    /** Panel que pinta las 2 cartas del jugador */
    class CardsPanel extends JPanel {
        private String cards = "";

        public void setCards(String cards) {
            this.cards = cards;
            setBackground(UiTheme.BG_CARD);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (cards != null && cards.length() >= 4) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                String card1 = cards.substring(0, 2);
                String card2 = cards.substring(2, 4);

                Image img1 = CardImages.get(card1); // caché
                Image img2 = CardImages.get(card2); // caché

                int w = (int) (getWidth() * 0.45);
                int h = (int) (getHeight() * 0.85);
                int overlap = (int) (w * 1.1);
                int y = (getHeight() - h) / 2;

                if (img1 != null) g2.drawImage(img1, 5, y, w, h, this);
                if (img2 != null) g2.drawImage(img2, 5 + overlap, y, w, h, this);
            }
        }
    }
}
