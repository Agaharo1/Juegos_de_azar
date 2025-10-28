package tp2.logic;

import tp2.model.Hand;

import java.util.*;

/**
 * Calcula probabilidades de ganar (equity) por Monte Carlo:
 * completa manos/board al azar, evalúa y promedia.
 */
public class RealEquityCalculator implements EquityCalculator {

    @Override
    public Map<String, Double> calcularEquity(
            List<String> names, List<Hand> hands, List<String> board, int trials, long seed) {

        if (names.size() != hands.size())
            throw new IllegalArgumentException("names y hands deben tener misma longitud");

        final int N = names.size();

        // 1) Baraja restante (quitamos cartas usadas)
        List<String> used = new ArrayList<>();
        for (Hand h : hands) if (h != null) used.addAll(h.asList());
        if (board != null) used.addAll(board);
        List<String> deck0 = buildDeckExcluding(used);

        double[] wins = new double[N];
        Random rnd = new Random(seed);

        int needBoard = 5 - (board == null ? 0 : board.size());
        if (needBoard < 0) needBoard = 0;

        boolean deterministic = (needBoard == 0) && hands.stream().allMatch(Objects::nonNull);
        int T = deterministic ? 1 : Math.max(1, trials);

        // 2) Simulaciones
        for (int t = 0; t < T; t++) {
            ArrayList<String> deck = new ArrayList<>(deck0);

            String[][] simHands = new String[N][2];
            for (int i = 0; i < N; i++) {
                if (hands.get(i) != null) {
                    simHands[i][0] = hands.get(i).card1();
                    simHands[i][1] = hands.get(i).card2();
                } else {
                    simHands[i][0] = draw(deck, rnd);
                    simHands[i][1] = draw(deck, rnd);
                }
            }

            List<String> board5 = new ArrayList<>(board == null ? List.of() : board);
            for (int k = 0; k < needBoard; k++) board5.add(draw(deck, rnd));

            long best = Long.MIN_VALUE;
            List<Integer> winners = new ArrayList<>(N);

            for (int i = 0; i < N; i++) {
                long sc = PokerHandEvaluator.evaluate7(simHands[i][0], simHands[i][1], board5);
                if (sc > best) {
                    best = sc;
                    winners.clear();
                    winners.add(i);
                } else if (sc == best) {
                    winners.add(i);
                }
            }

            double share = 1.0 / winners.size();
            for (int w : winners) wins[w] += share;
        }

        Map<String, Double> out = new LinkedHashMap<>();
        for (int i = 0; i < N; i++) out.put(names.get(i), 100.0 * wins[i] / T);
        return out;
    }

    // ======= helpers =======

    private static List<String> buildDeckExcluding(Collection<String> used) {
        String[] ranks = {"A","K","Q","J","T","9","8","7","6","5","4","3","2"};
        String[] suits = {"h","d","c","s"};
        Set<String> U = new HashSet<>(used == null ? List.of() : used);
        ArrayList<String> deck = new ArrayList<>(52);
        for (String r : ranks) for (String s : suits) {
            String c = r + s;
            if (!U.contains(c)) deck.add(c);
        }
        return deck;
    }

    private static String draw(ArrayList<String> deck, Random rnd) {
        int i = rnd.nextInt(deck.size());
        return deck.remove(i);
    }
}
