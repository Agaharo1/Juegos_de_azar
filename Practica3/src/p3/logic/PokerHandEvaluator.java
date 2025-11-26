package p3.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Evaluador claro y didáctico (como en la práctica anterior):
 * - De 7 cartas (2+5) genera todas las combinaciones de 5.
 * - Evalúa cada 5-cartas con reglas de poker (incluye ROYAL_FLUSH explícita).
 * - El mejor resultado se empaqueta en un long comparable (categoría + kickers).
 *
 * Categorías (de peor a mejor):
 *  0 HIGH_CARD
 *  1 ONE_PAIR
 *  2 TWO_PAIR
 *  3 THREE_OF_A_KIND
 *  4 STRAIGHT
 *  5 FLUSH
 *  6 FULL_HOUSE
 *  7 FOUR_OF_A_KIND
 *  8 STRAIGHT_FLUSH       (incluye ROYAL_FLUSH como caso especial alto)
 *
 * Nota: Tratamos ROYAL_FLUSH como STRAIGHT_FLUSH con high=14 (A),
 * y además lo detectamos explícitamente para claridad pedagógica.
 */
public final class PokerHandEvaluator {

    private PokerHandEvaluator() {}

    // ===== API pública esperada por el resto del proyecto =====

    /** Evalúa mejor 5-cartas a partir de 2 de mano + board (5). */
    public static long evaluate7(String c1, String c2, List<String> board5) {
        if (board5 == null || board5.size() != 5)
            throw new IllegalArgumentException("Se esperan 5 cartas de board.");
        String[] all = new String[]{ c1, c2, board5.get(0), board5.get(1), board5.get(2), board5.get(3), board5.get(4) };
        return evaluate7(all);
    }

    /** Evalúa mejor 5-cartas a partir de 7 códigos ("Ah","Kd",...). */
    public static long evaluate7(String... codes) {
        if (codes == null || codes.length != 7)
            throw new IllegalArgumentException("Se requieren exactamente 7 cartas.");

        // Generar todas las 21 combinaciones de 5
        Best best = null;
        int n = 7;
        int[] idx = {0,1,2,3,4}; // combinación inicial
        while (true) {
            // Construir la mano de 5 con los índices actuales
            String[] five = new String[5];
            for (int i = 0; i < 5; i++) five[i] = codes[idx[i]];

            // Evaluar esta 5-cartas
            HandResult r = evaluate5(five);

            // Empaquetar a long comparable
            long packed = pack(r);

            // Actualizar mejor si corresponde
            if (best == null || packed > best.packed) {
                best = new Best(r, packed);
            }

            // Siguiente combinación (lexicográfica)
            int p = 4;
            while (p >= 0 && idx[p] == n - 5 + p) p--;
            if (p < 0) break;
            idx[p]++;
            for (int j = p + 1; j < 5; j++) idx[j] = idx[j - 1] + 1;
        }

        return (best != null) ? best.packed : 0L;
    }

    // ===== Representación clara del resultado de 5 cartas =====

    // Orden creciente (HIGH_CARD = 0 ... STRAIGHT_FLUSH = 8)
    private enum Category {
        HIGH_CARD, ONE_PAIR, TWO_PAIR, THREE_OF_A_KIND,
        STRAIGHT, FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH
    }

    private static final class HandResult {
        final Category category;
        final int[]    tie;         // Desempates (valores numéricos altos primero)
        final boolean  royalFlush;  // para info/claridad (no afecta al empaquetado)

        HandResult(Category c, int[] tie, boolean royal) {
            this.category = c;
            this.tie = tie;
            this.royalFlush = royal;
        }
    }

    private static final class Best {
        final HandResult result;
        final long packed;
        Best(HandResult r, long p) { this.result = r; this.packed = p; }
    }

    // ===== Evaluación de 5 cartas (estilo práctica 1) =====

