package tp2.model;

import tp2.gui.Phase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estado mínimo del juego: manos de jugadores + board + fase.
 * No reparte; solo almacena y valida lo que le pongan.
 */
public final class GameState {
    private final List<Hand> players = new ArrayList<>();
    private final Board board = new Board();
    private Phase phase = Phase.PREFLOP;

    public void reset() {
        players.clear();
        board.clear();
        phase = Phase.PREFLOP;
    }

    /** Define/actualiza la mano de un jugador (por índice). */
    public void setPlayerHand(int index, Hand hand) {
        ensureSize(index + 1);
        players.set(index, hand);
    }

    /** Acceso inmutable a las manos. */
    public List<Hand> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Board getBoard() { return board; }

    public Phase getPhase() { return phase; }

    public void setPhase(Phase p) {
        if (p == null) throw new IllegalArgumentException("Phase no puede ser null");
        this.phase = p;
    }

    /** Devuelve todas las cartas en juego (manos + board) como códigos. */
    public List<String> allUsedCards() {
        List<String> out = new ArrayList<>();
        for (Hand h : players) {
            if (h != null) out.addAll(h.asList());
        }
        out.addAll(board.visible());
        return out;
    }

    private void ensureSize(int size) {
        while (players.size() < size) players.add(null);
    }
}
