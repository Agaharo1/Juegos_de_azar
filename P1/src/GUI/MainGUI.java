package GUI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import poker.Mano;
import poker.Carta;
import poker.Partida;
import poker.Utils;
/**
 * GUI con representación visual de las cartas.
 * Funcionan los apartados 1, 2 y 3: muestra board, jugadores, mejores jugadas y draws cuando corresponda.
 */
public class MainGUI extends JFrame {
    private JComboBox<String> apartadoCombo;
    private JTextField inputField;
    private JButton browseBtn;
    private JButton runBtn;
    private JButton saveBtn;
    private JTextArea outputArea;
    private JFileChooser fileChooser;
    private JPanel boardPanel;
    private JPanel playersPanel;

    private File currentInputFile;
    private String lastOutputText = "";

    public MainGUI() {
        super("Práctica 1 — Evaluador de manos (GUI)");
        initComponents();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel top = new JPanel(new BorderLayout(8, 8));

        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        apartadoCombo = new JComboBox<>(new String[]{"1 - 5 cartas", "2 - 2 cartas + board", "3 - Varios jugadores"});
        leftTop.add(new JLabel("Apartado:"));
        leftTop.add(apartadoCombo);

        inputField = new JTextField(40);
        inputField.setEditable(false);
        browseBtn = new JButton("Examinar...");

        leftTop.add(new JLabel("Entrada:"));
        leftTop.add(inputField);
        leftTop.add(browseBtn);

        top.add(leftTop, BorderLayout.CENTER);

        runBtn = new JButton("Ejecutar");
        saveBtn = new JButton("Guardar salida...");
        saveBtn.setEnabled(false);

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rightTop.add(runBtn);
        rightTop.add(saveBtn);

        top.add(rightTop, BorderLayout.EAST);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.4);

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBorder(BorderFactory.createTitledBorder("Mesa / Jugadores"));

        boardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        boardPanel.setPreferredSize(new Dimension(600, 120));
        boardPanel.setBackground(new Color(0, 102, 0));
        left.add(boardPanel, BorderLayout.NORTH);

        playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        JScrollPane plScr = new JScrollPane(playersPanel);
        left.add(plScr, BorderLayout.CENTER);

        split.setLeftComponent(left);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBorder(BorderFactory.createTitledBorder("Salida"));
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane outScr = new JScrollPane(outputArea);
        right.add(outScr, BorderLayout.CENTER);

        split.setRightComponent(right);

