package tp2.logic;

import tp2.model.Hand;
import java.util.List;
import java.util.Map;

public interface EquityCalculator {
    Map<String, Double> calcularEquity(
            List<String> names,
            List<Hand> hands,
            List<String> board,
            int trials,
            long seed
    );
}
