package p3.logic;

import p3.model.Hand;

/**
 * Utilidades para transformar manos concretas (como AhKd)
 * a su notación "169" (AKs, AKo, TT, etc.).
 */
public final class HandUtils {

    private static final String RANKS = "23456789TJQKA";

    private HandUtils() {}

    /**
     * Convierte una mano concreta en notación 169.
     * Ejemplos:
     *  - AhKh -> AKs
     *  - AhKd -> AKo
     *  - TdTc -> TT
     */
    public static String to169(Hand hand) {
        if (hand == null)
            throw new IllegalArgumentException("La mano no puede ser nula.");

        String c1 = hand.card1(); // Ej: "Ah"
        String c2 = hand.card2(); // Ej: "Kd"

        char r1 = c1.charAt(0); // Valor
        char s1 = c1.charAt(1); // Palo
        char r2 = c2.charAt(0);
        char s2 = c2.charAt(1);

        // Asegurar orden: carta alta primero según RANKS
        if (RANKS.indexOf(r1) < RANKS.indexOf(r2)) {
            // Intercambiar si la primera es más baja
            char tmpR = r1, tmpS = s1;
            r1 = r2; s1 = s2;
            r2 = tmpR; s2 = tmpS;
        }

        // Pares
        if (r1 == r2) {
            return "" + r1 + r2;
        }

        // Suited o offsuit
        boolean suited = (s1 == s2);
        return "" + r1 + r2 + (suited ? "s" : "o");
    }
}