        getContentPane().setLayout(new BorderLayout(8, 8));
        getContentPane().add(top, BorderLayout.NORTH);
        getContentPane().add(split, BorderLayout.CENTER);

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));

        browseBtn.addActionListener(e -> onBrowse());
        runBtn.addActionListener(e -> onRun());
        saveBtn.addActionListener(e -> onSave());
    }

    private void onBrowse() {
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            currentInputFile = fileChooser.getSelectedFile();
            inputField.setText(currentInputFile.getAbsolutePath());
        }
    }

    private void onRun() {
        if (currentInputFile == null || !currentInputFile.exists()) {
            JOptionPane.showMessageDialog(this, "Selecciona un fichero válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int apartado = apartadoCombo.getSelectedIndex() + 1;
        boardPanel.removeAll();
        playersPanel.removeAll();
        outputArea.setText("");
        lastOutputText = "";
        saveBtn.setEnabled(false);

        try (BufferedReader br = new BufferedReader(new FileReader(currentInputFile))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                if (apartado == 1) procesarApartado1(linea);
                else if (apartado == 2) procesarApartado2(linea);
                else if (apartado == 3) procesarApartado3(linea);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        boardPanel.revalidate();
        boardPanel.repaint();
        playersPanel.revalidate();
        playersPanel.repaint();
        saveBtn.setEnabled(true);
    }

    private void procesarApartado1(String linea) {
        Mano m = Mano.desdeString10(linea);
        Mano.Resultado r = m.evaluar();

        outputArea.append(linea + "\n");
        outputArea.append(" - Best hand: " + r.descripcion + "\n");
        lastOutputText += linea + "\n";
        lastOutputText += " - Best hand: " + r.descripcion + "\n";

        for (String d : m.detectarDraws()) {
            outputArea.append(" - Draw: " + d + "\n");
            lastOutputText += " - Draw: " + d + "\n";
        }

        outputArea.append("\n");
        lastOutputText += "\n";

        mostrarBoard(linea);
    }

    private void procesarApartado2(String linea) {
        List<Carta> todas = Utils.parseLineaApartado2(linea);
        List<List<Carta>> manos5 = Utils.generarManosDe5(todas);

        Mano.Resultado mejor = null;
        List<Carta> mejorCartas = null;

        for (List<Carta> subset : manos5) {
            Mano m = new Mano(subset);
            Mano.Resultado r = m.evaluar();
            if (Utils.mejorQue(r, mejor)) {
                mejor = r;
                mejorCartas = subset;
            }
        }

        outputArea.append(linea + "\n");
        outputArea.append(" - Best hand: " + mejor.descripcion + " with " + Utils.cartasAString(mejorCartas) + "\n");
        lastOutputText += linea + "\n";
        lastOutputText += " - Best hand: " + mejor.descripcion + " with " + Utils.cartasAString(mejorCartas) + "\n";

        if (todas.size() == 5 || todas.size() == 6) {
            Mano mBest = new Mano(mejorCartas);
            for (String d : mBest.detectarDraws()) {
                outputArea.append(" - Draw: " + d + "\n");
                lastOutputText += " - Draw: " + d + "\n";
            }
        }

        outputArea.append("\n");
        lastOutputText += "\n";

        mostrarBoardApartado2(linea);
    }

    private void procesarApartado3(String linea) {
        Partida p = Utils.parseLineaApartado3(linea);

        // calcular mejores manos de cada jugador
        for (int i = 0; i < p.n; i++) {
            List<Carta> siete = new ArrayList<>(7);
            siete.addAll(p.manos.get(i));
            siete.addAll(p.comunes);

            List<List<Carta>> manos5 = Utils.generarManosDe5(siete);

            Mano.Resultado mejorJ = null;
            List<Carta> mejorCincoJ = null;

            for (List<Carta> subset : manos5) {
                Mano m = new Mano(subset);
                Mano.Resultado r = m.evaluar();
                if (Utils.mejorQue(r, mejorJ)) {
                    mejorJ = r;
                    mejorCincoJ = subset;
                }
            }

            p.resultados.add(mejorJ);
            p.mejoresCinco.add(mejorCincoJ);
        }

        // ordenar
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < p.n; i++) idx.add(i);
        idx.sort((i, j) -> {
            Mano.Resultado a = p.resultados.get(i);
            Mano.Resultado b = p.resultados.get(j);
            if (a == null && b == null) return 0;
            if (a == null) return 1;
            if (b == null) return -1;
            return Utils.mejorQue(a, b) ? -1 : (Utils.mejorQue(b, a) ? 1 : 0);
        });

        outputArea.append(linea + "\n");
        lastOutputText += linea + "\n";

        // mostrar board
        boardPanel.removeAll();
        for (Carta c : p.comunes) {
            boardPanel.add(loadCardImage(c.toString()));
        }

        // mostrar jugadores ordenados
        playersPanel.removeAll();
        for (int i : idx) {
            String id = p.ids.get(i);
            Mano.Resultado r = p.resultados.get(i);
            List<Carta> mejores = p.mejoresCinco.get(i);

            String texto = id + ": " + Utils.cartasAString(mejores) + " (" + Utils.descripcionDetallada(r) + ")";
            outputArea.append(texto + "\n");
            lastOutputText += texto + "\n";

            JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            fila.add(new JLabel(id + ":"));
            for (Carta c : p.manos.get(i)) {
                fila.add(loadCardImage(c.toString()));
            }
            playersPanel.add(fila);
        }

        outputArea.append("\n");
        lastOutputText += "\n";
    }

    private void mostrarBoard(String cartas) {
        boardPanel.removeAll();
        for (int i = 0; i < cartas.length(); i += 2) {
            String carta = cartas.substring(i, i + 2);
            boardPanel.add(loadCardImage(carta));
        }
    }

    private void mostrarBoardApartado2(String linea) {
        try {
            String[] partes = linea.split(";");
            String hole = partes[0];
            String comunes = partes[2];

            boardPanel.removeAll();
            for (int i = 0; i < comunes.length(); i += 2) {
                String carta = comunes.substring(i, i + 2);
                boardPanel.add(loadCardImage(carta));
            }
            playersPanel.removeAll();
            JPanel jugador = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            jugador.add(new JLabel("Jugador:"));
            for (int i = 0; i < hole.length(); i += 2) {
                String carta = hole.substring(i, i + 2);
                jugador.add(loadCardImage(carta));
            }
            playersPanel.add(jugador);
        } catch (Exception e) {
            playersPanel.add(new JLabel("Error parseando apartado 2: " + e.getMessage()));
        }
    }

    private JLabel loadCardImage(String code) {
        try {
            String path = "resources/cartas/" + code + ".png";
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(60, 90, Image.SCALE_SMOOTH);
            return new JLabel(new ImageIcon(img));
        } catch (Exception e) {
            return new JLabel(code);
        }
    }

    private void onSave() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("salida.txt"));
        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File out = chooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(new FileWriter(out))) {
                pw.print(lastOutputText);
                JOptionPane.showMessageDialog(this, "Guardado en: " + out.getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}