package poker;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Uso: java -jar nombreProyecto.jar <apartado> <entrada.txt> <salida.txt>");
            return;
        }

        int apartado = Integer.parseInt(args[0]);
        String ficheroEntrada = args[1];
        String ficheroSalida = args[2];

        try (BufferedReader br = new BufferedReader(new FileReader(ficheroEntrada));
             PrintWriter pw = new PrintWriter(new FileWriter(ficheroSalida))) {

            String linea;
            while ((linea = br.readLine()) != null) {

                if (apartado == 1) {
                    // === Apartado 1 ===
                    Mano m = Mano.desdeString10(linea.trim());
                    Mano.Resultado r = m.evaluar();

                    pw.println(linea);
                    pw.println(" - Best hand: " + r.descripcion);
                    System.out.println(linea);
                    System.out.println(" - Best hand: " + r.descripcion);

                    for (String d : m.detectarDraws()) {
                        pw.println(" - Draw: " + d);
                        System.out.println(" - Draw: " + d);
                    }

                    pw.println();
                    System.out.println();

                } else if (apartado == 2) {
                    // === Apartado 2 ===
                    List<Carta> todas = Utils.parseLineaApartado2(linea.trim());
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

                    // imprimir
                    pw.println(linea);
                    pw.println(" - Best hand: " + mejor.descripcion + " with " + mejorCartas);
                    System.out.println(linea);
                    System.out.println(" - Best hand: " + mejor.descripcion + " with " + mejorCartas);

                    // draws solo si flop (5 cartas) o turn (6 cartas)
                    if (todas.size() == 5 || todas.size() == 6) {
                        Mano mBest = new Mano(mejorCartas);
                        for (String d : mBest.detectarDraws()) {
                            pw.println(" - Draw: " + d);
                            System.out.println(" - Draw: " + d);
                        }
                    }

                    pw.println();
                    System.out.println();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
