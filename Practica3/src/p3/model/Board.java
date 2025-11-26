package p3.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import p3.gui.Phase;

/**
 * Board de 5 posiciones: flop[0..2], turn[3], river[4].
 * Guarda códigos tipo "Ah","Td"... Vacío = "".
 */
public final class Board {
    private final String[] cards = {"", "", "", "", ""};

    /** Limpia todas las cartas del board. */
    public void clear() {
        Arrays.fill(cards, "");
    }

    /** Establece el flop (3 cartas). */
    public void setFlop(String c1, String c2, String c3) {
        validate(c1); validate(c2); validate(c3);
        cards[0] = c1; cards[1] = c2; cards[2] = c3;
    }

    /** Establece el turn (4ª carta). */
    public void setTurn(String c4) {
        validate(c4);
        cards[3] = c4;
    }

    /** Establece el river (5ª carta). */
    public void setRiver(String c5) {
        validate(c5);
        cards[4] = c5;
    }

    /** Devuelve solo las cartas no vacías del board, en orden. */
    public List<String> visible() {
        List<String> out = new ArrayList<>(5);
        for (String c : cards) if (c != null && !c.isEmpty()) out.add(c);
        return out;
    }

    /** Array completo (incluye strings vacíos). */
    public String[] raw() {
        return cards.clone();
    }

    /** Fase deducida por nº de cartas visibles. */
    public Phase phase() {
        int n = visible().size();
        if (n >= 5) return Phase.RIVER;
        if (n == 4)  return Phase.TURN;
        if (n >= 3)  return Phase.FLOP;
        return Phase.PREFLOP;
    }

    // ---- helpers ----
    private static void validate(String code) {
        if (!CardValidator.isValidCode(code)) {
            throw new IllegalArgumentException("Código de carta inválido en board: " + code);
        }
    }
}
