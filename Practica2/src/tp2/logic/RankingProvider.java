package tp2.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Proporciona un ranking de 169 manos (aprox. Sklansky) y utilidades para:
 *  - devolver el ranking completo (compatibilidad con GUI actual)
 *  - devolver el top-K por porcentaje
 *  - construir una máscara [13][13][2] (offsuit/suited) por porcentaje
 *
 * Índices 0..12 en máscara corresponden a ranks 2..A (RANKS="23456789TJQKA").
 */
public final class RankingProvider {

    private RankingProvider() {}

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

    private static final String RANKS = "23456789TJQKA";

    /** Compatibilidad con tu GUI: devuelve una copia del ranking completo (169 ítems). */
    public static List<String> getRanking() {
        return new ArrayList<>(Arrays.asList(RANKING_169));
    }

    /** Devuelve el top por porcentaje (p en 0..1); ej: p=0.25 ⇒ ~42 manos. */
    public static List<String> getTopByPercent(double p) {
        int k = Math.min(RANKING_169.length, Math.max(0, (int)Math.ceil(RANKING_169.length * p)));
        if (k == 0) return Collections.emptyList();
        ArrayList<String> top = new ArrayList<>(k);
        for (int i = 0; i < k; i++) top.add(RANKING_169[i]);
        return top;
    }

    /** Genera una máscara [13][13][2] con el top por porcentaje. */
    public static boolean[][][] getMaskForPercent(double percent) {
        boolean[][][] mask = new boolean[13][13][2];
        int limit = Math.min(RANKING_169.length, (int)Math.round(RANKING_169.length * percent));
        for (int i = 0; i < limit; i++) {
            mark(mask, RANKING_169[i]);
        }
        return mask;
    }

    // ==== helpers ====

    private static void mark(boolean[][][] mask, String hand) {
        char r1 = hand.charAt(0);
        char r2 = hand.charAt(1);
        int i = RANKS.indexOf(r1);
        int j = RANKS.indexOf(r2);
        if (i < 0 || j < 0) return;

        boolean suited  = hand.endsWith("s");
        boolean offsuit = hand.endsWith("o");

        int hi = Math.max(i, j);
        int lo = Math.min(i, j);

        if (r1 == r2) {               // pareja
            mask[i][j][0] = true;
            mask[i][j][1] = true;
        } else if (suited) {          // suited
            mask[hi][lo][1] = true;
        } else if (offsuit) {         // offsuit
            mask[hi][lo][0] = true;
        } else {                      // sin sufijo: ambas
            mask[hi][lo][0] = true;
            mask[hi][lo][1] = true;
        }
    }
}
