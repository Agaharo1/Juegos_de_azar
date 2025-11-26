package tp2.logic;

import tp2.model.Hand;
import java.util.*;

public class PokerStoveEquityCalculator implements EquityCalculator {
    @Override
    public Map<String, Double> calcularEquity(
        List<String> names, List<Hand> hands, List<String> board, int trials, long seed) {
        return PokerStoveAdapter.computeEquityWithFallback(names, hands, board, trials, seed);
    }
}