    private static HandResult evaluate5(String[] codes5) {
        // Parse a listas de valores (2..14) y palos ('h','d','c','s')
        int[] vals = new int[5];
        char[] suits = new char[5];
        for (int i = 0; i < 5; i++) {
            vals[i]  = toValue(codes5[i].charAt(0));        // A,K,Q,J,T,9..2 → 14..2
            suits[i] = Character.toLowerCase(codes5[i].charAt(1));
        }

        // Ordena por valor ascendente para facilitar checks
        sortByValueAsc(vals, suits);

        boolean flush = esColor(suits);
        boolean straight = esEscalera(vals);        // soporta A-2-3-4-5
        boolean straightFlush = false;
        boolean royal = false;

        if (flush) {
            // Para straight-flush se comprueba escalera sobre los valores dentro del mismo palo.
            // Como tenemos solo 5 cartas, "flush && straight" implica straight-flush.
            straightFlush = straight;
            royal = straightFlush && esEscaleraReal(vals) && mismoPalo(suits);
        }

        // Conteos por valor para detectar parejas/tríos/póker, etc.
        // freq[v] con v en 2..14
        int[] freq = new int[15];
        for (int v : vals) freq[v]++;

        // Detecciones por conteo
        int four = -1, three = -1;
        List<Integer> pairs = new ArrayList<>();

        for (int v = 14; v >= 2; v--) {
            if (freq[v] == 4) four = v;
            else if (freq[v] == 3) three = v;
            else if (freq[v] == 2) pairs.add(v);
        }

        // 1) Royal Flush
        if (royal) {
            // lo tratamos como STRAIGHT_FLUSH con high = 14
            return new HandResult(Category.STRAIGHT_FLUSH, new int[]{14}, true);
        }

        // 2) Straight Flush
        if (straightFlush) {
            int high = highOfStraight(vals);
            return new HandResult(Category.STRAIGHT_FLUSH, new int[]{high}, false);
        }

        // 3) Four of a Kind
        if (four != -1) {
            int kicker = highestExcluding(freq, four);
            return new HandResult(Category.FOUR_OF_A_KIND, new int[]{four, kicker}, false);
        }

        // 4) Full House
        if (three != -1 && !pairs.isEmpty()) {
            int pair = pairs.get(0); // ya viene en orden descendente
            return new HandResult(Category.FULL_HOUSE, new int[]{three, pair}, false);
        }

        // 5) Flush
        if (flush) {
            int[] top5 = topDesc(vals, 5);
            return new HandResult(Category.FLUSH, top5, false);
        }

        // 6) Straight
        if (straight) {
            int high = highOfStraight(vals);
            return new HandResult(Category.STRAIGHT, new int[]{high}, false);
        }

        // 7) Three of a Kind
        if (three != -1) {
            int k1 = highestExcluding(freq, three);
            int k2 = highestExcluding(freq, three, k1);
            return new HandResult(Category.THREE_OF_A_KIND, new int[]{three, k1, k2}, false);
        }

        // 8) Two Pair
        if (pairs.size() >= 2) {
            int p1 = pairs.get(0), p2 = pairs.get(1);
            int kicker = highestExcluding(freq, p1, p2);
            return new HandResult(Category.TWO_PAIR, new int[]{p1, p2, kicker}, false);
        }

        // 9) One Pair
        if (pairs.size() == 1) {
            int p = pairs.get(0);
            int k1 = highestExcluding(freq, p);
            int k2 = highestExcluding(freq, p, k1);
            int k3 = highestExcluding(freq, p, k1, k2);
            return new HandResult(Category.ONE_PAIR, new int[]{p, k1, k2, k3}, false);
        }

        // 10) High Card
        int[] highs = topDesc(vals, 5);
        return new HandResult(Category.HIGH_CARD, highs, false);
    }

    // ===== Empaquetado a long comparable (categoría + kickers) =====

