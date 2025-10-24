package tp2.logic;

import java.util.*;

public class RangeParser {

    // Acepta pares (AA, TT), suited (AKs) y off (AQo). Separados por comas.
    private static final String TOKEN = "([2-9TJQKA]{2}[so]?)";

    /** Validación simple del formato del rango textual. */
    public static boolean isBasicFormat(String input) {
        if (input == null) return false;
        return input.trim().matches("^\\s*" + TOKEN + "(\\s*,\\s*" + TOKEN + ")*\\s*$");
    }

    /**
     * Parsea el rango textual. 
     * Si el formato es inválido, lanza IllegalArgumentException.
     */
    public static List<String> parse(String rango) {
        if (rango == null || rango.isBlank()) {
            throw new IllegalArgumentException("El rango no puede estar vacío");
        }

        if (!isBasicFormat(rango)) {
            throw new IllegalArgumentException("Formato de rango inválido. Usa algo como: AA,AKs,AQo");
        }

        String[] partes = rango.split(",");
        List<String> manos = new ArrayList<>();

        for (String p : partes) {
            String t = p.trim().toUpperCase(Locale.ROOT);
            if (!t.isEmpty()) manos.add(t);
        }

        return manos;
    }
}
