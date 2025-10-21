package tp2.logic;

import java.util.ArrayList;
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

    public int remaining() {
        return cards.size() - index;
    }
}
