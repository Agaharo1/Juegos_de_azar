package p3.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import p3.gui.Phase;

/**
 * Representa el board de Texas Hold'em: flop (3), turn (1), river (1).
 * Internamente guarda siempre 5 posiciones:
 *   cards[0] = flop1
 *   cards[1] = flop2
 *   cards[2] = flop3
 *   cards[3] = turn
 *   cards[4] = river
 *
 * Una posición vacía se representa con "".
 *
 * Todas las validaciones se delegan en CardValidator.
 */
public final class Board {

    /** Array fijo con las 5 posiciones del board. */
    private final String[] cards = {"", "", "", "", ""};

    // ---------------------------------------------------------
    //                  MANEJO DEL BOARD
    // ---------------------------------------------------------

    /** Limpia completamente el board. */
    public void clear() {
        Arrays.fill(cards, "");
    }

    /** Establece las 3 cartas del flop. */
    public void setFlop(String c1, String c2, String c3) {
        validate(c1); validate(c2); validate(c3);
        cards[0] = c1; 
        cards[1] = c2; 
        cards[2] = c3;
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

    // ---------------------------------------------------------
    //                     CONSULTAS
    // ---------------------------------------------------------

    /**
     * Devuelve solo las cartas visibles (no vacías) en orden.
     * Ej: [Ah, Kd, Ts] si solo hay flop.
     */
    public List<String> visible() {
        List<String> list = new ArrayList<>(5);
        for (String c : cards) {
            if (c != null && !c.isEmpty()) list.add(c);
        }
        return list;
    }

    /** Copia completa del array con las 5 posiciones (incluye vacíos). */
    public String[] raw() {
        return cards.clone();
    }

    /**
     * Fase deducida automáticamente por nº de cartas visibles:
     *   0–2  → PREFLOP  
     *   3    → FLOP  
     *   4    → TURN  
     *   5    → RIVER  
     */
    public Phase phase() {
        int n = visible().size();
        return switch (n) {
            case 5 -> Phase.RIVER;
            case 4 -> Phase.TURN;
            case 3 -> Phase.FLOP;
            default -> Phase.PREFLOP;
        };
    }

    // ---------------------------------------------------------
    //                     HELPERS
    // ---------------------------------------------------------

    /** Lanza excepción si la carta no tiene formato válido. */
    private static void validate(String code) {
        if (!CardValidator.isValidCode(code)) {
            throw new IllegalArgumentException("Código de carta inválido en board: " + code);
        }
    }
}
