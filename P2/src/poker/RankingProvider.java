package poker;

import java.util.*;

/**
 * Clase que define un ranking predefinido de manos iniciales.
 * Por ahora contiene 15 combinaciones de manos iniciales ordenadas
 * desde la más fuerte a la más débil.
 */
public class RankingProvider {

    // Lista de manos ordenadas por fuerza (de mejor a peor)
    private static final List<String> ranking = Arrays.asList(
        "AA", "KK", "QQ", "JJ", "AKs", "AQs", "AJs", "KQs",
        "TT", "99", "88", "AKo", "AQo", "AJo", "KQo"
    );

    /**
     * Devuelve la posición de una mano en el ranking (1 = mejor).
     */
    public static int getPosicion(String mano) {
        int index = ranking.indexOf(mano);
        if (index == -1) return 999; // Si no está en la lista, la consideramos baja
        return index + 1;
    }

    /**
     * Devuelve la lista completa del ranking.
     */
    public static List<String> getRanking() {
        return new ArrayList<>(ranking);
    }

    // Método de prueba
    public static void main(String[] args) {
        System.out.println("Ranking de manos iniciales:");
        for (int i = 0; i < ranking.size(); i++) {
            System.out.println((i + 1) + ". " + ranking.get(i));
        }
        System.out.println("Posición de AKs: " + getPosicion("AKs"));
    }
}
