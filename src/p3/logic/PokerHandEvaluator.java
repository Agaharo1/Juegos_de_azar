package p3.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Evaluador de manos estilo práctica 1:
 * - Recibe 7 cartas (2 mano + 5 board)
 * - Genera las 21 combinaciones posibles de 5 cartas
 * - Evalúa cada 5-cartas con ranking estándar de Texas Hold'em
 *
 * Categorías (peor → mejor):
 * 0 HIGH_CARD
 * 1 ONE_PAIR
 * 2 TWO_PAIR
 * 3 THREE_OF_A_KIND
 * 4 STRAIGHT
 * 5 FLUSH
 * 6 FULL_HOUSE
 * 7 FOUR_OF_A_KIND
 * 8 STRAIGHT_FLUSH (incluye ROYAL_FLUSH)
 *
 * ROYAL_FLUSH se marca explícitamente, pero se trata como
 * STRAIGHT_FLUSH con high = 14.
 */
public final class PokerHandEvaluator {

    private PokerHandEvaluator() {}

    /* =========================================================
     *                 API PÚBLICA PRINCIPAL
     * ========================================================= */

    /** Evalúa mejor mano a partir de c1, c2 y las 5 cartas del board. */
    public static long evaluate7(String c1, String c2, List<String> board5) {
        if (board5 == null || board5.size() != 5)
            throw new IllegalArgumentException("Se esperan 5 cartas de board.");

        return evaluate7(
                c1, c2,
                board5.get(0), board5.get(1), board5.get(2),
                board5.get(3), board5.get(4)
        );
    }

    /** Evalúa mejor mano a partir de exactamente 7 códigos ("Ah", "Kd", ...). */
    public static long evaluate7(String... codes) {
        if (codes == null || codes.length != 7)
            throw new IllegalArgumentException("Se requieren exactamente 7 cartas.");

        Best best = null;

        // Generación lexicográfica de combinaciones de 5 entre 7
        int[] idx = {0, 1, 2, 3, 4};
        while (true) {
            // Construcción de la combinación actual
            String[] five = new String[5];
            for (int i = 0; i < 5; i++) {
                five[i] = codes[idx[i]];
            }

            // Evaluación 5-cartas
            HandResult r = evaluate5(five);
            long packed = pack(r);

            if (best == null || packed > best.packed) {
                best = new Best(r, packed);
            }

            // Siguiente combinación
            int p = 4;
            while (p >= 0 && idx[p] == (7 - 5 + p)) p--;
            if (p < 0) break;
            idx[p]++;
            for (int j = p + 1; j < 5; j++) idx[j] = idx[j - 1] + 1;
        }

        return best.packed;
    }

    /* =========================================================
     *                       TIPOS INTERNOS
     * ========================================================= */

    // Ranking oficial: 0(low) → 8(high)
    private enum Category {
        HIGH_CARD, ONE_PAIR, TWO_PAIR, THREE_OF_A_KIND,
        STRAIGHT, FLUSH, FULL_HOUSE, FOUR_OF_A_KIND,
        STRAIGHT_FLUSH
    }

    private static final class HandResult {
        final Category category;
        final int[] tie;          // desempate, ordenado por relevancia
        final boolean royalFlush; // solo informativo

        HandResult(Category c, int[] tie, boolean rf) {
            this.category = c;
            this.tie = tie;
            this.royalFlush = rf;
        }
    }

    private static final class Best {
        final HandResult result;
        final long packed;
        Best(HandResult r, long p) { this.result = r; this.packed = p; }
    }

    /* =========================================================
     *                  EVALUACIÓN 5 CARTAS
     * ========================================================= */

