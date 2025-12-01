package p3.logic;

import java.util.*;
import p3.model.Hand;

/**
 * Calcula probabilidades de ganar (equity) mediante Monte Carlo:
 * completa board y manos desconocidas al azar, evalúa con PokerHandEvaluator
 * y promedia resultados.
 *
 * Mantiene EXACTAMENTE la misma lógica que la versión original,
 * pero con código más claro, más seguro y más eficiente.
 */
public class RealEquityCalculator implements EquityCalculator {

    @Override
    public Map<String, Double> calcularEquity(
            List<String> names,
            List<Hand> hands,
            List<String> board,
            int trials,
            long seed) {

        if (names.size() != hands.size())
            throw new IllegalArgumentException("names y hands deben tener misma longitud");

        final int N = names.size();
        final List<String> safeBoard = (board == null) ? List.of() : board;
        final int knownBoard = safeBoard.size();
        final int missingBoard = Math.max(0, 5 - knownBoard);

        // → Si todo está completo, una sola simulación basta (resultado determinista).
        boolean deterministic = (missingBoard == 0) && hands.stream().allMatch(Objects::nonNull);
        final int T = deterministic ? 1 : Math.max(1, trials);

        // Baraja inicial sin cartas usadas
        List<String> deck0 = buildDeckExcluding(collectUsed(hands, safeBoard));

        double[] wins = new double[N];
        Random rnd = new Random(seed);

        /* =========================
         *   SIMULACIONES MONTECARLO
         * ========================= */
        for (int t = 0; t < T; t++) {

            List<String> deck = new ArrayList<>(deck0);  // copia limpia
            String[][] simHands = resolveHands(hands, deck, rnd); // llenar unknowns
            List<String> board5 = buildBoard5(safeBoard, missingBoard, deck, rnd);

            long bestScore = Long.MIN_VALUE;
            List<Integer> winners = new ArrayList<>(N);

            // Evaluación de cada jugador
            for (int i = 0; i < N; i++) {
                long score = PokerHandEvaluator.evaluate7(
                        simHands[i][0], simHands[i][1], board5);

                if (score > bestScore) {
                    bestScore = score;
                    winners.clear();
                    winners.add(i);
                } else if (score == bestScore) {
                    winners.add(i);
                }
            }

            // Empate → repartir la victoria
            double share = 1.0 / winners.size();
            for (int w : winners) wins[w] += share;
        }

        /* =========================
         *   NORMALIZAR % Y SALIDA
         * ========================= */
        Map<String, Double> out = new LinkedHashMap<>();
        for (int i = 0; i < N; i++) {
            out.put(names.get(i), 100.0 * wins[i] / T);
        }
        return out;
    }

    /* =====================================================
     *                  FUNCIONES AUXILIARES
     * ===================================================== */

    /** Reúne cartas usadas por manos fijas + board. */
    private static List<String> collectUsed(List<Hand> hands, List<String> board) {
        List<String> used = new ArrayList<>(board);
        for (Hand h : hands) if (h != null) used.addAll(h.asList());
        return used;
    }

    /** Construye baraja completa menos cartas usadas. */
    private static List<String> buildDeckExcluding(Collection<String> used) {
        final String[] ranks = {"A","K","Q","J","T","9","8","7","6","5","4","3","2"};
        final String[] suits = {"h","d","c","s"};

        Set<String> banned = new HashSet<>(used == null ? List.of() : used);
        List<String> deck = new ArrayList<>(52);

        for (String r : ranks)
            for (String s : suits) {
                String c = r + s;
                if (!banned.contains(c)) deck.add(c);
            }
        return deck;
    }

    /** Devuelve una mano simulada: fija si existe, aleatoria si es null. */
    private static String[][] resolveHands(List<Hand> hands, List<String> deck, Random rnd) {
        int N = hands.size();
        String[][] sim = new String[N][2];

        for (int i = 0; i < N; i++) {
            Hand h = hands.get(i);

            if (h != null) {
                sim[i][0] = h.card1();
                sim[i][1] = h.card2();
            } else {
                sim[i][0] = draw(deck, rnd);
                sim[i][1] = draw(deck, rnd);
            }
        }
        return sim;
    }

    /** Construye el board de 5 cartas añadiendo las que falten desde la baraja. */
    private static List<String> buildBoard5(List<String> board, int missing, List<String> deck, Random rnd) {
        List<String> b = new ArrayList<>(board);
        for (int i = 0; i < missing; i++) b.add(draw(deck, rnd));
        return b;
    }

    /** Roba una carta aleatoria y la elimina del deck. */
    private static String draw(List<String> deck, Random rnd) {
        int idx = rnd.nextInt(deck.size());
        return deck.remove(idx);
    }
}
