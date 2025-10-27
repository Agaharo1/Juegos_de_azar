package tp2.logic;

import tp2.model.Hand;
import java.util.List;
import java.util.Objects;

/**
 * Especificación de jugador para el calculador REAL:
 * - name (obligatorio)
 * - o bien hand (conocida) o bien range (lista de combos tipo "AKs","AQo"...)
 */
public final class PlayerSpec {
    private final String name;
    private final Hand hand;            // puede ser null si se usa range
    private final List<String> range;   // puede ser null si se usa hand

    public PlayerSpec(String name, Hand hand) {
        this.name = Objects.requireNonNull(name, "name");
        this.hand = hand;
        this.range = null;
    }

    public PlayerSpec(String name, List<String> range) {
        this.name = Objects.requireNonNull(name, "name");
        this.range = Objects.requireNonNull(range, "range");
        this.hand = null;
    }

    public String getName()        { return name; }
    public Hand getHand()          { return hand; }
    public List<String> getRange() { return range; }

    public boolean hasHand()  { return hand != null; }
    public boolean hasRange() { return range != null && !range.isEmpty(); }
}
