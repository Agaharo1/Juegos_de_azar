package tp2.logic;

import java.util.*;

public class RangeParser {
    /** Stub: separa por comas y limpia espacios. */
    public static List<String> parse(String rango) {
        if (rango == null) return Collections.emptyList();
        String[] partes = rango.split(",");
        List<String> manos = new ArrayList<>();
        for (String p : partes) {
            String t = p.trim();
            if (!t.isEmpty()) manos.add(t);
        }
        return manos;
    }
}
