package poker;

import java.util.*;

//Clase Provisional para que funcionen los botones

public class RangeParser {
    public static List<String> parse(String rango) {
        // Por ahora separa las manos por comas y elimina espacios
        String[] partes = rango.split(",");
        List<String> manos = new ArrayList<>();
        for (String p : partes) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) manos.add(trimmed);
        }
        return manos;
    }
}
