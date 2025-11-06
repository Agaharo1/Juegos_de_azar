package p3.logic;

import java.util.List;
import java.util.Map;

import p3.model.Hand;

public class PokerStoveEquityCalculator implements EquityCalculator {
    @Override
    public Map<String, Double> calcularEquity(
        List<String> names, List<Hand> hands, List<String> board, int trials, long seed) {
        return PokerStoveAdapter.computeEquityWithFallback(names, hands, board, trials, seed);
    }
}
