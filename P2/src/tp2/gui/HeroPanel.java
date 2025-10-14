package GUI;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class HeroPanel extends JPanel {
    private JRadioButton rbTextualRange;
    private JRadioButton rbPercentageRange;
    private JTextField textualRangeField;
    private JComboBox<String> rankingCombo;
    private JSpinner percentageSpinner;
    private JCheckBox cbRandomCards;
    private JCheckBox cbRandomBoard;

    public HeroPanel() {
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(new Color(30,40,55));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(75,85,99),1),
                new EmptyBorder(8,10,8,10)
        ));

        add(createRangeTypePanel());
        add(Box.createHorizontalStrut(15));
        add(createTextualPanel());
        add(Box.createHorizontalStrut(15));
        add(createRankingPanel());
        add(Box.createHorizontalStrut(15));
        add(createPercentagePanel());
        add(Box.createHorizontalStrut(15));
        add(createCheckPanel());
        add(Box.createHorizontalGlue());
    }

    private JPanel createRangeTypePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30,40,55));

        JLabel typeLabel = new JLabel("Range Type");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD,10));
        typeLabel.setForeground(new Color(180,180,180));
        panel.add(typeLabel);

        ButtonGroup bg = new ButtonGroup();
        rbTextualRange = new JRadioButton("Textual",true);
        rbPercentageRange = new JRadioButton("Percentage",false);
        styleRadioButton(rbTextualRange); 
        styleRadioButton(rbPercentageRange);
        bg.add(rbTextualRange); 
        bg.add(rbPercentageRange);
        rbTextualRange.addActionListener(e->updateRangeFields());
        rbPercentageRange.addActionListener(e->updateRangeFields());

        panel.add(rbTextualRange); 
        panel.add(rbPercentageRange);
        return panel;
    }

    private JPanel createTextualPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30,40,55));
        JLabel rangeLabel = new JLabel("Textual Range:");
        rangeLabel.setFont(new Font("Segoe UI",Font.PLAIN,9));
        rangeLabel.setForeground(new Color(180,180,180));
        panel.add(rangeLabel);
        textualRangeField = createTextField("AA,KK,QQ,JJ",140);
        panel.add(textualRangeField);
        return panel;
    }

    private JPanel createRankingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30,40,55));
        JLabel rankingLabel = new JLabel("Ranking:");
        rankingLabel.setFont(new Font("Segoe UI",Font.PLAIN,9));
        rankingLabel.setForeground(new Color(180,180,180));
        panel.add(rankingLabel);
        rankingCombo = new JComboBox<>(new String[]{"Sklansky-Chubukov","Custom"});
        rankingCombo.setMaximumSize(new Dimension(150,25));
        rankingCombo.setEnabled(false);
        rankingCombo.setFont(new Font("Segoe UI",Font.PLAIN,9));
        rankingCombo.setBackground(new Color(50,60,80));
        rankingCombo.setForeground(new Color(220,220,220));
        panel.add(rankingCombo);
        return panel;
    }

    private JPanel createPercentagePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30,40,55));
        JLabel percentageLabel = new JLabel("Percentage:");
        percentageLabel.setFont(new Font("Segoe UI",Font.PLAIN,9));
        percentageLabel.setForeground(new Color(180,180,180));
        panel.add(percentageLabel);
        percentageSpinner = new JSpinner(new SpinnerNumberModel(25,1,100,1));
        percentageSpinner.setMaximumSize(new Dimension(90,25));
        percentageSpinner.setEnabled(false);
        ((JSpinner.DefaultEditor)percentageSpinner.getEditor()).getTextField().setFont(new Font("Segoe UI",Font.PLAIN,9));
        ((JSpinner.DefaultEditor)percentageSpinner.getEditor()).getTextField().setBackground(new Color(50,60,80));
        ((JSpinner.DefaultEditor)percentageSpinner.getEditor()).getTextField().setForeground(new Color(220,220,220));
        panel.add(percentageSpinner);
        return panel;
    }

    private JPanel createCheckPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30,40,55));
        cbRandomCards = new JCheckBox("Random Cards"); 
        cbRandomCards.setSelected(true); 
        styleCheckBox(cbRandomCards);
        cbRandomBoard = new JCheckBox("Random Board"); 
        cbRandomBoard.setSelected(true); 
        styleCheckBox(cbRandomBoard);
        panel.add(cbRandomCards); 
        panel.add(cbRandomBoard);
        return panel;
    }

    private void styleRadioButton(JRadioButton rb) {
        rb.setFont(new Font("Segoe UI",Font.PLAIN,9));
        rb.setBackground(new Color(30,40,55));
        rb.setForeground(new Color(220,220,220));
    }
    
    private void styleCheckBox(JCheckBox cb){
        cb.setFont(new Font("Segoe UI",Font.PLAIN,9));
        cb.setBackground(new Color(30,40,55));
        cb.setForeground(new Color(220,220,220));
    }
    
    private JTextField createTextField(String text,int width){
        JTextField field = new JTextField(text);
        field.setMaximumSize(new Dimension(width,25));
        field.setFont(new Font("Segoe UI",Font.PLAIN,9));
        field.setBackground(new Color(50,60,80));
        field.setForeground(new Color(220,220,220));
        field.setBorder(BorderFactory.createLineBorder(new Color(75,85,99),1));
        field.setCaretColor(new Color(150,150,150));
        return field;
    }
    
    private void updateRangeFields(){
        textualRangeField.setEnabled(rbTextualRange.isSelected());
        rankingCombo.setEnabled(rbPercentageRange.isSelected());
        percentageSpinner.setEnabled(rbPercentageRange.isSelected());
    }
}