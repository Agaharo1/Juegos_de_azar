package tp2.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Locale;

public class PlayerPanel extends JPanel {
    private final String playerName;
    private final boolean isHero;

    private String cards = "";
    private JLabel equityField;
    private CardsPanel cardsPanel;

    private Color rangeFeedbackColor = null; // Almacena el color de feedback (verde/rojo)

    // Hooks
    private JButton editBtn;
    private JButton clearBtn;
    private Runnable onEditHand;
    private Runnable onClearHand;

    public PlayerPanel(String name, boolean isHero) {
        this.playerName = name;
        this.isHero = isHero;
        initializeComponents();
    }
    
    /**
     * Actualiza el borde del panel. Utiliza rangeFeedbackColor si está establecido,
     * o el color por defecto (HERO_BORDER o BORDER) si no hay feedback de rango.
     */
    private void updateBorder() {
        Color mainColor = isHero ? UiTheme.HERO_BORDER : UiTheme.BORDER;
        Color borderColor = (rangeFeedbackColor != null) ? rangeFeedbackColor : mainColor;
        
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 3),
            new EmptyBorder(5, 5, 5, 5)
        ));
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(2, 2));
        setBackground(UiTheme.BG_CARD);
        // Llama a updateBorder() en lugar de setBorder directamente
        updateBorder(); 

        // ---- Top: nombre + botón Editar (estilo Alberto)
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        top.setBackground(getBackground());

        JLabel nameLabel = new JLabel(isHero ? playerName + " (YOU)" : playerName);
        nameLabel.setFont(isHero ? UiTheme.F_10B : UiTheme.F_10);
        nameLabel.setForeground(isHero ? UiTheme.HERO_BORDER : UiTheme.FG_TEXT);
        top.add(nameLabel);

        editBtn = new JButton("Editar");
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        editBtn.setMargin(new Insets(1, 4, 1, 4));
        editBtn.setFocusable(false);
        editBtn.addActionListener(e -> { if (onEditHand != null) onEditHand.run(); });
        top.add(editBtn);

        add(top, BorderLayout.NORTH);

        // ---- Centro: cartas
        cardsPanel = new CardsPanel();
        add(cardsPanel, BorderLayout.CENTER);

        // ---- Abajo: chip equity + “Quitar mano”
        JPanel southContainer = new JPanel();
        southContainer.setLayout(new BoxLayout(southContainer, BoxLayout.Y_AXIS));
        southContainer.setOpaque(false);

        JPanel chip = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiTheme.CHIP_FILL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UiTheme.CHIP_STROK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        chip.setLayout(new BorderLayout());
        chip.setPreferredSize(new Dimension(120, 30));
        chip.setOpaque(false);

        equityField = new JLabel("0.0%", JLabel.CENTER);
        equityField.setForeground(Color.WHITE);
        equityField.setFont(UiTheme.F_18B);
        chip.add(equityField, BorderLayout.CENTER);

        JPanel chipWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        chipWrap.setOpaque(false);
        chipWrap.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 5));
        chipWrap.add(chip);
        southContainer.add(chipWrap);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(2, 5, 5, 5));
        clearBtn = new JButton("Quitar mano");
        clearBtn.addActionListener(e -> { if (onClearHand != null) onClearHand.run(); });
        actions.add(clearBtn);
        southContainer.add(actions);

        add(southContainer, BorderLayout.SOUTH);
    }

    public void setCards(String cards) {
        this.cards = cards == null ? "" : cards;
        cardsPanel.setCards(this.cards);
        repaint();
    }

    public String getCards() { return cards; }

    public void setEquity(double pct) {
        equityField.setText(String.format(Locale.ROOT, "%.1f%%", pct));
    }
    
    /**
     * Establece el color de feedback del rango.
     * null = neutral/sin mano; true = dentro (verde); false = fuera (rojo).
     */
    public void setRangeFeedback(Boolean inRange) {
        if (inRange == null) {
            this.rangeFeedbackColor = null; 
        } else if (inRange) {
            this.rangeFeedbackColor = new Color(60, 180, 75); // Verde sutil
        } else {
            this.rangeFeedbackColor = new Color(180, 60, 60); // Rojo sutil
        }
        updateBorder(); // Actualiza el borde inmediatamente
    }

    public String getPlayerName() { return playerName; }

    public void reset() {
        cards = "";
        cardsPanel.setCards("");
        equityField.setText("0.0%");
        setRangeFeedback(null); // Resetea el feedback del rango a neutral
    }

    public void setOnEditHand(Runnable r)  { this.onEditHand  = r; }
    public void setOnClearHand(Runnable r) { this.onClearHand = r; }

    public void setActionsEnabled(boolean enabled) {
        editBtn.setEnabled(enabled);
        clearBtn.setEnabled(enabled);
    }

    class CardsPanel extends JPanel {
        private String cards = "";
        public void setCards(String cards) {
            this.cards = cards == null ? "" : cards;
            setBackground(UiTheme.BG_CARD);
            repaint();
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (cards != null && cards.length() >= 4) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                String card1 = cards.substring(0, 2);
                String card2 = cards.substring(2, 4);
                Image img1 = CardImages.get(card1);
                Image img2 = CardImages.get(card2);
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