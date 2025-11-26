package tp2.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Hand {
    private final String c1;
    private final String c2;

    public Hand(String c1, String c2) {
        if (!CardValidator.isValidCode(c1) || !CardValidator.isValidCode(c2)) {
            throw new IllegalArgumentException("Código de carta inválido (usa formato como Ah, Kd, Tc, ...)");
        }
        if (c1.equals(c2)) {
            throw new IllegalArgumentException("Una mano no puede tener dos cartas iguales.");
        }
        this.c1 = c1;
        this.c2 = c2;
    }

    /** “AhKd” → new Hand("Ah","Kd") */
    public static Hand fromString(String s) {
        if (s == null || s.length() < 4) {
            throw new IllegalArgumentException("Cadena mano inválida (esperado 4 chars, ej: AhKd).");
        }
        return new Hand(s.substring(0,2), s.substring(2,4));
    }

    public String card1() { return c1; }
    public String card2() { return c2; }

    public List<String> asList() { return Arrays.asList(c1, c2); }

    /** “AhKd” */
    @Override public String toString() { return c1 + c2; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hand)) return false;
        Hand hand = (Hand) o;
        return c1.equals(hand.c1) && c2.equals(hand.c2);
    }

    @Override public int hashCode() { return Objects.hash(c1, c2); }
}
