package tp2.tests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import tp2.logic.EquityCalculator;
import tp2.logic.RealEquityCalculator;
import tp2.model.Hand;

import java.util.*;

public class EquityCalculatorTest {

    @Test
    public void testEquitiesSumTo100() {
        EquityCalculator calc = new RealEquityCalculator();

        List<String> nombres = List.of("P1", "P2", "P3");
        List<Hand> manos = List.of(
                new Hand("Ah", "Ad"),  // Ases
                new Hand("Kc", "Kd"),  // Reyes
                new Hand("Qh", "Qs")   // Damas
        );
        List<String> board = Collections.emptyList(); // Preflop

        Map<String, Double> result = calc.calcularEquity(nombres, manos, board, 2000, 1234L);

        double suma = result.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(100.0, suma, 0.5, "Las equities deben sumar ~100%");
    }

    @Test
    public void testAllPlayersHavePositiveEquity() {
        EquityCalculator calc = new RealEquityCalculator();

        List<String> nombres = List.of("P1", "P2");
        List<Hand> manos = List.of(
                new Hand("Ah", "Ad"),
                new Hand("Ks", "Kd")
        );
        List<String> board = Collections.emptyList();

        Map<String, Double> result = calc.calcularEquity(nombres, manos, board, 2000, 42L);

        for (double v : result.values()) {
            assertTrue(v > 0.0, "Cada jugador debe tener equity positiva");
        }
    }
}
