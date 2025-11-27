package p3.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
            // PARES
            "AA","KK","QQ","JJ","TT","99","88","77","66","55","44","33","22",

            // SUITED FUERTES
            "AKs","AQs","AJs","ATs","A9s","A8s","A7s","A6s","A5s","A4s","A3s","A2s",
            "KQs","KJs","KTs","K9s","K8s","K7s","K6s","K5s","K4s","K3s","K2s",
            "QJs","QTs","Q9s","Q8s","Q7s","Q6s","Q5s","Q4s","Q3s","Q2s",
            "JTs","J9s","J8s","J7s","J6s","J5s","J4s","J3s","J2s",
            "T9s","T8s","T7s","T6s","T5s","T4s","T3s","T2s",
            "98s","97s","96s","95s","94s","93s","92s",
            "87s","86s","85s","84s","83s","82s",
            "76s","75s","74s","73s","72s",
            "65s","64s","63s","62s",
            "54s","53s","52s",
            "43s","42s",
            "32s",

            // OFFSUIT
            "AKo","AQo","AJo","ATo","A9o","A8o","A7o","A6o","A5o","A4o","A3o","A2o",
            "KQo","KJo","KTo","K9o","K8o","K7o","K6o","K5o","K4o","K3o","K2o",
            "QJo","QTo","Q9o","Q8o","Q7o","Q6o","Q5o","Q4o","Q3o","Q2o",
            "JTo","J9o","J8o","J7o","J6o","J5o","J4o","J3o","J2o",
            "T9o","T8o","T7o","T6o","T5o","T4o","T3o","T2o",
            "98o","97o","96o","95o","94o","93o","92o",
            "87o","86o","85o","84o","83o","82o",
            "76o","75o","74o","73o","72o",
            "65o","64o","63o","62o",
            "54o","53o","52o",
            "43o","42o",
            "32o"
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
    	if (p > 100) p = 100;
    	if (p > 1) p /= 100.0;

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
        int limit = Math.min(RANKING_169.length, (int)Math.ceil(RANKING_169.length * percent));
        for (int i = 0; i < limit; i++) {
            mark(mask, RANKING_169[i]);
        }
        return mask;
    }
    
    /**
     * Devuelve true si una mano concreta está dentro del top por porcentaje dado.
     * Usa HandUtils.to169() para convertir la mano real a su forma textual.
     */
    public static boolean isInTopPercent(p3.model.Hand hand, double percent) {
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
