package p3.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import p3.gui.Phase;

/**
 * Estado mínimo del juego:
 *  - Manos de 6 jugadores (null = asiento vacío)
 *  - Board (flop/turn/river)
 *  - Fase actual
 *  - Cartas foldeadas (no se reutilizan en el mazo)
 *
 * Siempre mantiene como mínimo 6 asientos.
 */
public final class GameState {

    /** Manos de los jugadores (índices 0..5). Puede contener nulls. */
    private final List<Hand> players = new ArrayList<>();

    /** Board completo (flop/turn/river). */
    private final Board board = new Board();

    /** Fase actual del juego. */
    private Phase phase = Phase.PREFLOP;

    /** Cartas foldeadas (para mantener coherencia con el mazo). */
    private final List<String> foldedCards = new ArrayList<>();


    // ---------------------------------------------------------
    //                    CONSTRUCTOR
    // ---------------------------------------------------------

    /** Estado inicial con 6 asientos vacíos. */
    public GameState() {
        ensureSize(6);
    }


    // ---------------------------------------------------------
    //                RESET Y CONFIGURACIÓN
    // ---------------------------------------------------------

    /** Reinicia completamente el estado (manos, board, fase, folded). */
    public void reset() {
        players.clear();
        ensureSize(6);
        board.clear();
        phase = Phase.PREFLOP;
        foldedCards.clear();
    }

    /** Garantiza que hay al menos n asientos (mínimo 6). */
    public void ensurePlayersCount(int n) {
        ensureSize(Math.max(6, n));
    }

    private void ensureSize(int size) {
        while (players.size() < size) {
            players.add(null);
        }
    }


    // ---------------------------------------------------------
    //                    MANOS DE JUGADORES
    // ---------------------------------------------------------

    /** Define o actualiza la mano del jugador index (0..5). Puede ser null. */
    public void setPlayerHand(int index, Hand hand) {
        ensureSize(Math.max(6, index + 1));
        players.set(index, hand);
    }

    /** Devuelve la lista INMUTABLE de manos (puede contener nulls). */
    public List<Hand> getPlayers() {
        return Collections.unmodifiableList(players);
    }


    // ---------------------------------------------------------
    //                       BOARD
    // ---------------------------------------------------------

    public Board getBoard() {
        return board;
    }


    // ---------------------------------------------------------
    //                       PHASE
    // ---------------------------------------------------------

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase p) {
        if (p == null) throw new IllegalArgumentException("Phase no puede ser null");
        this.phase = p;
    }


    // ---------------------------------------------------------
    //               CARTAS EN JUEGO / CARTAS FOLDEADAS
    // ---------------------------------------------------------

    /**
     * Devuelve todas las cartas actualmente en uso:
     *  - Cartas de jugadores activos
     *  - Cartas visibles del board
     */
    public List<String> allUsedCards() {
        List<String> out = new ArrayList<>();
        for (Hand h : players) {
            if (h != null) out.addAll(h.asList());
        }
        out.addAll(board.visible());
        return out;
    }

    /** Devuelve copia inmutable de las cartas foldeadas. */
    public List<String> getFoldedCards() {
        return Collections.unmodifiableList(foldedCards);
    }

    /** Añade al registro una mano foldeada completa (2 cartas). */
    public void addFoldedHand(Hand hand) {
        if (hand != null) foldedCards.addAll(hand.asList());
    }

    /** Limpia la lista de cartas foldeadas. */
    public void clearFoldedCards() {
        foldedCards.clear();
    }
}
