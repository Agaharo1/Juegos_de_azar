package p3.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Mazo de 52 cartas para Texas Hold'em.
 * Las cartas se representan como strings tipo "Ah", "Kd", "Ts", etc.
 *
 * Funcionalidad:
 *  - Construcción y barajado
 *  - Robar cartas una a una con draw()
 *  - Eliminar cartas ya usadas mediante removeCards()
 *  - Consultar cuántas quedan
 */
public class Deck {

    private final List<String> cards = new ArrayList<>();
    private int index = 0;

    public Deck() {
        String[] ranks = {"A","K","Q","J","T","9","8","7","6","5","4","3","2"};
        String[] suits = {"h","d","c","s"};

        for (String r : ranks) {
            for (String s : suits) {
                cards.add(r + s);
            }
        }
        Collections.shuffle(cards);
    }

    /**
     * Roba la siguiente carta del mazo.
     * @return carta en formato "Ah", "7d", etc.
     */
    public String draw() {
        if (index >= cards.size())
            throw new IllegalStateException("No hay más cartas en el mazo");

        return cards.get(index++);
    }

    /**
     * Elimina cartas ya usadas del mazo (solo afecta a las que quedan por robar).
     * Reinicia el índice para que draw() empiece desde el inicio del nuevo mazo.
     */
    public void removeCards(Collection<String> codes) {
        if (codes == null || codes.isEmpty())
            return;

        // Cartas aun no robadas
        List<String> remaining = new ArrayList<>(cards.subList(index, cards.size()));

        // Quitamos las que estén en 'codes'
        remaining.removeIf(codes::contains);

        // Actualizamos el mazo
        cards.clear();
        cards.addAll(remaining);

        index = 0;
    }

    /**
     * @return número de cartas aún disponibles para robar.
     */
    public int remaining() {
        return cards.size() - index;
    }
}
