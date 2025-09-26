package GUI;

import javax.swing.*;
import java.awt.*;
import poker.Utils;

public class PokerGUI extends JFrame {
    
    public PokerGUI() {
        setTitle("Poker Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(800, 600);
        
        // Create panels for each section
        BoardPanel boardPanel = new BoardPanel();
        PlayerCardsPanel playerCardsPanel = new PlayerCardsPanel();
        InputFilePanel inputFilePanel = new InputFilePanel();
        SectionSelectorPanel sectionSelectorPanel = new SectionSelectorPanel();
        ResultPanel resultPanel = new ResultPanel();
        
        // Add sections to the main frame
        add(boardPanel, BorderLayout.CENTER);
        add(playerCardsPanel, BorderLayout.EAST);
        add(inputFilePanel, BorderLayout.WEST);
        add(sectionSelectorPanel, BorderLayout.NORTH);
        add(resultPanel, BorderLayout.SOUTH);
        
        // Set the visibility
        setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PokerGUI());
    }
}