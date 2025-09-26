package GUI;

import javax.swing.*;
import java.awt.*;

public class ResultPanel extends JPanel {
    
    private JLabel resultLabel;
    private JTextArea resultTextArea;

    public ResultPanel() {
        setLayout(new BorderLayout());
        
        resultLabel = new JLabel("Game Results");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(resultLabel, BorderLayout.NORTH);
        
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Arial", Font.PLAIN, 16));
        add(new JScrollPane(resultTextArea), BorderLayout.CENTER);
    }

    public void displayResults(String results) {
        resultTextArea.setText(results);
    }
}