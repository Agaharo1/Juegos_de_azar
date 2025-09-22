package poker;

import java.util.*;

public class Utils {

    public static List<Carta> parseLineaApartado2(String linea) {
        String[] partes = linea.split(";");
        String propias = partes[0];   // ej: "AhAc"
        int n = Integer.parseInt(partes[1].trim()); // ej: 4
        String comunes = partes[2];   // ej: "As2s3h4d"

        List<Carta> todas = new ArrayList<>();

        for (int i = 0; i < propias.length(); i += 2) {
            todas.add(new Carta(propias.charAt(i), propias.charAt(i + 1)));
        }
        for (int i = 0; i < comunes.length(); i += 2) {
            todas.add(new Carta(comunes.charAt(i), comunes.charAt(i + 1)));
        }

        // opcional: validar que todas.size() == 2+n
        return todas;
    }

    public static List<List<Carta>> generarManosDe5(List<Carta> todas) {
        int m = todas.size();
        List<List<Carta>> res = new ArrayList<>();

        if (m == 5) {
            res.add(new ArrayList<>(todas));
        } else if (m == 6) {
            for (int out = 0; out < 6; out++) {
                List<Carta> mano = new ArrayList<>();
                for (int i = 0; i < 6; i++) if (i != out) mano.add(todas.get(i));
                res.add(mano);
            }
        } else if (m == 7) {
            for (int i = 0; i < 7; i++) {
                for (int j = i + 1; j < 7; j++) {
                    List<Carta> mano = new ArrayList<>();
                    for (int k = 0; k < 7; k++) if (k != i && k != j) mano.add(todas.get(k));
                    res.add(mano);
                }
            }
        }
        return res;
    }

    // Comparar dos resultados: categoría primero, luego desempates
    public static boolean mejorQue(Mano.Resultado a, Mano.Resultado b) {
        if (a == null) return false;
        if (b == null) return true;

        int cmp = a.categoria.compareTo(b.categoria);
        if (cmp != 0) {
            return cmp > 0; // porque el enum está ordenado de peor a mejor
        }
        // si empatan, comparar listas de enteros
        for (int i = 0; i < Math.min(a.desempate.size(), b.desempate.size()); i++) {
            if (!a.desempate.get(i).equals(b.desempate.get(i))) {
                return a.desempate.get(i) > b.desempate.get(i);
            }
        }
        return false;
    }
}
