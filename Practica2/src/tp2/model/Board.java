package tp2.model;

import tp2.gui.Phase; // Reutilizamos tu enum Phase

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Board {
    // 5 posiciones: flop[0..2], turn[3], river[4]
    private final String[] cards = {"", "", "", "", ""};

    public void clear() {
        Arrays.fill(cards, "");
    }

    public void setFlop(String c1, String c2, String c3) {
        validate(c1); validate(c2); validate(c3);
        cards[0] = c1; cards[1] = c2; cards[2] = c3;
        // turn/river se mantienen como estén (vacíos normalmente)
    }

    public void setTurn(String c4) {
        validate(c4);
        cards[3] = c4;
    }

    public void setRiver(String c5) {
        validate(c5);
        cards[4] = c5;
    }

    public List<String> visible() {
        List<String> out = new ArrayList<>(5);
        for (String c : cards) if (c != null && !c.isEmpty()) out.add(c);
        return out;
    }

    public String[] raw() { return cards.clone(); }

    public Phase phase() {
        int n = visible().size();
        if (n >= 5) return Phase.RIVER;
        if (n == 4)  return Phase.TURN;
        if (n >= 3)  return Phase.FLOP;
        return Phase.PREFLOP;
    }

    private static void validate(String code) {
        if (!CardValidator.isValidCode(code)) {
            throw new IllegalArgumentException("Código de carta inválido en board: " + code);
        }
    }
}
