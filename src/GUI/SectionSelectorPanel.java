package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SectionSelectorPanel extends JPanel {
    
    private JButton boardButton;
    private JButton playerCardsButton;
    private JButton inputFileButton;
    private JButton resultButton;

    public SectionSelectorPanel() {
        setLayout(new FlowLayout());

        boardButton = new JButton("Board");
        playerCardsButton = new JButton("Player Cards");
        inputFileButton = new JButton("Input File");
        resultButton = new JButton("Results");

        add(boardButton);
        add(playerCardsButton);
        add(inputFileButton);
        add(resultButton);

        // Add action listeners for buttons
        boardButton.addActionListener(new SectionButtonListener("Board"));
        playerCardsButton.addActionListener(new SectionButtonListener("Player Cards"));
        inputFileButton.addActionListener(new SectionButtonListener("Input File"));
        resultButton.addActionListener(new SectionButtonListener("Results"));
    }

    private class SectionButtonListener implements ActionListener {
        private String section;

        public SectionButtonListener(String section) {
            this.section = section;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Logic to switch to the selected section
            System.out.println("Switching to: " + section);
            // Here you would add the logic to update the main GUI to show the selected section
        }
    }
}