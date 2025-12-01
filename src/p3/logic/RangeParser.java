package p3.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * RangeParser convierte expresiones de rango como:
 *
 *   JJ+
 *   T2s+
 *   52o+
 *   ATs-A2s
 *   QQ-AA
 *   JJ+,ATs-A8s,76o,54o
 *
 * en una lista de manos individuales en notación 169 (AA, AKs, QJo…).
 *
 * Reglas:
 *  - "+" expande hacia arriba (JJ+ → JJ,QQ,KK,AA).
 *  - "-" expande rangos crecientes dentro de la categoría indicada.
 *  - Ambos soportan suited 's', offsuit 'o' y parejas.
 */
public final class RangeParser {

    private static final String RANKS = "23456789TJQKA";

    private RangeParser() {}

    /* =============================================================
     *                   VALIDACIÓN BÁSICA
     * ============================================================= */

    /** Comprueba si el formato es compatible con el parser. */
    public static boolean isBasicFormat(String input) {
        if (input == null) return false;

        // Token típico: "JJ+", "ATs-A2s", "QQ-AA", "AKo"
        String token = "([2-9TJQKA]{2}[so]?([+-][2-9TJQKA]{0,2}[so]?)?)";

        String regex = "^\\s*" + token + "(\\s*,\\s*" + token + ")*\\s*$";
        return input.trim().matches(regex);
    }


    /* =============================================================
     *                       PARSING GENERAL
     * ============================================================= */

    /**
     * Parsea un rango textual como "JJ+,ATs-A8s,76o".
     * Devuelve manos individuales en notación textual (169).
     */
    public static List<String> parse(String rango) {
        if (rango == null || rango.isBlank())
            return Collections.emptyList();

        List<String> result = new ArrayList<>();

        for (String token : rango.split(",")) {
            token = token.trim().toUpperCase(Locale.ROOT);
            if (token.isEmpty()) continue;

            if (token.endsWith("+")) {
                result.addAll(expandPlus(token));
            } else if (token.contains("-")) {
                result.addAll(expandDash(token));
            } else {
                result.add(token);
            }
        }

        return result;
    }


    /* =============================================================
     *                       EXPANSIÓN "+"
     * ============================================================= */

    /** Expande tokens con '+' como "JJ+", "T2s+", "52o+". */
    private static List<String> expandPlus(String token) {
        List<String> out = new ArrayList<>();

        // Remove "+"
        token = token.substring(0, token.length() - 1);

        // Pares
        if (isPair(token)) {
            char start = token.charAt(0);
            int idx = RANKS.indexOf(start);

            for (int i = idx; i < RANKS.length(); i++) {
                out.add("" + RANKS.charAt(i) + RANKS.charAt(i));
            }
            return out;
        }

        // No parejas: T2s+, 52o+, etc.
        char high = token.charAt(0);
        char low = token.charAt(1);
        char type = token.charAt(2); // 's' o 'o'

        int idxHigh = RANKS.indexOf(high);
        int idxLow  = RANKS.indexOf(low);

        // Ejemplo: T2s+ → T2s, T3s, ..., T9s
        for (int i = idxLow; i < idxHigh; i++) {
            out.add("" + high + RANKS.charAt(i) + type);
        }

        return out;
    }


    /* =============================================================
     *                       EXPANSIÓN "-"
     * ============================================================= */

    /** Expande rangos con "-" como "QQ-AA" o "ATs-A2s". */
    private static List<String> expandDash(String token) {
        List<String> out = new ArrayList<>();

        String[] parts = token.split("-");
        if (parts.length != 2)
            return out;

        String a = parts[0].trim();
        String b = parts[1].trim();

        // Rango de parejas
        if (isPair(a) && isPair(b))
            return expandPairRange(a, b);

        // Rango no pareja
        return expandNonPairRange(a, b);
    }


    /* =============================================================
     *              EXPANSIÓN RANGOS PAREJA (QQ-AA)
     * ============================================================= */

    private static List<String> expandPairRange(String start, String end) {
        List<String> out = new ArrayList<>();

        int i1 = RANKS.indexOf(start.charAt(0));
        int i2 = RANKS.indexOf(end.charAt(0));

        if (i1 > i2) {
            int tmp = i1; i1 = i2; i2 = tmp;
        }

        for (int i = i1; i <= i2; i++) {
            out.add("" + RANKS.charAt(i) + RANKS.charAt(i));
        }

        return out;
    }


    /* =============================================================
     *           EXPANSIÓN RANGOS NO PAREJA (ATs-A2s)
     * ============================================================= */

    private static List<String> expandNonPairRange(String start, String end) {
        List<String> out = new ArrayList<>();

        // Ej: ATs → A,T,s
        char highStart = start.charAt(0);
        char lowStart  = start.charAt(1);
        char type      = start.charAt(2); // 's' o 'o'

        char lowEnd = end.charAt(1);

        int i1 = RANKS.indexOf(lowStart);
        int i2 = RANKS.indexOf(lowEnd);

        if (i1 < 0 || i2 < 0)
            return out;

        if (i1 > i2) {
            int tmp = i1; i1 = i2; i2 = tmp;
        }

        for (int i = i1; i <= i2; i++) {
            out.add("" + highStart + RANKS.charAt(i) + type);
        }

        return out;
    }


    /* =============================================================
     *                        UTILIDADES
     * ============================================================= */

    private static boolean isPair(String token) {
        return token.length() == 2 && token.charAt(0) == token.charAt(1);
    }
}
