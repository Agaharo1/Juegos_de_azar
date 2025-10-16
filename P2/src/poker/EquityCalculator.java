package poker;

import java.util.*;

/**
 * Clase base para el cálculo de equity.
 * Por ahora devuelve valores aleatorios (dummy).
 */
public class EquityCalculator {

    /**
     * Calcula la equity de cada jugador dadas las cartas comunes.
     * @param jugadores Lista con los nombres o identificadores de los jugadores.
     * @param board Lista con las cartas comunes (puede estar vacía).
     * @return Un mapa con la equity (valor entre 0 y 1) de cada jugador.
     */
    public Map<String, Double> calcularEquity(List<String> jugadores, List<String> board) {
        Map<String, Double> resultado = new LinkedHashMap<>();

        for (String j : jugadores) {
            double valor = Math.random(); // valor aleatorio
            resultado.put(j, valor);
        }

        return resultado;
    }

    // Método de prueba
    public static void main(String[] args) {
    	EquityCalculator calc = new EquityCalculator();
        List<String> jugadores = Arrays.asList("Hero", "Villano1", "Villano2");
        Map<String, Double> eq = calc.calcularEquity(jugadores, new ArrayList<>());
        System.out.println(eq);
    }
}
