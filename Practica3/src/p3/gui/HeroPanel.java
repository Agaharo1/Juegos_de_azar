package p3.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
/**
 * Panel de controles del "héroe" (el jugador principal).
 * Aquí el usuario elige cómo definir su rango (texto o porcentaje),
 * y si quiere cartas/board aleatorios o no.
 *
 * Estructura visual (en una sola fila):
 * [Tipo de rango]  [Texto de rango]  [Selector de ranking]  [Porcentaje]  [Checks aleatorio]
 */
public class HeroPanel extends JPanel {

    // Botones para elegir el tipo de rango:
    // - Texto (por ejemplo: "AA,KK,AKs")
    // - Porcentaje (por ejemplo: 25%)
    private JRadioButton rbTextualRange;
    private JRadioButton rbPercentageRange;

    // Campo donde se escribe el rango en texto
    private JTextField textualRangeField;

    // Desplegable para escoger el tipo de ranking (por ahora solo informativo)
    private JComboBox<String> rankingCombo;

    // Control para elegir un número 1..100 (porcentaje del ranking)
    private JSpinner percentageSpinner;

    // Casillas para decidir si las cartas y el board salen aleatorios
    private JCheckBox cbRandomCards;
    private JCheckBox cbRandomBoard;

    /**
     * Construye el panel y deja todo listo para usarse.
     * También actualiza qué campos están activos según el tipo elegido.
     */
    public HeroPanel() {
        initializeComponents();
        updateRangeFields(); // asegura que lo que toca esté activo/inactivo al iniciar
    }
    
    public void addRandomBoardListener(ItemListener l) {
    	 cbRandomBoard.addItemListener(l);
    	}


