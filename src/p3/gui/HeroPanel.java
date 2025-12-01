package p3.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.SpinnerNumberModel;

/**
 * Panel principal del héroe: selección de rango (texto o porcentaje)
 * y opciones de aleatoriedad de cartas/board.
 */
public class HeroPanel extends JPanel {

    // --- Controles principales ---
    private JRadioButton rbTextualRange;
    private JRadioButton rbPercentageRange;

    private JTextField textualRangeField;
    private JComboBox<String> rankingCombo;
    private JSpinner percentageSpinner;

    private JCheckBox cbRandomCards;
    private JCheckBox cbRandomBoard;

    public HeroPanel() {
        initializeComponents();
        updateRangeFields();
    }

    /** Permite enganchar listeners externos al check de Random Board. */
    public void addRandomBoardListener(ItemListener l) {
        cbRandomBoard.addItemListener(l);
    }

    // =====================================================================
    // INIT UI
    // =====================================================================

    private void initializeComponents() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(UiTheme.BG_PANEL);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));

        add(createRangeTypePanel());
        add(Box.createHorizontalStrut(15));

        add(createTextualPanel());
        add(Box.createHorizontalStrut(15));

        add(createRankingPanel());
        add(Box.createHorizontalStrut(15));

        add(createPercentagePanel());
        add(Box.createHorizontalStrut(15));

        add(createRandomPanel());
        add(Box.createHorizontalGlue());
    }

    // =====================================================================
    // SUB-PANELES
    // =====================================================================

    private JPanel createRangeTypePanel() {
        JPanel p = createVerticalPanel();
        p.add(label("Range Type", UiTheme.F_10B));

        rbTextualRange = radio("Textual", true);
        rbPercentageRange = radio("Percentage", false);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbTextualRange);
        bg.add(rbPercentageRange);

        rbTextualRange.addActionListener(e -> updateRangeFields());
        rbPercentageRange.addActionListener(e -> updateRangeFields());

        p.add(rbTextualRange);
        p.add(rbPercentageRange);
        return p;
    }

    private JPanel createTextualPanel() {
        JPanel p = createVerticalPanel();
        p.add(label("Textual Range:", UiTheme.F_9));

        textualRangeField = createTextField("AA,KK,QQ,JJ", 140);
        p.add(textualRangeField);
        return p;
    }

    private JPanel createRankingPanel() {
        JPanel p = createVerticalPanel();
        p.add(label("Ranking:", UiTheme.F_9));

        rankingCombo = new JComboBox<>(new String[]{"Sklansky-Chubukov", "Custom"});
        configureCombo(rankingCombo, 150);
        rankingCombo.setEnabled(false);

        p.add(rankingCombo);
        return p;
    }

    private JPanel createPercentagePanel() {
        JPanel p = createVerticalPanel();
        p.add(label("Percentage:", UiTheme.F_9));

        percentageSpinner = new JSpinner(new SpinnerNumberModel(25, 1, 100, 1));
        configureSpinner(percentageSpinner, 90);
        percentageSpinner.setEnabled(false);

        p.add(percentageSpinner);
        return p;
    }

    private JPanel createRandomPanel() {
        JPanel p = createVerticalPanel();

        cbRandomCards = check("Random Cards", true);
        cbRandomBoard = check("Random Board", true);

        p.add(cbRandomCards);
        p.add(cbRandomBoard);
        return p;
    }

    // =====================================================================
    // ENABLE / DISABLE
    // =====================================================================

    private void updateRangeFields() {
        boolean textual = rbTextualRange.isSelected();

        textualRangeField.setEnabled(textual);

        rankingCombo.setEnabled(!textual);
        percentageSpinner.setEnabled(!textual);
    }

    // =====================================================================
    // FACTORÍAS Y HELPERS
    // =====================================================================

    private JPanel createVerticalPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UiTheme.BG_PANEL);
        return p;
    }

    private JLabel label(String text, java.awt.Font font) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(UiTheme.FG_TEXT_DIM);
        return l;
    }

    private JRadioButton radio(String text, boolean selected) {
        JRadioButton rb = new JRadioButton(text, selected);
        rb.setFont(UiTheme.F_9);
        rb.setBackground(UiTheme.BG_PANEL);
        rb.setForeground(UiTheme.FG_TEXT);
        return rb;
    }

    private JCheckBox check(String text, boolean sel) {
        JCheckBox cb = new JCheckBox(text);
        cb.setSelected(sel);
        cb.setFont(UiTheme.F_9);
        cb.setBackground(UiTheme.BG_PANEL);
        cb.setForeground(UiTheme.FG_TEXT);
        return cb;
    }

    private JTextField createTextField(String text, int width) {
        JTextField tf = new JTextField(text);
        tf.setMaximumSize(new Dimension(width, 25));
        tf.setFont(UiTheme.F_9);
        tf.setBackground(UiTheme.BG_INPUT);
        tf.setForeground(UiTheme.FG_TEXT);
        tf.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER, 1));
        tf.setCaretColor(new Color(150, 150, 150));
        return tf;
    }

    private void configureCombo(JComboBox<String> combo, int width) {
        combo.setMaximumSize(new Dimension(width, 25));
        combo.setFont(UiTheme.F_9);
        combo.setBackground(UiTheme.BG_INPUT);
        combo.setForeground(UiTheme.FG_TEXT);
    }

    private void configureSpinner(JSpinner spinner, int width) {
        spinner.setMaximumSize(new Dimension(width, 25));

        JSpinner.DefaultEditor ed = (JSpinner.DefaultEditor) spinner.getEditor();
        ed.getTextField().setFont(UiTheme.F_9);
        ed.getTextField().setBackground(UiTheme.BG_INPUT);
        ed.getTextField().setForeground(UiTheme.FG_TEXT);
    }

    // =====================================================================
    // GETTERS
    // =====================================================================

    public boolean isTextualSelected()    { return rbTextualRange.isSelected(); }
    public boolean isPercentageSelected() { return rbPercentageRange.isSelected(); }

    public String getTextualRange()       { return textualRangeField.getText(); }
    public int getPercentage()            { return (int) percentageSpinner.getValue(); }

    public boolean isRandomCards()        { return cbRandomCards.isSelected(); }
    public boolean isRandomBoard()        { return cbRandomBoard.isSelected(); }
}