    private static long pack(HandResult r) {
        long v = ((long) r.category.ordinal()) << 40;
        final int MAX = 5; // hasta 5 componentes de desempate
        for (int i = 0; i < MAX; i++) {
            int x = (i < r.tie.length) ? r.tie[i] : 0;
            int shift = (MAX - 1 - i) * 5;   // <<— ¡clave! el más alto, más significativo
            v |= ((long) (x & 0x1F)) << shift;
        }
        return v;
    }


    // ===== Utilidades de evaluación claras =====

    /** ‘A’,’K’,’Q’,’J’,’T’,’9’..’2’ → 14..2 */
    private static int toValue(char r) {
        switch (Character.toUpperCase(r)) {
            case 'A': return 14;
            case 'K': return 13;
            case 'Q': return 12;
            case 'J': return 11;
            case 'T': return 10;
            default:
                int v = Character.getNumericValue(r);
                if (v < 2 || v > 9) throw new IllegalArgumentException("Valor de carta inválido: " + r);
                return v;
        }
    }

    private static void sortByValueAsc(int[] vals, char[] suits) {
        // burbujeo simple por claridad (5 elementos)
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 4; j++) {
                if (vals[j] > vals[j+1]) {
                    int tv = vals[j]; vals[j] = vals[j+1]; vals[j+1] = tv;
                    char ts = suits[j]; suits[j] = suits[j+1]; suits[j+1] = ts;
                }
            }
        }
    }

    private static boolean mismoPalo(char[] suits) {
        char p = suits[0];
        for (int i = 1; i < suits.length; i++) if (suits[i] != p) return false;
        return true;
    }

    private static boolean esColor(char[] suits) {
        return mismoPalo(suits);
    }

    /** ¿Son 5 consecutivas? Soporta A-bajo (A=14 considerado como 1). */
    private static boolean esEscalera(int[] valsAsc) {
        // Intento normal A-alto
        boolean ok = true;
        for (int i = 1; i < 5; i++) {
            if (valsAsc[i] != valsAsc[i-1] + 1) { ok = false; break; }
        }
        if (ok) return true;

        // Caso especial A-2-3-4-5: mapea A(14)→1 y re-prueba
        boolean tieneAs = false;
        for (int v : valsAsc) if (v == 14) { tieneAs = true; break; }
        if (!tieneAs) return false;

        int[] alt = valsAsc.clone();
        for (int i = 0; i < 5; i++) if (alt[i] == 14) alt[i] = 1;
        Arrays.sort(alt);
        for (int i = 1; i < 5; i++) {
            if (alt[i] != alt[i-1] + 1) return false;
        }
        return true;
    }

    /** Alto de la escalera (5..A). Para A-2-3-4-5 devuelve 5. */
    private static int highOfStraight(int[] valsAsc) {
        if (valsAsc[4] == 14 && valsAsc[0] == 2
                && valsAsc[1] == 3 && valsAsc[2] == 4 && valsAsc[3] == 5) {
            return 5; // wheel
        }
        return valsAsc[4];
    }

    /** Escalera real = 10,J,Q,K,A (da igual el orden siempre que sean consecutivas) */
    private static boolean esEscaleraReal(int[] valsAsc) {
        int[] target = {10,11,12,13,14};
        for (int i = 0; i < 5; i++) if (valsAsc[i] != target[i]) return false;
        return true;
    }

    /** Máximo valor disponible que NO esté en ‘ex’ y con freq>0 (de mayor a menor). */
    private static int highestExcluding(int[] freq, int... ex) {
        Set<Integer> e = new HashSet<>();
        for (int x : ex) e.add(x);
        for (int v = 14; v >= 2; v--) if (!e.contains(v) && freq[v] > 0) return v;
        return 0;
    }

    /** Devuelve los ‘n’ valores más altos en orden descendente (sin duplicar). */
    private static int[] topDesc(int[] valsAsc, int n) {
        // Copiamos a descendente
        int[] out = new int[Math.min(n, 5)];
        int k = 0;
        for (int i = valsAsc.length - 1; i >= 0 && k < out.length; i--) {
            out[k++] = valsAsc[i];
        }
        return out;
    }
}
