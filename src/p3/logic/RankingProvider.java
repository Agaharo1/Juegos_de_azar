package p3.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * RankingProvider gestiona el ranking estándar de 169 manos iniciales (Sklansky-like)
 * y proporciona utilidades para:
 *
 *  1) Obtener la lista completa ordenada de las 169 manos.
 *  2) Obtener el top X% de manos.
 *  3) Construir una máscara [13][13][2] para visualización o lookup rápido.
 *  4) Consultar si una mano concreta pertenece al top X%.
 *
 * Notación:
 *  - RANKS = "23456789TJQKA" → índices 0..12
 *  - Para suited: capa 1  (mask[hi][lo][1])
 *  - Para offsuit: capa 0 (mask[hi][lo][0])
 *  - Pares se marcan en ambas capas para simplificar.
 */
public final class RankingProvider {

    private RankingProvider() {}

    /** Orden de cartas: 2..A **/
    private static final String RANKS = "23456789TJQKA";

    /** Ranking completo de manos iniciales (169), de mejor a peor. */
    private static final String[] RANKING_169 = {
            // Pares
            "AA","KK","QQ","JJ","TT","99","88","77","66","55","44","33","22",

            // Suited fuertes
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

            // Offsuited
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

    /* ---------------------------------------------------------
     *                   API PÚBLICA
     * --------------------------------------------------------- */

    /** Devuelve una copia del ranking completo (169 manos). */
    public static List<String> getRanking() {
        return new ArrayList<>(Arrays.asList(RANKING_169));
    }

    /**
     * Devuelve el top por porcentaje.
     * Acepta "p" como:
     *  - 0..1  → porcentaje directo
     *  - 0..100 → se interpreta como %
     *  - >100 → se clampa a 100
     */
    public static List<String> getTopByPercent(double p) {

        // Normalización robusta
        if (p < 0) return Collections.emptyList();
        if (p > 100) p = 100;
        if (p > 1)   p /= 100.0;

        int k = (int) Math.ceil(RANKING_169.length * p);
        k = Math.max(0, Math.min(k, RANKING_169.length));

        if (k == 0) return Collections.emptyList();

        List<String> out = new ArrayList<>(k);
        for (int i = 0; i < k; i++) out.add(RANKING_169[i]);
        return out;
    }

    /**
     * Devuelve una máscara [13][13][2] con el top marcado.
     * mask[hi][lo][1] = suited
     * mask[hi][lo][0] = offsuit
     * Parejas → ambas capas para simplificar lookup.
     */
    public static boolean[][][] getMaskForPercent(double percent) {
        boolean[][][] mask = new boolean[13][13][2];

        // Normalizar igual que getTopByPercent
        if (percent > 100) percent = 100;
        if (percent > 1)   percent /= 100.0;

        int limit = (int) Math.ceil(RANKING_169.length * percent);
        limit = Math.max(0, Math.min(limit, RANKING_169.length));

        for (int i = 0; i < limit; i++) {
            markHand(mask, RANKING_169[i]);
        }

        return mask;
    }

    /** Consulta si una mano pertenece al top X% usando HandUtils.to169(). */
    public static boolean isInTopPercent(p3.model.Hand hand, double percent) {
        String normalized = HandUtils.to169(hand);
        return getTopByPercent(percent).contains(normalized);
    }

    /* ---------------------------------------------------------
     *                MÉTODOS PRIVADOS DE APOYO
     * --------------------------------------------------------- */

    /** Marca en la máscara la mano (AKs, AKo, TT…). */
    private static void markHand(boolean[][][] mask, String hand) {

        char r1 = hand.charAt(0);
        char r2 = hand.charAt(1);

        int i1 = RANKS.indexOf(r1);
        int i2 = RANKS.indexOf(r2);
        if (i1 < 0 || i2 < 0) return; // seguridad

        int hi = Math.max(i1, i2);
        int lo = Math.min(i1, i2);

        boolean suited  = hand.endsWith("s");
        boolean offsuit = hand.endsWith("o");
        boolean pair    = (r1 == r2);

        if (pair) {
            // Parejas: marcar ambas capas en [i][i]
            mask[i1][i2][0] = true;
            mask[i1][i2][1] = true;
            return;
        }

        if (suited) {
            mask[hi][lo][1] = true; // capa suited
        } else if (offsuit) {
            mask[hi][lo][0] = true; // capa offsuit
        } else {
            // Sin especificar → marcamos ambas capas por compatibilidad
            mask[hi][lo][0] = true;
            mask[hi][lo][1] = true;
        }
    }
}
