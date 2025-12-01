package p3.logic;

import p3.model.Hand;

/**
 * Utilidades para transformar manos concretas ("AhKd")
 * a su notación en formato 169 ("AKs", "AKo", "TT", etc.).
 */
public final class HandUtils {

    private static final String RANKS = "23456789TJQKA";

    private HandUtils() {}

    /**
     * Convierte una mano concreta a notación 169.
     * Ejemplos:
     *  - AhKh -> AKs
     *  - AhKd -> AKo
     *  - TdTc -> TT
     */
    public static String to169(Hand hand) {
        if (hand == null)
            throw new IllegalArgumentException("hand cannot be null");

        String c1 = hand.card1(); // Ej: "Ah"
        String c2 = hand.card2(); // Ej: "Kd"

        char r1 = c1.charAt(0), s1 = c1.charAt(1);
        char r2 = c2.charAt(0), s2 = c2.charAt(1);

        // Asegurar que r1 es la carta más alta según RANKS
        if (RANKS.indexOf(r1) < RANKS.indexOf(r2)) {
            char tmpR = r1, tmpS = s1;
            r1 = r2; s1 = s2;
            r2 = tmpR; s2 = tmpS;
        }

        // Pares (TT, JJ, AA…)
        if (r1 == r2) {
            return "" + r1 + r2;
        }

        // Suited u offsuit
        return "" + r1 + r2 + ((s1 == s2) ? "s" : "o");
    }
}
