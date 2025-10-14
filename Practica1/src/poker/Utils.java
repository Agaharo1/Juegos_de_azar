package poker;

import java.util.*;

public class Utils {
	
	// Convierte un número de carta a texto
    public static String valorToTexto(int v) {
        switch (v) {
            case 14: return "Aces";
            case 13: return "Kings";
            case 12: return "Queens";
            case 11: return "Jacks";
            case 10: return "Tens";
            default: return v + "s";
        }
    }

    // Genera descripción detallada en función de la categoría y desempate
    public static String descripcionDetallada(Mano.Resultado r) {
        switch (r.categoria) {
            case ONE_PAIR:
                return "Pair of " + valorToTexto(r.desempate.get(0));
            case TWO_PAIR:
                return "Two Pair (" + valorToTexto(r.desempate.get(0))
                       + " and " + valorToTexto(r.desempate.get(1)) + ")";
            case THREE_OF_A_KIND:
                return "Three of " + valorToTexto(r.desempate.get(0));
            case FOUR_OF_A_KIND:
                return "Four of " + valorToTexto(r.desempate.get(0));
            case FULL_HOUSE:
                return "Full House (" + valorToTexto(r.desempate.get(0))
                       + " over " + valorToTexto(r.desempate.get(1)) + ")";
            default:
                // para los demás usamos la descripción original
                return r.descripcion;
        }
    }

    public static String cartasAString(List<Carta> cartas) {
        if (cartas == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < cartas.size(); i++) {
            sb.append(cartas.get(i).toString());
            if (i < cartas.size() - 1) sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }

	
    public static Partida parseLineaApartado3(String linea) {
        String[] partes = linea.trim().split(";");
        int n = Integer.parseInt(partes[0]);

        List<String> ids = new ArrayList<>();
        List<List<Carta>> manos = new ArrayList<>();

        // jugadores
        for (int i = 1; i <= n; i++) {
            String token = partes[i];      // ej: "J1AhAc"
            String id = token.substring(0, 2); // "J1"
            ids.add(id);

            String cartasStr = token.substring(2); // "AhAc"
            List<Carta> hole = new ArrayList<>();
            for (int j = 0; j < cartasStr.length(); j += 2) {
                hole.add(new Carta(cartasStr.charAt(j), cartasStr.charAt(j + 1)));
            }
            manos.add(hole);
        }

        // comunes
        String comunesStr = partes[n + 1];
        List<Carta> comunes = new ArrayList<>();
        for (int i = 0; i < comunesStr.length(); i += 2) {
            comunes.add(new Carta(comunesStr.charAt(i), comunesStr.charAt(i + 1)));
        }

        return new Partida(n, ids, manos, comunes);
    }


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
        for (int i = 0; i < a.desempate.size(); i++) {
            if (!a.desempate.get(i).equals(b.desempate.get(i))) {
                return a.desempate.get(i) > b.desempate.get(i);
            }
        }
        return false;
    }
}
