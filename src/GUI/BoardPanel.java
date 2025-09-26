package GUI;

import javax.swing.*;
import java.awt.*;
import poker.Carta;

public class BoardPanel extends JPanel {
    private Carta[] communityCards;

    public BoardPanel() {
        communityCards = new Carta[5]; // Assuming a maximum of 5 community cards
        setLayout(new GridLayout(1, 5)); // Layout for displaying community cards
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < communityCards.length; i++) {
            communityCards[i] = null; // Initialize with no cards
            JLabel cardLabel = new JLabel("Card " + (i + 1)); // Placeholder for card display
            cardLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(cardLabel);
        }
    }

    public void updateBoard(Carta[] newCards) {
        communityCards = newCards;
        removeAll(); // Clear existing labels
        for (Carta card : communityCards) {
            JLabel cardLabel = new JLabel(card != null ? card.toString() : "Empty");
            cardLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(cardLabel);
        }
        revalidate(); // Refresh the panel
        repaint(); // Repaint to show updated cards
    }
}