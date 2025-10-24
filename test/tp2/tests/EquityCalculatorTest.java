package tp2.tests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tp2.logic.EquityCalculator;

import java.util.*;

public class EquityCalculatorTest {

    @Test
    public void testEquitiesSumTo100() {
        EquityCalculator calc = new EquityCalculator();
        List<String> jugadores = List.of("AA", "KK", "QQ");
        List<String> board = Collections.emptyList();

        Map<String, Double> result = calc.calcularEquity(jugadores, board);

        double suma = result.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(100.0, suma, 0.0001, "Las equities deben sumar 100%");
    }

    @Test
    public void testAllPlayersHavePositiveEquity() {
        EquityCalculator calc = new EquityCalculator();
        List<String> jugadores = List.of("AA", "KK", "QQ");
        List<String> board = Collections.emptyList();

        Map<String, Double> result = calc.calcularEquity(jugadores, board);

        for (double v : result.values()) {
            assertTrue(v > 0.0, "Cada jugador debe tener equity positiva");
        }
    }

    // Si has añadido getMetodoCalculo() en tu clase, puedes mantener esta prueba:
    // (Si no existe, simplemente elimina este test)
    /*
    @Test
    public void testGetMetodoCalculo() {
        EquityCalculator calc = new EquityCalculator();
        assertTrue(calc.getMetodoCalculo().toLowerCase().contains("dummy"));
    }
    */
}
