package p3.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component; 
import java.awt.Cursor; 
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter; 
import java.awt.event.MouseEvent; 
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class PlayerPanel extends JPanel {
    private final String playerName;
    private final boolean isHero;

    private String cards = "";
    private JLabel equityField;
    private CardsPanel cardsPanel;
    
    private JTextField rangeField;
    private JTextField emField;

    // Colores de la Práctica 3
    private final Color COLOR_RANGO_BORDE = new Color(138, 43, 226); // Morado
    private final Color COLOR_EM_BORDE = new Color(255, 165, 0);    // Naranja/Amarillo

    // Hooks separados
    private JButton editBtn;
    private JButton clearBtn;
    private Runnable onEditHand;
    private Runnable onClearHand;
    private Runnable onEditRange; 
    private Runnable onEditEM;    

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

        // ---- Top: nombre + botón Editar
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

        // ---- Abajo: Inputs, chip equity + “Quitar mano”
        JPanel southContainer = new JPanel();
        southContainer.setLayout(new BoxLayout(southContainer, BoxLayout.Y_AXIS));
        southContainer.setOpaque(false);

        // --- Panel de Inputs (Rango y EM) apilados verticalmente ---
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS)); 
        inputPanel.setOpaque(false);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));

        // Sub-panel para Rango
        JPanel rangePanel = new JPanel();
        rangePanel.setLayout(new BoxLayout(rangePanel, BoxLayout.X_AXIS)); 
        rangePanel.setOpaque(false);
        rangePanel.setAlignmentX(Component.CENTER_ALIGNMENT); 
        
        rangeField = new JTextField();
        styleField(rangeField, COLOR_RANGO_BORDE, 90, 28); 
        
        rangeField.setEditable(false);
        rangeField.setFocusable(false);
        rangeField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rangeField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onEditRange != null) onEditRange.run(); 
            }
        });
        
        JLabel labelR = new JLabel("RG:");
        styleLabel(labelR);
        
        rangePanel.add(labelR);
        rangePanel.add(Box.createHorizontalStrut(4));
        rangePanel.add(rangeField);
        inputPanel.add(rangePanel);

        inputPanel.add(Box.createVerticalStrut(4)); 

        // Sub-panel para EM
        JPanel emPanel = new JPanel();
        emPanel.setLayout(new BoxLayout(emPanel, BoxLayout.X_AXIS)); 
        emPanel.setOpaque(false);
        emPanel.setAlignmentX(Component.CENTER_ALIGNMENT); 
        
        emField = new JTextField();
        styleField(emField, COLOR_EM_BORDE, 90, 28); 
        
        emField.setEditable(false);
        emField.setFocusable(false);
        emField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        emField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onEditEM != null) onEditEM.run(); 
            }
        });
        
        JLabel labelEM = new JLabel("EM:");
        styleLabel(labelEM);
        
        emPanel.add(labelEM);
        emPanel.add(Box.createHorizontalStrut(4));
        emPanel.add(emField);
        inputPanel.add(emPanel);
        
        southContainer.add(inputPanel);

        // ---- Chip equity
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
        chipWrap.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        chipWrap.setAlignmentX(Component.CENTER_ALIGNMENT); 
        chipWrap.add(chip);
        southContainer.add(chipWrap);

        // ---- Acciones
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        actions.setAlignmentX(Component.CENTER_ALIGNMENT); 
        
        clearBtn = new JButton("Quitar mano");
        clearBtn.addActionListener(e -> { if (onClearHand != null) onClearHand.run(); });
        actions.add(clearBtn);
        southContainer.add(actions);

        add(southContainer, BorderLayout.SOUTH);
    }
    
    /**
     * Aplica el estilo visual estándar a un JTextField.
     */
    private void styleField(JTextField field, Color borderColor, int width, int height) {
        field.setFont(UiTheme.F_13B); 
        
        field.setBackground(UiTheme.BG_INPUT);
        field.setForeground(UiTheme.FG_TEXT);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        
        Dimension dim = new Dimension(width, height);
        field.setPreferredSize(dim);
        field.setMaximumSize(dim); 
    }
    
    // Helper para dar estilo a las etiquetas R: y EM:
    private void styleLabel(JLabel label) {
        label.setFont(UiTheme.F_10B);
        label.setForeground(UiTheme.FG_TEXT_DIM);
    }

    public void setCards(String cards) {
        this.cards = cards == null ? "" : cards;
        cardsPanel.setCards(this.cards);
        repaint();
    }

    public String getCards() { return cards; }



    public String getPlayerName() { return playerName; }

    public void reset() {
        cards = "";
        cardsPanel.setCards("");
        equityField.setText("0.0%");
        if (rangeField != null) rangeField.setText("");
        if (emField != null) emField.setText("");
    }

    // Setters para los hooks y para el texto
    
    public void setOnEditHand(Runnable r)  { this.onEditHand  = r; }
    public void setOnClearHand(Runnable r) { this.onClearHand = r; }
    
    public void setOnEditRange(Runnable r) { this.onEditRange = r; } 
    public void setOnEditEM(Runnable r) { this.onEditEM = r; }      

    public void setRangeText(String text) {
        if (rangeField != null) {
            rangeField.setText(text);
        }
    }
    
    public void setEMText(String text) {
        if (emField != null) {
            emField.setText(text);
        }
    }

    public void setActionsEnabled(boolean enabled) {
        editBtn.setEnabled(enabled);
        clearBtn.setEnabled(enabled);
    }
    
    // Getters para Rango y EM
    public String getRangeInput() {
        return (rangeField != null) ? rangeField.getText() : "";
    }
    public String getEMInput() {
        return (emField != null) ? emField.getText() : "";
    }

    // --- Panel interno para dibujar las cartas ---
    class CardsPanel extends JPanel {
        private String cards = "";
        public void setCards(String cards) {
            this.cards = cards == null ? "" : cards;
            setBackground(UiTheme.BG_CARD);
            repaint();
        }
        
        // --- Lógica de dibujado de cartas (la que tenías originalmente) ---
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
    
    
 
    public void setRangeStatus(Boolean status) {
        if (rangeField == null) return;
        if (status == null) {
            rangeField.setBackground(UiTheme.BG_INPUT); 
        } else {
            rangeField.setBackground(status ? new Color(0, 130, 0) : new Color(130, 0, 0));
        }
    }


    public void setEMStatus(Boolean status) {
        if (emField == null) return;
        if (status == null) {
            emField.setBackground(UiTheme.BG_INPUT); 
        } else {
            emField.setBackground(status ? new Color(0, 130, 0) : new Color(130, 0, 0));
        }
    }
    
 
    public void setEquityText(String text) {
        equityField.setText(text);
        
        
        if (text != null && text.length() > 10) {
            equityField.setFont(UiTheme.F_9);
        } else {
            equityField.setFont(UiTheme.F_18B); 
        }
    }

 
    public void setEquity(double pct) {
        equityField.setFont(UiTheme.F_18B); 
        equityField.setText(String.format(Locale.ROOT, "%.3f%%", pct));
    }
}




