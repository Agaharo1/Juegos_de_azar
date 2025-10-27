package tp2.logic;

import java.util.*;

/**
 * Dummy determinista: genera equities normalizadas a 100% con una semilla
 * derivada de (orden de jugadores + board visible) para que no "bailen".
 */
public class EquityCalculator {

    public Map<String, Double> calcularEquity(List<String> jugadores, List<String> board) {
        Map<String, Double> out = new LinkedHashMap<>();

        String seedKey = String.join("-", jugadores) + "|" + String.join("-", board);
        long seed = seedKey.hashCode();
        Random rnd = new Random(seed);

        double sum = 0.0;
        List<Double> vals = new ArrayList<>(jugadores.size());
        for (int i = 0; i < jugadores.size(); i++) {
            double v = 1.0 + rnd.nextDouble(); // 1..2
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
