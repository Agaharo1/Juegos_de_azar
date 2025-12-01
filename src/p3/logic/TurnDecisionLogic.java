package p3.logic;

import java.util.*;
import p3.model.Hand;

/**
 * Lógica para el apartado 2.2 (Turn Decision):
 * - Calcula outs medios del Hero contra un rango del Villano.
 * - Evalúa equity aproximada en el Turn.
 * - Aplica regla EM (equity mínima) para decidir CALL o FOLD.
 */
public final class TurnDecisionLogic {

    private TurnDecisionLogic() {}

    private static final String[] SUITS = {"h","d","c","s"};
    private static final String RANKS = "23456789TJQKA";

    // =====================================================================
    //                         API PRINCIPAL
    // =====================================================================

    /** Resultado empaquetado de la decisión. */
    public static class TurnDecisionResult {
        public final double avgOuts;
        public final double equityPercent;
        public final boolean call;

        public TurnDecisionResult(double avgOuts, double equityPercent, boolean call) {
            this.avgOuts = avgOuts;
            this.equityPercent = equityPercent;
            this.call = call;
        }
    }

    /**
     * Evaluación completa: outs medios + equity aproximada + decisión CALL/FOLD.
     */
    public static TurnDecisionResult evaluateDecision(
            Hand heroHand,
            String villainRangeRaw,
            List<String> board4,
            double emPercent) {

        double avgOuts = calculateAverageOuts(heroHand, villainRangeRaw, board4);
        double equity = (avgOuts / 44.0) * 100.0;     // Turn → 44 rivers posibles
        boolean call = avgOuts > emPercent;

        return new TurnDecisionResult(avgOuts, equity, call);
    }

    /**
     * Media de outs del Hero vs el rango del Villano en el Turn.
     * El empate se reparte 50/50.
     */
    public static double calculateAverageOuts(
            Hand heroHand,
            String villainRangeRaw,
            List<String> board4) {

        if (board4 == null || board4.size() != 4) return 0.0;

        Set<String> dead = new HashSet<>(heroHand.asList());
        dead.addAll(board4);

        // Rango textual → lista de manos 169 → combinación a manos concretas
        List<String> tokens = RangeParser.parse(villainRangeRaw);
        List<Hand> villainHands = expandToSpecificHands(tokens, dead);

        if (villainHands.isEmpty()) return 0.0;

        long total = 0;
        for (Hand v : villainHands)
            total += countOuts(heroHand, v, board4, dead);

        return (double) total / villainHands.size();
    }

    // =====================================================================
    //                         CÁLCULO DE OUTS
    // =====================================================================

    /** Cuenta outs del Hero contra una mano concreta del Villano. */
    private static int countOuts(
            Hand hero,
            Hand villain,
            List<String> board4,
            Set<String> globalDead) {

        // Dead cards específicos (Hero + Board + esa mano de Villano)
        Set<String> dead = new HashSet<>(globalDead);
        dead.addAll(villain.asList());

        // Construir mazo restante
        List<String> deck = buildRemainingDeck(dead);

        int wins = 0;
        int ties = 0;

        // Cada posible river
        for (String river : deck) {
            List<String> board5 = new ArrayList<>(board4);
            board5.add(river);

            long h = PokerHandEvaluator.evaluate7(hero.card1(), hero.card2(), board5);
            long v = PokerHandEvaluator.evaluate7(villain.card1(), villain.card2(), board5);

            if (h > v) wins++;
            else if (h == v) ties++;
        }

        // Empates cuentan medio
        return wins + ties / 2;
    }

    /** Devuelve todas las cartas posibles excluyendo las dead. */
    private static List<String> buildRemainingDeck(Set<String> dead) {
        List<String> deck = new ArrayList<>(44);

        for (int i = 0; i < RANKS.length(); i++) {
            char r = RANKS.charAt(i);
            for (String s : SUITS) {
                String c = "" + r + s;
                if (!dead.contains(c)) deck.add(c);
            }
        }
        return deck;
    }

    // =====================================================================
    //                   EXPANSIÓN DEL RANGO A MANOS CONCRETAS
    // =====================================================================

    /** Convierte manos "AKs", "QQ", "T9o" en combinaciones reales AhKh, KsKd, etc. */
    private static List<Hand> expandToSpecificHands(
            List<String> tokens,
            Set<String> dead) {

        List<Hand> out = new ArrayList<>();

        for (String t : tokens) {
            if (t.length() < 2) continue;

            char r1 = t.charAt(0);
            char r2 = t.charAt(1);
            boolean pair = r1 == r2;
            boolean suited = t.endsWith("s");
            boolean offsuit = t.endsWith("o");

            if (pair) {
                expandPairs(out, r1, dead);
            } else if (suited) {
                expandSuited(out, r1, r2, dead);
            } else if (offsuit) {
                expandOffsuit(out, r1, r2, dead);
            } else {
                expandNoSuffix(out, r1, r2, dead);
            }
        }
        return out;
    }

    /** Parejas → 6 combinaciones. */
    private static void expandPairs(List<Hand> out, char r, Set<String> dead) {
        for (int i = 0; i < 4; i++)
            for (int j = i + 1; j < 4; j++)
                tryAdd(out, "" + r + SUITS[i], "" + r + SUITS[j], dead);
    }

    /** Suited → 4 combinaciones. */
    private static void expandSuited(List<Hand> out, char r1, char r2, Set<String> dead) {
        for (String s : SUITS)
            tryAdd(out, "" + r1 + s, "" + r2 + s, dead);
    }

    /** Offsuit → 12 combinaciones. */
    private static void expandOffsuit(List<Hand> out, char r1, char r2, Set<String> dead) {
        for (String s1 : SUITS)
            for (String s2 : SUITS)
                if (!s1.equals(s2))
                    tryAdd(out, "" + r1 + s1, "" + r2 + s2, dead);
    }

    /** Sin sufijo → 16 combinaciones posibles. */
    private static void expandNoSuffix(List<Hand> out, char r1, char r2, Set<String> dead) {
        for (String s1 : SUITS)
            for (String s2 : SUITS)
                tryAdd(out, "" + r1 + s1, "" + r2 + s2, dead);
    }

    /** Intenta registrar la mano, ignorando colisiones e inválidas. */
    private static void tryAdd(
            List<Hand> out,
            String c1,
            String c2,
            Set<String> dead) {

        if (dead.contains(c1) || dead.contains(c2)) return;
        if (c1.equals(c2)) return;

        try {
            out.add(new Hand(c1, c2));
        } catch (Exception ignored) {}
    }
}
