package tp2.logic;

import java.util.List;
import java.util.Map;

import tp2.model.Hand;

public interface EquityCalculator {
    Map<String, Double> calcularEquity(
            List<String> names,
            List<Hand> hands,
            List<String> board,
            int trials,
            long seed
    );
}
