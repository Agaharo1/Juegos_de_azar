package tp2.logic;

import java.util.*;

/**
 * RangeParser amplía el soporte de rangos textuales.
 * Permite expresiones como:
 *  - JJ+           -> JJ,QQ,KK,AA
 *  - T2s+          -> T2s,T3s,...,T9s
 *  - 52o+          -> 52o,53o,...,T9o
 *  - ATs-A2s       -> ATs,A9s,A8s,...,A2s
 *  - QQ-AA         -> QQ,KK,AA
 *  - Mezclas: JJ+,ATs-A8s,76o,54o
 */
public class RangeParser {

    private static final String RANKS = "23456789TJQKA";

    /** Validación simple del formato del rango textual básico. */
    public static boolean isBasicFormat(String input) {
        if (input == null) return false;
        // Permite tokens como JJ+, ATs-A2s, QQ-AA, además de los básicos
        String token = "([2-9TJQKA]{2}[so]?([+-][2-9TJQKA]{0,2}[so]?)?)";
        String regex = "^\\s*" + token + "(\\s*,\\s*" + token + ")*\\s*$";
        return input.trim().matches(regex);
    }


    /**
     * Parsea un rango textual (como "JJ+,ATs-A8s,76o,54o") a una lista de manos individuales.
     */
    public static List<String> parse(String rango) {
        if (rango == null || rango.isBlank()) return Collections.emptyList();

        List<String> manos = new ArrayList<>();
        for (String token : rango.split(",")) {
            token = token.trim().toUpperCase(Locale.ROOT);
            if (token.isEmpty()) continue;

            if (token.endsWith("+")) {
                manos.addAll(expandPlus(token));
            } else if (token.contains("-")) {
                manos.addAll(expandRange(token));
            } else {
                manos.add(token);
            }
        }
        return manos;
    }

    /** Expande los tokens con '+' como JJ+, T2s+, 52o+ */
    private static List<String> expandPlus(String token) {
        List<String> result = new ArrayList<>();
        token = token.substring(0, token.length() - 1); // eliminar '+'

        // --- Pares: JJ+ -> JJ,QQ,KK,AA ---
        if (token.length() == 2 && token.charAt(0) == token.charAt(1)) {
            char start = token.charAt(0);
            int idx = RANKS.indexOf(start);
            for (int i = idx; i < RANKS.length(); i++) {
                result.add("" + RANKS.charAt(i) + RANKS.charAt(i));
            }
            return result;
        }

        // --- No pareja: T2s+, 52o+ ---
        char high = token.charAt(0);
        char low = token.charAt(1);
        char type = token.charAt(2); // 's' o 'o'

        int idxHigh = RANKS.indexOf(high);
        int idxLow = RANKS.indexOf(low);

        // Por convenio: misma primera carta, subir la segunda hasta una por debajo de la alta
        for (int i = idxLow; i <= idxHigh-1; i++) {
            result.add("" + high + RANKS.charAt(i) + type);
        }
        return result;
    }

    /** Determina si el rango con '-' es de parejas o no, y delega. */
    private static List<String> expandRange(String token) {
        List<String> result = new ArrayList<>();
        String[] parts = token.split("-");
        if (parts.length != 2) return result;

        String start = parts[0].trim();
        String end = parts[1].trim();

        // Pares (QQ-AA)
        if (isPair(start) && isPair(end)) {
            return expandPairRange(start, end);
        }

        // No pareja (ATs-A2s, KQo-KTo, etc.)
        return expandNonPairRange(start, end);
    }

    /** Expande un rango de parejas (por ejemplo QQ-AA). */
    private static List<String> expandPairRange(String start, String end) {
        List<String> result = new ArrayList<>();
        int i1 = RANKS.indexOf(start.charAt(0));
        int i2 = RANKS.indexOf(end.charAt(0));

        // Asegurar que se recorra en orden creciente
        if (i1 > i2) {
            int tmp = i1;
            i1 = i2;
            i2 = tmp;
        }

        for (int i = i1; i <= i2; i++) {
            result.add("" + RANKS.charAt(i) + RANKS.charAt(i));
        }
        return result;
    }

    /** Expande un rango no pareja (ATs-A2s, KQo-KTo). */
    private static List<String> expandNonPairRange(String start, String end) {
        List<String> result = new ArrayList<>();

        char high = start.charAt(0);
        char type = start.charAt(2); // s / o
        int i1 = RANKS.indexOf(start.charAt(1));
        int i2 = RANKS.indexOf(end.charAt(1));

        if (i1 < 0 || i2 < 0) return result;
        // Ordenar índices (por si vienen invertidos)
        if (i1 > i2) {
            int tmp = i1;
            i1 = i2;
            i2 = tmp;
        }

        for (int i = i1; i <= i2; i++) {
            result.add("" + high + RANKS.charAt(i) + type);
        }
        return result;
    }

    /** Devuelve true si la mano es pareja (como "TT", "QQ", "AA"). */
    private static boolean isPair(String token) {
        return token.length() == 2 && token.charAt(0) == token.charAt(1);
    }
}
