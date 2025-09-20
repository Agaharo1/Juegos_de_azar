package poker;

import java.io.*;

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
                Mano m = Mano.desdeString10(linea.trim());
                Mano.Resultado r = m.evaluar();

                // Imprimir en fichero
                pw.println(linea);
                pw.println(" - Best hand: " + r.descripcion);

                // Imprimir en consola
                System.out.println(linea);
                System.out.println(" - Best hand: " + r.descripcion);

                for (String d : m.detectarDraws()) {
                    pw.println(" - Draw: " + d);
                    System.out.println(" - Draw: " + d);
                }

                pw.println();
                System.out.println();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