    private static HandResult evaluate5(String[] codes) {

        int[] vals = new int[5];
        char[] suits = new char[5];

        for (int i = 0; i < 5; i++) {
            vals[i] = toValue(codes[i].charAt(0));
            suits[i] = Character.toLowerCase(codes[i].charAt(1));
        }

        // Orden ascendente por valor
        sort5(vals, suits);

        boolean flush = sameSuit(suits);
        boolean straight = isStraight(vals);
        boolean straightFlush = flush && straight;
        boolean royal = straightFlush && isRoyal(vals);

        // Conteo de valores
        int[] freq = new int[15];
        for (int v : vals) freq[v]++;

        int four = -1, three = -1;
        List<Integer> pairs = new ArrayList<>();

        for (int v = 14; v >= 2; v--) {
            if (freq[v] == 4) four = v;
            else if (freq[v] == 3) three = v;
            else if (freq[v] == 2) pairs.add(v);
        }

        // Royal Flush
        if (royal)
            return new HandResult(Category.STRAIGHT_FLUSH, new int[]{14}, true);

        // Straight Flush
        if (straightFlush)
            return new HandResult(Category.STRAIGHT_FLUSH,
                    new int[]{highStraight(vals)}, false);

        // Four of a Kind
        if (four != -1) {
            int kicker = highestExcluding(freq, four);
            return new HandResult(Category.FOUR_OF_A_KIND, new int[]{four, kicker}, false);
        }

        // Full House
        if (three != -1 && !pairs.isEmpty()) {
            return new HandResult(Category.FULL_HOUSE,
                    new int[]{three, pairs.get(0)}, false);
        }

        // Flush
        if (flush)
            return new HandResult(Category.FLUSH, topDesc(vals, 5), false);

        // Straight
        if (straight)
            return new HandResult(Category.STRAIGHT,
                    new int[]{highStraight(vals)}, false);

        // Three of a Kind
        if (three != -1) {
            int k1 = highestExcluding(freq, three);
            int k2 = highestExcluding(freq, three, k1);
            return new HandResult(Category.THREE_OF_A_KIND, new int[]{three, k1, k2}, false);
        }

        // Two Pair
        if (pairs.size() >= 2) {
            int p1 = pairs.get(0), p2 = pairs.get(1);
            int kicker = highestExcluding(freq, p1, p2);
            return new HandResult(Category.TWO_PAIR, new int[]{p1, p2, kicker}, false);
        }

        // One Pair
        if (pairs.size() == 1) {
            int p = pairs.get(0);
            int k1 = highestExcluding(freq, p);
            int k2 = highestExcluding(freq, p, k1);
            int k3 = highestExcluding(freq, p, k1, k2);
            return new HandResult(Category.ONE_PAIR, new int[]{p, k1, k2, k3}, false);
        }

        // High Card
        return new HandResult(Category.HIGH_CARD, topDesc(vals, 5), false);
    }

    /* =========================================================
     *                   EMPAQUETADO A long
     * ========================================================= */

    private static long pack(HandResult r) {
        long v = ((long) r.category.ordinal()) << 40;

        final int MAX = 5; // siempre 5 slots
        for (int i = 0; i < MAX; i++) {
            int x = (i < r.tie.length ? r.tie[i] : 0);
            int shift = (MAX - 1 - i) * 5;
            v |= ((long) (x & 0x1F)) << shift;
        }

        return v;
    }

    /* =========================================================
     *                     UTILIDADES INTERNAS
     * ========================================================= */

    /** A,K,Q,J,T,9..2 → 14..2 */
    private static int toValue(char r) {
        switch (Character.toUpperCase(r)) {
            case 'A': return 14;
            case 'K': return 13;
            case 'Q': return 12;
            case 'J': return 11;
            case 'T': return 10;
            default:
                int v = Character.getNumericValue(r);
                if (v < 2 || v > 9)
                    throw new IllegalArgumentException("Valor inválido: " + r);
                return v;
        }
    }

    /** Orden burbuja para 5 elementos (suficiente y pedagógico). */
    private static void sort5(int[] vals, char[] suits) {
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 4; j++)
                if (vals[j] > vals[j + 1]) {
                    int tv = vals[j]; vals[j] = vals[j + 1]; vals[j + 1] = tv;
                    char ts = suits[j]; suits[j] = suits[j + 1]; suits[j + 1] = ts;
                }
    }

    private static boolean sameSuit(char[] suits) {
        char s = suits[0];
        for (char c : suits) if (c != s) return false;
        return true;
    }

    /** Straight con soporte para wheel (A-2-3-4-5). */
    private static boolean isStraight(int[] vals) {
        boolean normal = true;
        for (int i = 1; i < 5; i++)
            if (vals[i] != vals[i - 1] + 1)
                normal = false;

        if (normal) return true;

        // Caso especial wheel
        if (vals[4] == 14 && vals[0] == 2 &&
            vals[1] == 3 && vals[2] == 4 && vals[3] == 5)
            return true;

        return false;
    }

    /** Valor alto de la escalera. A-2-3-4-5 → 5. */
    private static int highStraight(int[] vals) {
        if (vals[4] == 14 && vals[0] == 2 &&
            vals[1] == 3 && vals[2] == 4 && vals[3] == 5)
            return 5;
        return vals[4];
    }

    private static boolean isRoyal(int[] vals) {
        return Arrays.equals(vals, new int[]{10, 11, 12, 13, 14});
    }

    private static int highestExcluding(int[] freq, int... excluded) {
        Set<Integer> ex = new HashSet<>();
        for (int v : excluded) ex.add(v);

        for (int v = 14; v >= 2; v--)
            if (!ex.contains(v) && freq[v] > 0)
                return v;

        return 0;
    }

    /** Devuelve los N valores más altos (descendentes). */
    private static int[] topDesc(int[] asc, int n) {
        int[] out = new int[Math.min(n, 5)];
        int k = 0;
        for (int i = asc.length - 1; i >= 0 && k < out.length; i--)
            out[k++] = asc[i];
        return out;
    }
}
