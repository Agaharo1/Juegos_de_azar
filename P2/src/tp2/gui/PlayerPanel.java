package tp2.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class PlayerPanel extends JPanel {
    private String playerName;
    private boolean isHero;
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
        setBackground(new Color(40, 50, 65));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isHero ? new Color(100, 200, 255) : new Color(75, 85, 99), 3),
                new EmptyBorder(5, 5, 5, 5)
        ));

        // Panel superior: Nombre
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        topPanel.setBackground(getBackground());
        JLabel nameLabel = new JLabel(isHero ? playerName + " (YOU)" : playerName);
        nameLabel.setFont(new Font("Segoe UI", isHero ? Font.BOLD : Font.PLAIN, 10));
        nameLabel.setForeground(isHero ? new Color(100, 200, 255) : new Color(200, 200, 200));
        topPanel.add(nameLabel);
        add(topPanel, BorderLayout.NORTH);

        // Panel central: cartas
        cardsPanel = new CardsPanel();
        add(cardsPanel, BorderLayout.CENTER);

        // Panel inferior: Equity
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bottomPanel.setBackground(new Color(34, 60, 85));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JPanel equityPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(34, 150, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.setColor(new Color(20, 100, 200));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            }
        };
        equityPanel.setLayout(new BorderLayout());
        equityPanel.setPreferredSize(new Dimension(120, 30));
        equityPanel.setOpaque(false);

        equityField = new JLabel("0.0%", JLabel.CENTER);
        equityField.setForeground(Color.WHITE);
        equityField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        equityField.setOpaque(false);
        equityField.setBackground(new Color(0, 0, 0, 0));
        
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

    public void reset() {
        cards = "";
        cardsPanel.setCards("");
        equityField.setText("0.0%");
    }

    class CardsPanel extends JPanel {
        private String cards = "";
        
        public void setCards(String cards) { 
            this.cards = cards; 
            setBackground(new Color(34,60,85)); 
            repaint(); 
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(cards.length()>=4){
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                String card1 = cards.substring(0,2);
                String card2 = cards.substring(2,4);
                Image img1 = loadCardImage(card1);
                Image img2 = loadCardImage(card2);

                int w = (int)(getWidth()*0.45);
                int h = (int)(getHeight()*0.85);
                int overlap = (int)(w*1.1);
                int y = (getHeight()-h)/2;

                if(img1!=null) g2.drawImage(img1,5,y,w,h,this);
                if(img2!=null) g2.drawImage(img2,5+overlap,y,w,h,this);
            }
        }

        private Image loadCardImage(String code){
            String path = "resources/cartas/"+code+".png";
            java.io.File file = new java.io.File(path);
            if(file.exists()) return new ImageIcon(path).getImage();
            else return null;
        }
    }
}
