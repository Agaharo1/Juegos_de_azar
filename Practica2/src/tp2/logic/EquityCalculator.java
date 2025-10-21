package tp2.logic;

import java.util.*;

public class EquityCalculator {

    /**
     * Calcula equities "fake" normalizadas a 100%.
     * Mantiene el orden de inserción usando LinkedHashMap.
     */
    public Map<String, Double> calcularEquity(List<String> jugadores, List<String> board) {
        Map<String, Double> out = new LinkedHashMap<>();
        double sum = 0.0;
        List<Double> vals = new ArrayList<>();

        for (int i = 0; i < jugadores.size(); i++) {
            double v = 1.0 + Math.random(); // evitar cero
            vals.add(v);
            sum += v;
        }
        for (int i = 0; i < jugadores.size(); i++) {
            double pct = 100.0 * vals.get(i) / sum;
            out.put(jugadores.get(i), pct);
        }
        return out;
    }
}
