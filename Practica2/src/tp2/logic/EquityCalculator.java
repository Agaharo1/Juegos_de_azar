package tp2.logic;

import java.util.*;

public class EquityCalculator {
    public Map<String, Double> calcularEquity(List<String> jugadores, List<String> board) {
        Map<String, Double> out = new LinkedHashMap<>();
        for (String j : jugadores) out.put(j, Math.random());
        return out;
    }
}
