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

    /** Stub: separa por comas, limpia espacios y normaliza a mayúsculas. */
    public static List<String> parse(String rango) {
        if (rango == null) return Collections.emptyList();
        String[] partes = rango.split(",");
        List<String> manos = new ArrayList<>();
        for (String p : partes) {
            String t = p.trim().toUpperCase(Locale.ROOT);
            if (!t.isEmpty()) manos.add(t);
        }
        return manos;
    }
}
