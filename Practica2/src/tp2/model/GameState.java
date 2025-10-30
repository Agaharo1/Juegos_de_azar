package tp2.model;

import tp2.gui.Phase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estado mínimo del juego: manos de 6 jugadores + board + fase.
 * Siempre mantiene 6 posiciones (null si el asiento no tiene mano).
 */
public final class GameState {
    private final List<Hand> players = new ArrayList<>();
    private final Board board = new Board();
    private Phase phase = Phase.PREFLOP;

    /** Crea el estado con 6 slots iniciales en null. */
    public GameState() {
        ensureSize(6);
    }

    /** Vuelve a estado inicial (6 nulls, board vacío, PREFLOP). */
    public void reset() {
        players.clear();
        ensureSize(6);
        board.clear();
        phase = Phase.PREFLOP;
    }

    /** Define/actualiza la mano del jugador 'index' (0..5). Puede ser null. */
    public void setPlayerHand(int index, Hand hand) {
        ensureSize(Math.max(6, index + 1));
        players.set(index, hand);
    }

    /** Acceso inmutable a la lista de 6 manos (puede contener nulls). */
    public List<Hand> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /** Acceso al board (flop/turn/river). */
    public Board getBoard() {
        return board;
    }

    /** Fase actual. */
    public Phase getPhase() {
        return phase;
    }

    /** Establece la fase actual. */
    public void setPhase(Phase p) {
        if (p == null) throw new IllegalArgumentException("Phase no puede ser null");
        this.phase = p;
    }

    /** Garantiza que hay al menos 'n' posiciones (siempre 6 como mínimo). */
    public void ensurePlayersCount(int n) {
        ensureSize(Math.max(6, n));
    }

    /** Todas las cartas en juego (manos + board) como códigos "Ah","Kd"... */
    public List<String> allUsedCards() {
        List<String> out = new ArrayList<>();
        for (Hand h : players) {
            if (h != null) out.addAll(h.asList());
        }
        out.addAll(board.visible());
        return out;
    }

    // ---- helpers ----
    private void ensureSize(int size) {
        while (players.size() < size) players.add(null);
    }
    

    /** Devuelve la mano del jugador i (puede ser null). */
    public Hand getPlayerHand(int index) {
        return players.get(index);
    }

    /** Limpia la mano del jugador i (pone null). */
    public void clearPlayerHand(int index) {
        ensureSize(Math.max(6, index + 1));
        players.set(index, null);
    }

}
