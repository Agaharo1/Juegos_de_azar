package tp2.logic;

import java.util.*;

/** Evaluador de manos de 7 cartas (Texas Hold'em).
 *  Devuelve un entero/long mayor cuanto mejor es la mano.
 *  Categorías (de mayor a menor):
 *   8 Straight Flush, 7 Four, 6 Full House, 5 Flush, 4 Straight,
 *   3 Trips, 2 Two Pair, 1 Pair, 0 High Card
 */
public final class PokerHandEvaluator {

    private static final String RANKS = "23456789TJQKA";

    private static int rIndex(char r) { return RANKS.indexOf(r); }  // 0..12

    /** code tipo "Ah", "Td"... -> [rank(0..12), suit(0..3)] */
    private static int[] parse(String code) {
        int r = rIndex(code.charAt(0));
        int s = "hdcs".indexOf(Character.toLowerCase(code.charAt(1)));
        if (r < 0 || s < 0) throw new IllegalArgumentException("Carta inválida: " + code);
        return new int[]{r, s};
    }

    private static long packScore(int category, int... ranksHiToLo) {
        // Empaqueta categoría y 5 kickers en un long para comparar fácilmente
        long v = ((long) category) << 40;
        long shift = 0;
        for (int i = 0; i < 5; i++) {
            int r = (i < ranksHiToLo.length) ? ranksHiToLo[i] : 0;
            v |= ((long) r & 0x1F) << shift; // 5 bits por carta (0..31)
            shift += 5;
        }
        return v;
    }

    /** Evalúa mejor 5-cartas a partir de 7 códigos (2 mano + 5 board). */
    public static long evaluate7(String c1, String c2, List<String> board5) {
        if (board5.size() != 5) throw new IllegalArgumentException("Se esperan 5 cartas de board.");
        String[] all = new String[]{c1, c2, board5.get(0), board5.get(1), board5.get(2), board5.get(3), board5.get(4)};
        return evaluate7(all);
    }

    public static long evaluate7(String... codes) {
        if (codes.length != 7) throw new IllegalArgumentException("Se requieren 7 cartas.");
        int[] rankCnt = new int[13];
        int[] suitCnt = new int[4];
        int[][] suitRanks = new int[4][7]; // ranks por palo
        int[] suitLen = new int[4];

        int ranksMask = 0;
        int[][] cards = new int[7][2];
        for (int i = 0; i < 7; i++) {
            cards[i] = parse(codes[i]);
            int r = cards[i][0], s = cards[i][1];
            rankCnt[r]++;
            suitCnt[s]++;
            suitRanks[s][suitLen[s]++] = r;
            ranksMask |= (1 << r);
        }

        // ---- Straight helpers
        int bestStraightHigh = straightHighFromMask(ranksMask);
        // Straight Flush
        int sfHigh = -1;
        int flushSuit = -1;
        for (int s = 0; s < 4; s++) {
            if (suitCnt[s] >= 5) { flushSuit = s; break; }
        }
        if (flushSuit != -1) {
            int m = 0;
            for (int i = 0; i < suitLen[flushSuit]; i++) m |= (1 << suitRanks[flushSuit][i]);
            sfHigh = straightHighFromMask(m);
            if (sfHigh >= 0) {
                // Rellenamos 5 cartas del straight flush (descendente desde sfHigh)
                int[] five = straightRanks(sfHigh);
                return packScore(8, five);
            }
        }

        // ---- Four / Trips / Pairs
        int four = -1, trips1 = -1, trips2 = -1;
        List<Integer> pairs = new ArrayList<>();
        for (int r = 12; r >= 0; r--) {
            if (rankCnt[r] == 4) four = r;
            else if (rankCnt[r] == 3) {
                if (trips1 == -1) trips1 = r; else trips2 = r;
            } else if (rankCnt[r] == 2) pairs.add(r);
        }

        if (four != -1) {
            int kicker = highestExcluding(rankCnt, four);
            return packScore(7, four, kicker, 0, 0, 0);
        }

        if (trips1 != -1 && (trips2 != -1 || !pairs.isEmpty())) {
            int pair = (trips2 != -1) ? trips2 : pairs.get(0);
            return packScore(6, trips1, pair, 0, 0, 0);
        }

        if (flushSuit != -1) {
            int[] fr = suitRanks[flushSuit].clone();
            Arrays.sort(fr, 0, suitLen[flushSuit]);
            int[] top5 = new int[5];
            int k = 0;
            for (int i = suitLen[flushSuit]-1; i >= 0 && k < 5; i--) top5[k++] = fr[i];
            return packScore(5, top5);
        }

        if (bestStraightHigh >= 0) {
            int[] five = straightRanks(bestStraightHigh);
            return packScore(4, five);
        }

        if (trips1 != -1) {
            int k1 = highestExcluding(rankCnt, trips1);
            int k2 = highestExcluding(rankCnt, trips1, k1);
            return packScore(3, trips1, k1, k2, 0, 0);
        }

        if (pairs.size() >= 2) {
            int p1 = pairs.get(0), p2 = pairs.get(1); // ya vienen ordenadas descendentemente
            int kicker = highestExcluding(rankCnt, p1, p2);
            return packScore(2, p1, p2, kicker, 0, 0);
        }

        if (pairs.size() == 1) {
            int p = pairs.get(0);
            int k1 = highestExcluding(rankCnt, p);
            int k2 = highestExcluding(rankCnt, p, k1);
            int k3 = highestExcluding(rankCnt, p, k1, k2);
            return packScore(1, p, k1, k2, k3, 0);
        }

        // High card
        int[] highs = topNFromMask(ranksMask, 5);
        return packScore(0, highs);
    }

    // ===== Helpers de ranking =====
    private static int highestExcluding(int[] rankCnt, int... ex) {
        Set<Integer> exc = new HashSet<>();
        for (int x : ex) exc.add(x);
        for (int r = 12; r >= 0; r--) if (!exc.contains(r) && rankCnt[r] > 0) return r;
        return 0;
    }

    private static int straightHighFromMask(int mask) {
        // Trata wheel (A-5)
        int wheelMask = (1<<12)|(1<<3)|(1<<2)|(1<<1)|(1<<0);
        if ( (mask & wheelMask) == wheelMask ) return 3; // 5 alta
        int run = 0;
        for (int r = 0; r < 13; r++) {
            if ((mask & (1<<r)) != 0) run++; else run = 0;
            if (run >= 5) return r; // r es la alta
        }
        return -1;
    }

    private static int[] straightRanks(int high) {
        // Devuelve [high, high-1, ..., high-4] con wheel especial
        if (high == 3) return new int[]{3,2,1,0,12}; // 5,4,3,2,A
        return new int[]{high, high-1, high-2, high-3, high-4};
    }

    private static int[] topNFromMask(int mask, int n) {
        int[] out = new int[n];
        int k = 0;
        for (int r = 12; r >= 0 && k < n; r--) if ((mask & (1<<r)) != 0) out[k++] = r;
        for (; k < n; k++) out[k] = 0;
        return out;
    }
}
