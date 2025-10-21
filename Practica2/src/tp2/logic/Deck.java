package tp2.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    /** Roba una carta del mazo. Lanza excepción si no quedan. */
    public String draw() {
        if (index >= cards.size()) {
            throw new IllegalStateException("Deck is empty");
        }
        return cards.get(index++);
    }

    /** Elimina del mazo todas las 'codes' evitando conflictos con cartas ya usadas. */
    public void removeCards(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) return;
        // Conserva solo el tramo aún no robado, filtra y reinicia índice.
        List<String> remaining = new ArrayList<>(cards.subList(index, cards.size()));
        remaining.removeIf(codes::contains);
        cards.clear();
        cards.addAll(remaining);
        index = 0;
    }

    public int remaining() {
        return cards.size() - index;
    }
}
