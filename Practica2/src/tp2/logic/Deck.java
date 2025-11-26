package tp2.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Representa un mazo de 52 cartas para Texas Hold'em.
 * Guarda las cartas como textos tipo "Ah", "Kd", "Ts", etc.
 * Permite robar cartas en orden y eliminar cartas que ya estén en juego.
 */
public class Deck {

    // Lista con todas las cartas del mazo.
    // Se guardan como textos: primera letra = valor, segunda = palo (h, d, c, s).
    private final List<String> cards = new ArrayList<>();

    // Posición actual desde donde se roba la siguiente carta.
    private int index = 0;

    /**
     * Crea un mazo nuevo con las 52 cartas y lo mezcla.
     * Valores de mayor a menor: A, K, Q, J, T, 9...2
     * Palos: h (corazones), d (diamantes), c (tréboles), s (picas)
     */
    public Deck() {
        String[] ranks = {"A","K","Q","J","T","9","8","7","6","5","4","3","2"};
        String[] suits = {"h","d","c","s"};

        // Crea todas las combinaciones posibles (13 x 4 = 52)
        for (String r : ranks) {
            for (String s : suits) {
                cards.add(r + s); // ejemplo: "Ah", "Kd"
            }
        }

        // Mezcla el mazo para que el orden sea aleatorio
        Collections.shuffle(cards);
    }

    /**
     * Roba una carta del mazo.
     * Va devolviendo cartas en el orden interno del mazo mezclado.
     * Si no quedan cartas, lanza un error para avisar.
     */
    public String draw() {
        if (index >= cards.size()) {
            throw new IllegalStateException("Deck is empty"); // no quedan cartas
        }
        return cards.get(index++); // devuelve la carta y avanza la posición
    }

    /**
     * Elimina del mazo un conjunto de cartas (por ejemplo, cartas ya repartidas).
     * Útil para evitar que se repitan cartas que ya están en manos o en el board.
     *
     * Funcionamiento:
     * - Solo mira las cartas que aún no se han robado.
     * - Quita de ese tramo las que coincidan con la lista 'codes'.
     * - Reinicia el índice para seguir robando desde el principio del tramo limpio.
     */
    public void removeCards(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) return;

        // Se queda con la parte del mazo que aún no se ha usado
        List<String> remaining = new ArrayList<>(cards.subList(index, cards.size()));

        // Quita de ahí cualquier carta que esté en 'codes' (cartas ya usadas)
        remaining.removeIf(codes::contains);

        // Sustituye el mazo por ese tramo limpio y reinicia la posición
        cards.clear();
        cards.addAll(remaining);
        index = 0;
    }

    /**
     * Devuelve cuántas cartas quedan por robar.
     * (No cuenta las que ya se han pasado).
     */
    public int remaining() {
        return cards.size() - index;
    }
}
