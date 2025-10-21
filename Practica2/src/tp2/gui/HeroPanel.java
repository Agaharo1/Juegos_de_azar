package tp2.gui;

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
        updateRangeFields(); // asegura enabled/disabled correcto al iniciar
    }


    private void initializeComponents() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(UiTheme.BG_PANEL);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER,1),
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
        panel.setBackground(UiTheme.BG_PANEL);

        JLabel typeLabel = new JLabel("Range Type");
        typeLabel.setFont(UiTheme.F_10B);
        typeLabel.setForeground(UiTheme.FG_TEXT_DIM);
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
        panel.setBackground(UiTheme.BG_PANEL);
        JLabel rangeLabel = new JLabel("Textual Range:");
        rangeLabel.setFont(UiTheme.F_9);
        rangeLabel.setForeground(UiTheme.FG_TEXT_DIM);
        panel.add(rangeLabel);
        textualRangeField = createTextField("AA,KK,QQ,JJ",140);
        panel.add(textualRangeField);
        return panel;
    }

    private JPanel createRankingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UiTheme.BG_PANEL);
        JLabel rankingLabel = new JLabel("Ranking:");
        rankingLabel.setFont(UiTheme.F_9);
        rankingLabel.setForeground(UiTheme.FG_TEXT_DIM);
        panel.add(rankingLabel);
        rankingCombo = new JComboBox<>(new String[]{"Sklansky-Chubukov","Custom"});
        rankingCombo.setMaximumSize(new Dimension(150,25));
        rankingCombo.setEnabled(false);
        rankingCombo.setFont(UiTheme.F_9);
        rankingCombo.setBackground(UiTheme.BG_INPUT);
        rankingCombo.setForeground(UiTheme.FG_TEXT);
        panel.add(rankingCombo);
        return panel;
    }

    private JPanel createPercentagePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UiTheme.BG_PANEL);
        JLabel percentageLabel = new JLabel("Percentage:");
        percentageLabel.setFont(UiTheme.F_9);
        percentageLabel.setForeground(UiTheme.FG_TEXT_DIM);
        panel.add(percentageLabel);
        percentageSpinner = new JSpinner(new SpinnerNumberModel(25,1,100,1));
        percentageSpinner.setMaximumSize(new Dimension(90,25));
        percentageSpinner.setEnabled(false);
        ((JSpinner.DefaultEditor)percentageSpinner.getEditor()).getTextField().setFont(UiTheme.F_9);
        ((JSpinner.DefaultEditor)percentageSpinner.getEditor()).getTextField().setBackground(UiTheme.BG_INPUT);
        ((JSpinner.DefaultEditor)percentageSpinner.getEditor()).getTextField().setForeground(UiTheme.FG_TEXT);
        panel.add(percentageSpinner);
        return panel;
    }

    private JPanel createCheckPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UiTheme.BG_PANEL);
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
        rb.setFont(UiTheme.F_9);
        rb.setBackground(UiTheme.BG_PANEL);
        rb.setForeground(UiTheme.FG_TEXT);
    }

    private void styleCheckBox(JCheckBox cb){
        cb.setFont(UiTheme.F_9);
        cb.setBackground(UiTheme.BG_PANEL);
        cb.setForeground(UiTheme.FG_TEXT);
    }

    private JTextField createTextField(String text,int width){
        JTextField field = new JTextField(text);
        field.setMaximumSize(new Dimension(width,25));
        field.setFont(UiTheme.F_9);
        field.setBackground(UiTheme.BG_INPUT);
        field.setForeground(UiTheme.FG_TEXT);
        field.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER,1));
        field.setCaretColor(new Color(150,150,150)); // este puede quedarse literal
        return field;
    }

    private void updateRangeFields(){
        textualRangeField.setEnabled(rbTextualRange.isSelected());
        rankingCombo.setEnabled(rbPercentageRange.isSelected());
        percentageSpinner.setEnabled(rbPercentageRange.isSelected());
    }
}
