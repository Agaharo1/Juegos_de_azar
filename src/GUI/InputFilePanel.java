package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InputFilePanel extends JPanel {
    private JTextField filePathField;
    private JButton loadButton;
    private JLabel statusLabel;

    public InputFilePanel() {
        setLayout(new FlowLayout());

        filePathField = new JTextField(20);
        loadButton = new JButton("Load Game Data");
        statusLabel = new JLabel(" ");

        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadGameData();
            }
        });

        add(new JLabel("File Path:"));
        add(filePathField);
        add(loadButton);
        add(statusLabel);
    }

    private void loadGameData() {
        String filePath = filePathField.getText();
        // Logic to load game data from the specified file path
        // For now, just update the status label
        statusLabel.setText("Loading data from: " + filePath);
    }
}