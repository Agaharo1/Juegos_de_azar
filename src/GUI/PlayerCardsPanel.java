package GUI;

import javax.swing.*;
import java.awt.*;

public class PlayerCardsPanel extends JPanel {
    
    private JLabel titleLabel;
    private JPanel cardsPanel;

    public PlayerCardsPanel() {
        setLayout(new BorderLayout());
        
        titleLabel = new JLabel("Player's Cards");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new FlowLayout());
        
        add(titleLabel, BorderLayout.NORTH);
        add(cardsPanel, BorderLayout.CENTER);
    }

    public void displayCards(String[] cards) {
        cardsPanel.removeAll();
        for (String card : cards) {
            JLabel cardLabel = new JLabel(card);
            cardLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            cardsPanel.add(cardLabel);
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }
}