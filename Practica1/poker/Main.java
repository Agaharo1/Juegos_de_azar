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

                    // Declaradas al MISMO nivel que los println
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

                    // imprimir (APARTADO 2)
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

                } else if (apartado == 3) {
                    // === Apartado 3 ===
                    Partida p = Utils.parseLineaApartado3(linea.trim());

                    // 1) Mejor mano de cada jugador (de 7 cartas → 5)
                    for (int i = 0; i < p.n; i++) {
                        // 7 cartas = 2 propias + 5 comunes
                        List<Carta> siete = new ArrayList<>(7);
                        siete.addAll(p.manos.get(i));
                        siete.addAll(p.comunes);

                        // 21 combinaciones de 5
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

                        // Guardamos resultado y las 5 cartas ganadoras
                        p.resultados.add(mejorJ);
                        p.mejoresCinco.add(mejorCincoJ);
                    }

                    // 2) Ordenar jugadores de mejor a peor
                    List<Integer> idx = new ArrayList<>();
                    for (int i = 0; i < p.n; i++) idx.add(i);

                    idx.sort((i, j) -> {
                        Mano.Resultado a = p.resultados.get(i);
                        Mano.Resultado b = p.resultados.get(j);
                        if (a == null && b == null) return 0;
                        if (a == null) return 1;
                        if (b == null) return -1;
                        // mejor primero
                        return Utils.mejorQue(a, b) ? -1 : (Utils.mejorQue(b, a) ? 1 : 0);
                    });

                    // 3) Imprimir (APARTADO 3)
                    pw.println(linea);
                    System.out.println(linea);

                    for (int i : idx) {
                        String id = p.ids.get(i);
                        Mano.Resultado r = p.resultados.get(i);
                        List<Carta> mejores = p.mejoresCinco.get(i);

                        String texto = id + ": " + mejores + " (" + Utils.descripcionDetallada(r) + ")";
                        pw.println(texto);
                        System.out.println(texto);
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
