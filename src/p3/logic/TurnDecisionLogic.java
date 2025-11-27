package p3.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import p3.model.Hand;

public class TurnDecisionLogic {

    private static final String[] SUITS = {"h", "d", "c", "s"};

    /**
     * Calcula la media de outs del Hero contra el rango del Villano en el Turn.
     * @param heroHand Mano del Hero (ej: "AhKh")
     * @param villainRangeRaw Texto del rango (ej: "QQ+, AKs")
     * @param board Cartas del board (deben ser 4)
     * @return La media de outs.
     */
    public static double calculateAverageOuts(Hand heroHand, String villainRangeRaw, List<String> board) {
        // 1. Validaciones básicas
        if (board.size() != 4) return 0.0; // Solo funciona en Turn
        
        // Cartas ya conocidas (Hero + Board)
        Set<String> deadCards = new HashSet<>(heroHand.asList());
        deadCards.addAll(board);

        // 2. Expandir rango del villano a manos CONCRETAS (AhAd, etc.)
        List<String> genericRange = RangeParser.parse(villainRangeRaw);
        List<Hand> villainPossibleHands = expandRangeToSpecificHands(genericRange, deadCards);

        if (villainPossibleHands.isEmpty()) return 0.0;

        // 3. Calcular outs contra cada mano posible del villano
        long totalOuts = 0;

        for (Hand villainHand : villainPossibleHands) {
            totalOuts += countOuts(heroHand, villainHand, board, deadCards);
        }

        // 4. Calcular media aritmética [cite: 288]
        return (double) totalOuts / villainPossibleHands.size();
    }

    private static int countOuts(Hand hero, Hand villain, List<String> board, Set<String> knownDead) {
        // Cartas muertas para esta simulación específica (Hero + Board + Villano actual)
        Set<String> currentDead = new HashSet<>(knownDead);
        currentDead.addAll(villain.asList());

        // Generar mazo restante (52 - 2 - 4 - 2 = 44 cartas) [cite: 301]
        List<String> deck = new ArrayList<>();
        String[] ranks = "23456789TJQKA".split("");
        for (String r : ranks) {
            for (String s : SUITS) {
                String card = r + s;
                if (!currentDead.contains(card)) {
                    deck.add(card);
                }
            }
        }

        int outs = 0;
        int ties = 0;

        // Probar cada carta del River
        for (String river : deck) {
            List<String> finalBoard = new ArrayList<>(board);
            finalBoard.add(river);

            long scoreHero = PokerHandEvaluator.evaluate7(hero.card1(), hero.card2(), finalBoard);
            long scoreVillain = PokerHandEvaluator.evaluate7(villain.card1(), villain.card2(), finalBoard);

            if (scoreHero > scoreVillain) {
                outs++; // Ganamos
            } else if (scoreHero == scoreVillain) {
                ties++; // Empate [cite: 308]
            }
        }
        
        // Empate cuenta como media out (o se reparte, simplificamos sumando ties y gestionando 0.5 fuera si se quiere)
        // Según enunciado: "el empate se reparte de forma equitativa"
        // Podemos devolver outs * 2 + ties para trabajar con enteros, o double.
        // Trabajaremos con enteros redondeando ties:
        return outs + (ties / 2); 
    }
    
    public static class TurnDecisionResult {
        public final double avgOuts;       // media de outs
        public final double equityPercent; // equity aproximada en %

        public final boolean call;         // true → CALL, false → FOLD

        public TurnDecisionResult(double avgOuts, double equityPercent, boolean call) {
            this.avgOuts = avgOuts;
            this.equityPercent = equityPercent;
            this.call = call;
        }
    }
    /**
     * Calcula outs medios, equity aproximada y decisión CALL/FOLD
     * según el EM introducido por el usuario.
     *
     * @param heroHand mano del Hero
     * @param villainRangeRaw rango textual del villano (ej: "AA,QQ+,AKs")
     * @param board 4 cartas del board en el Turn
     * @param emPercent EM en porcentaje (ej: 30.0)
     */
    public static TurnDecisionResult evaluateDecision(Hand heroHand,
                                                      String villainRangeRaw,
                                                      List<String> board,
                                                      double emPercent) {
        double avgOuts = calculateAverageOuts(heroHand, villainRangeRaw, board);

        // En el Turn quedan 44 cartas posibles en el river (como en el enunciado).
        double equity = (avgOuts / 44.0) * 100.0;

        boolean call = avgOuts > emPercent;

        return new TurnDecisionResult(avgOuts, equity, call);
    }


    // --- Expansión de combinaciones ---

    private static List<Hand> expandRangeToSpecificHands(List<String> genericRange, Set<String> deadCards) {
        List<Hand> concreteHands = new ArrayList<>();

        for (String gen : genericRange) { // gen es "AA", "AKs", "AKo"
            if (gen.length() < 2) continue;
            
            char r1 = gen.charAt(0);
            char r2 = gen.charAt(1);
            boolean isPair = (r1 == r2);
            boolean isSuited = gen.endsWith("s");
            boolean isOffsuit = gen.endsWith("o");

            if (isPair) {
                // Generar 6 pares: hd, hc, hs, dc, ds, cs
                for (int i = 0; i < SUITS.length; i++) {
                    for (int j = i + 1; j < SUITS.length; j++) {
                        tryAddHand(concreteHands, "" + r1 + SUITS[i], "" + r2 + SUITS[j], deadCards);
                    }
                }
            } else if (isSuited) {
                // Generar 4 suited: hh, dd, cc, ss
                for (String s : SUITS) {
                    tryAddHand(concreteHands, "" + r1 + s, "" + r2 + s, deadCards);
                }
            } else if (isOffsuit) {
                // Generar 12 offsuit
                for (String s1 : SUITS) {
                    for (String s2 : SUITS) {
                        if (!s1.equals(s2)) {
                            tryAddHand(concreteHands, "" + r1 + s1, "" + r2 + s2, deadCards);
                        }
                    }
                }
            } else {
                // Si viene sin sufijo (ej "AK"), asumimos que puede ser suited u offsuit (16 combinaciones)
                 for (String s1 : SUITS) {
                    for (String s2 : SUITS) {
                         if (r1 == r2 && s1.equals(s2)) continue; // No AAhh
                         tryAddHand(concreteHands, "" + r1 + s1, "" + r2 + s2, deadCards);
                    }
                }
            }
        }
        return concreteHands;
    }

    private static void tryAddHand(List<Hand> list, String c1, String c2, Set<String> dead) {
        if (!dead.contains(c1) && !dead.contains(c2)) {
            try {
                list.add(new Hand(c1, c2));
            } catch (Exception ignored) {}
        }
    }
}