    /**
     * Crea y coloca todos los bloques del panel (de izquierda a derecha).
     * También aplica colores y márgenes para que se vea ordenado.
     */
    private void initializeComponents() {
        // Coloca todo en una fila
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(UiTheme.BG_PANEL);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER,1),
                new EmptyBorder(8,10,8,10)
        ));

        // Orden de los bloques en la fila
        add(createRangeTypePanel());   // Elegir "Textual" o "Percentage"
        add(Box.createHorizontalStrut(15));
        add(createTextualPanel());     // Campo para escribir el rango textual
        add(Box.createHorizontalStrut(15));
        add(createRankingPanel());     // Desplegable del tipo de ranking
        add(Box.createHorizontalStrut(15));
        add(createPercentagePanel());  // Selector de porcentaje
        add(Box.createHorizontalStrut(15));
        add(createCheckPanel());       // Opciones de aleatoriedad
        add(Box.createHorizontalGlue()); // Empuja todo hacia la izquierda
    }

    /**
     * Bloque: selector del tipo de rango (Texto o Porcentaje).
     * Al cambiar la opción, se activan/desactivan los campos correspondientes.
     */
    private JPanel createRangeTypePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UiTheme.BG_PANEL);

        JLabel typeLabel = new JLabel("Range Type");
        typeLabel.setFont(UiTheme.F_10B);
        typeLabel.setForeground(UiTheme.FG_TEXT_DIM);
        panel.add(typeLabel);

        ButtonGroup bg = new ButtonGroup(); // asegura que solo una opción esté marcada

        rbTextualRange = new JRadioButton("Textual", true);   // marcada por defecto
        rbPercentageRange = new JRadioButton("Percentage", false);

        styleRadioButton(rbTextualRange);
        styleRadioButton(rbPercentageRange);

        // Añadimos las dos opciones al grupo
        bg.add(rbTextualRange);
        bg.add(rbPercentageRange);

        // Cuando cambia la opción, actualizamos qué campos están activos
        rbTextualRange.addActionListener(e -> updateRangeFields());
        rbPercentageRange.addActionListener(e -> updateRangeFields());

        panel.add(rbTextualRange);
        panel.add(rbPercentageRange);
        return panel;
    }

    /**
     * Bloque: campo de texto para escribir el rango manualmente.
     * Ejemplo de contenido: "AA,KK,QQ,JJ"
     */
    private JPanel createTextualPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UiTheme.BG_PANEL);

        JLabel rangeLabel = new JLabel("Textual Range:");
        rangeLabel.setFont(UiTheme.F_9);
        rangeLabel.setForeground(UiTheme.FG_TEXT_DIM);
        panel.add(rangeLabel);

        // Campo de texto con un ejemplo por defecto
        textualRangeField = createTextField("AA,KK,QQ,JJ", 140);
        panel.add(textualRangeField);
        return panel;
    }

    /**
     * Bloque: selector del "tipo de ranking".
     * Por ahora solo es decorativo (no cambia el cálculo por dentro),
     * pero deja el diseño preparado si en el futuro se añaden más tipos.
     */
    private JPanel createRankingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UiTheme.BG_PANEL);

        JLabel rankingLabel = new JLabel("Ranking:");
        rankingLabel.setFont(UiTheme.F_9);
        rankingLabel.setForeground(UiTheme.FG_TEXT_DIM);
        panel.add(rankingLabel);

        rankingCombo = new JComboBox<>(new String[]{"Sklansky-Chubukov", "Custom"});
        rankingCombo.setMaximumSize(new Dimension(150, 25));
        rankingCombo.setEnabled(false); // desactivado al iniciar (solo se usa con porcentaje)
        rankingCombo.setFont(UiTheme.F_9);
        rankingCombo.setBackground(UiTheme.BG_INPUT);
        rankingCombo.setForeground(UiTheme.FG_TEXT);
        panel.add(rankingCombo);
        return panel;
    }

    /**
     * Bloque: selector de porcentaje (1 a 100).
     * Se usa cuando el tipo de rango es "Percentage".
     */
    private JPanel createPercentagePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UiTheme.BG_PANEL);

        JLabel percentageLabel = new JLabel("Percentage:");
        percentageLabel.setFont(UiTheme.F_9);
        percentageLabel.setForeground(UiTheme.FG_TEXT_DIM);
        panel.add(percentageLabel);

        // Rueda numérica: empieza en 25, mínimo 1, máximo 100, paso 1
        percentageSpinner = new JSpinner(new SpinnerNumberModel(25, 1, 100, 1));
        percentageSpinner.setMaximumSize(new Dimension(90, 25));
        percentageSpinner.setEnabled(false); // desactivado al iniciar (solo se usa con porcentaje)

        // Ajuste de aspecto para que pegue con el tema
        ((JSpinner.DefaultEditor) percentageSpinner.getEditor()).getTextField().setFont(UiTheme.F_9);
        ((JSpinner.DefaultEditor) percentageSpinner.getEditor()).getTextField().setBackground(UiTheme.BG_INPUT);
        ((JSpinner.DefaultEditor) percentageSpinner.getEditor()).getTextField().setForeground(UiTheme.FG_TEXT);

        panel.add(percentageSpinner);
        return panel;
    }

    /**
     * Bloque: casillas para decidir si las cartas y el board salen aleatorios.
     * - Random Cards: reparte cartas al héroe de forma automática.
     * - Random Board: saca flop/turn/river de forma automática.
     */
    private JPanel createCheckPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UiTheme.BG_PANEL);

        cbRandomCards = new JCheckBox("Random Cards");
        cbRandomCards.setSelected(true); // activado por defecto
        styleCheckBox(cbRandomCards);

        cbRandomBoard = new JCheckBox("Random Board");
        cbRandomBoard.setSelected(true); // activado por defecto
        styleCheckBox(cbRandomBoard);

        panel.add(cbRandomCards);
        panel.add(cbRandomBoard);
        return panel;
    }

    /**
     * Aplica el estilo visual a un botón de opción (colores y letra).
     */
    private void styleRadioButton(JRadioButton rb) {
        rb.setFont(UiTheme.F_9);
        rb.setBackground(UiTheme.BG_PANEL);
        rb.setForeground(UiTheme.FG_TEXT);
    }

    /**
     * Aplica el estilo visual a una casilla de verificación (colores y letra).
     */
    private void styleCheckBox(JCheckBox cb){
        cb.setFont(UiTheme.F_9);
        cb.setBackground(UiTheme.BG_PANEL);
        cb.setForeground(UiTheme.FG_TEXT);
    }

    /**
     * Crea un campo de texto con el ancho deseado y colores del tema.
     * @param text  texto que aparece al inicio a modo de ejemplo
     * @param width ancho máximo del campo
     */
    private JTextField createTextField(String text,int width){
        JTextField field = new JTextField(text);
        field.setMaximumSize(new Dimension(width,25));
        field.setFont(UiTheme.F_9);
        field.setBackground(UiTheme.BG_INPUT);
        field.setForeground(UiTheme.FG_TEXT);
        field.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER,1));
        field.setCaretColor(new Color(150,150,150)); // color del cursor al escribir
        return field;
    }

    /**
     * Activa o desactiva los campos según el tipo de rango elegido:
     * - Si se elige "Textual": se activa el campo de texto y se desactiva el porcentaje.
     * - Si se elige "Percentage": se activa el porcentaje y se desactiva el texto.
     */
    private void updateRangeFields(){
        textualRangeField.setEnabled(rbTextualRange.isSelected());
        rankingCombo.setEnabled(rbPercentageRange.isSelected());
        percentageSpinner.setEnabled(rbPercentageRange.isSelected());
    }

    // ==== Accesos "públicos" para que otras partes de la app puedan leer estos valores ====

    /** Devuelve true si está elegido el modo "Textual". */
    public boolean isTextualSelected()   { return rbTextualRange.isSelected(); }

    /** Devuelve true si está elegido el modo "Percentage". */
    public boolean isPercentageSelected(){ return rbPercentageRange.isSelected(); }

    /** Devuelve el texto que ha escrito el usuario en el campo de rango. */
    public String  getTextualRange()     { return textualRangeField.getText(); }

    /** Devuelve el porcentaje elegido en la rueda numérica (1..100). */
    public int     getPercentage()       { return (int) percentageSpinner.getValue(); }

    /** Devuelve true si las cartas del héroe deben salir aleatorias. */
    public boolean isRandomCards()       { return cbRandomCards.isSelected(); }

    /** Devuelve true si el board (flop/turn/river) debe salir aleatorio. */
    public boolean isRandomBoard()       { return cbRandomBoard.isSelected(); }
}
