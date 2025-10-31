package tp2.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import tp2.logic.HandUtils;

/**
 * Gestiona el ranking de las 169 manos iniciales (aprox. Sklansky)
 * y ofrece utilidades para:
 *  1) Devolver el ranking completo (para la GUI actual).
 *  2) Devolver el top por porcentaje (0..1) como lista de manos.
 *  3) Construir una "máscara" [13][13][2] que indica qué manos están dentro
 *     del top, separando offsuit (índice 0) y suited (índice 1).
 *
 * Nota sobre índices en la máscara:
 *  - Ejes 0..12 representan valores 2..A (ver RANKS = "23456789TJQKA").
 *  - [max][min][1] se usa para suited y [max][min][0] para offsuit.
 *  - Las parejas (AA, KK, ...) ocupan la diagonal [i][i] y se marcan en
 *    ambas capas [i][i][0] y [i][i][1] para simplificar.
 */
public final class RankingProvider {

    // Clase de utilidades: se evita crear instancias
    private RankingProvider() {}

    // Ranking de 169 combinaciones (ordenadas de mejor a peor).
    // Mezcla parejas, suited y offsuit. Es una aproximación suficiente para la práctica.
    private static final String[] RANKING_169 = {
        "AA","KK","AKs","QQ","AKo","JJ","AQs","TT","AQo","99","AJs","KQs",
        "88","AJo","KJs","77","ATs","KTs","QJs","66","ATo","QTs","55","KQo",
        "44","A9s","JTs","33","A8s","KJo","22","A7s","K9s","T9s","A5s","A6s",
        "QJo","KTo","A4s","A3s","A2s","K8s","Q9s","J9s","T8s","98s","K7s",
        "87s","K6s","Q8s","K5s","K4s","K3s","K2s","Q7s","Q6s","Q5s","J8s",
        "97s","76s","Q4s","Q3s","Q2s","T7s","65s","54s","J7s","86s","75s",
        "64s","T6s","T5s","T4s","T3s","T2s","53s","43s","J6s","J5s","J4s",
        "J3s","J2s","98o","87o","76o","65o","54o","K9o","Q9o","J9o","T9o",
        "A9o","K8o","Q8o","J8o","T8o","A8o","K7o","Q7o","J7o","T7o","A7o",
        "K6o","Q6o","J6o","T6o","A6o","K5o","Q5o","J5o","T5o","A5o","K4o",
        "Q4o","J4o","T4o","A4o","K3o","Q3o","J3o","T3o","A3o","K2o","Q2o",
        "J2o","T2o","A2o"
    };

    // Cadena para mapear letras de valor a índice 0..12 (2..A)
    private static final String RANKS = "23456789TJQKA";

    /**
     * Devuelve una copia del ranking completo (169 elementos).
     * Útil para compatibilidad con partes de la GUI que esperan una lista.
     */
    public static List<String> getRanking() {
        return new ArrayList<>(Arrays.asList(RANKING_169));
    }

    /**
     * Devuelve la lista de manos del top por porcentaje (0..1).
     * Ejemplos:
     *  - p = 0.25  → devuelve ~42 manos (el 25% superior).
     *  - p = 1.0   → devuelve las 169 manos.
     *  - p <= 0    → devuelve lista vacía.
     */
    public static List<String> getTopByPercent(double p) {
        int k = Math.min(RANKING_169.length, Math.max(0, (int)Math.ceil(RANKING_169.length * p)));
        if (k == 0) return Collections.emptyList();
        ArrayList<String> top = new ArrayList<>(k);
        for (int i = 0; i < k; i++) top.add(RANKING_169[i]);
        return top;
    }

    /**
     * Construye una máscara [13][13][2] marcando en true las manos dentro del top.
     * - percent en 0..1 (igual que getTopByPercent).
     * - Capa [..][..][1] = suited ; Capa [..][..][0] = offsuit.
     * - Las parejas se marcan en ambas capas de su casilla diagonal.
     */
    public static boolean[][][] getMaskForPercent(double percent) {
        boolean[][][] mask = new boolean[13][13][2];
        int limit = Math.min(RANKING_169.length, (int)Math.round(RANKING_169.length * percent));
        for (int i = 0; i < limit; i++) {
            mark(mask, RANKING_169[i]);
        }
        return mask;
    }
    
    /**
     * Devuelve true si una mano concreta está dentro del top por porcentaje dado.
     * Usa HandUtils.to169() para convertir la mano real a su forma textual.
     */
    public static boolean isInTopPercent(tp2.model.Hand hand, double percent) {
        String normalized = HandUtils.to169(hand);
        List<String> top = getTopByPercent(percent);
        return top.contains(normalized);
    }


    // ======================
    //    FUNCIONES APOYO
    // ======================

    /**
     * Marca en la máscara la mano dada (por ejemplo "AKs", "AQo" o "TT").
     * Convierte las letras de valor a índices, detecta si es suited/offsuit/pareja
     * y enciende la casilla correspondiente.
     */
    private static void mark(boolean[][][] mask, String hand) {
        char r1 = hand.charAt(0);          // valor 1 (ej: 'A')
        char r2 = hand.charAt(1);          // valor 2 (ej: 'K' o 'A' si pareja)
        int i = RANKS.indexOf(r1);         // índice 0..12
        int j = RANKS.indexOf(r2);         // índice 0..12
        if (i < 0 || j < 0) return;        // seguridad: si algo raro, no marcamos

        boolean suited  = hand.endsWith("s"); // termina en 's' → suited
        boolean offsuit = hand.endsWith("o"); // termina en 'o' → offsuit

        // Para mantener una sola casilla por mano no pareja:
        // usamos la convención [mayor][menor]
        int hi = Math.max(i, j);
        int lo = Math.min(i, j);

        if (r1 == r2) {               // Pareja (diagonal)
            mask[i][j][0] = true;     // marcamos ambas capas por simplicidad
            mask[i][j][1] = true;
        } else if (suited) {          // Mano suited → capa 1
            mask[hi][lo][1] = true;
        } else if (offsuit) {         // Mano offsuit → capa 0
            mask[hi][lo][0] = true;
        } else {                      // Sin sufijo: marcamos ambas (por si acaso)
            mask[hi][lo][0] = true;
            mask[hi][lo][1] = true;
        }
    }
}
