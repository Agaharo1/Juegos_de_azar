package p3.model;

import java.util.Arrays;

/**
 * Gestiona el estado de apuestas de una ronda para 6 jugadores.
 * Simplificación suficiente para la práctica:
 *  - currentBet: apuesta máxima actual en esta ronda
 *  - betsInRound[p]: contribución del jugador p en la ronda
 *  - stacks[p]: fichas restantes del jugador p
 *  - totalPot: acumulado histórico antes de la ronda actual
 */
public class BettingState {

    public static final int STARTING_STACK = 2000;
    private static final int PLAYER_COUNT = 6;

    /** Apuesta actual a la que deben igualar los jugadores. */
    private int currentBet = 0;

    /** Contribución de cada jugador en la ronda vigente. */
    private final int[] betsInRound = new int[PLAYER_COUNT];

    /** Fichas restantes de cada jugador. */
    private final int[] stacks = new int[PLAYER_COUNT];

    /** Pozo total acumulado antes de la ronda actual. */
    private int totalPot = 0;

    // -------------------------------------------------------
    //                   CONSTRUCTOR
    // -------------------------------------------------------
    public BettingState() {
        Arrays.fill(stacks, STARTING_STACK);
    }

    // -------------------------------------------------------
    //                   RESETEOS
    // -------------------------------------------------------

    /** Reinicia todo el estado del torneo. */
    public void reset() {
        Arrays.fill(stacks, STARTING_STACK);
        Arrays.fill(betsInRound, 0);
        currentBet = 0;
        totalPot = 0;
    }

    /**
     * Comienza una nueva ronda de apuestas (Flop, Turn, River).
     * Mueve lo acumulado en betsInRound al pozo y resetea apuestas de la nueva calle.
     */
    public void newBettingRound() {
        for (int contrib : betsInRound) totalPot += contrib;
        Arrays.fill(betsInRound, 0);
        currentBet = 0;
    }

    // -------------------------------------------------------
    //                   GETTERS
    // -------------------------------------------------------

    public int getCurrentBet() { return currentBet; }
    public int getTotalPot() { return totalPot; }
    public int getStack(int p) { return stacks[p]; }
    public int getBetInRound(int p) { return betsInRound[p]; }

    /** Cantidad necesaria para igualar. Si <=0, se puede hacer check. */
    public int toCall(int p) {
        return currentBet - betsInRound[p];
    }

    public void setStacks(int p, int stack) {
        stacks[p] = stack;
    }

    // -------------------------------------------------------
    //                   ACCIONES
    // -------------------------------------------------------

    /**
     * Acción de apostar/subir (raise).
     *
     * amountToRaise = cantidad EXTRA por encima de un call.
     * El jugador pone: toCall(p) + amountToRaise.
     *
     * Devuelve true si la acción se completa.
     */
    public boolean actionBet(int p, int amountToRaise) {
        int callNeeded = toCall(p);
        int totalToPut = callNeeded + amountToRaise;

        if (totalToPut <= 0) return false;

        // All-in si no llega
        if (totalToPut > stacks[p]) {
            totalToPut = stacks[p];
        }

        // Aplicar apuesta
        stacks[p] -= totalToPut;
        betsInRound[p] += totalToPut;

        // Actualizar la apuesta máxima de la ronda
        currentBet = betsInRound[p];
        return true;
    }

    /**
     * Acción de igualar la apuesta (call).
     * Si no hay diferencia, basta con check.
     */
    public boolean actionCall(int p) {
        int need = toCall(p);
        if (need <= 0) return true; // check

        // Ajustar a stack disponible
        if (need > stacks[p]) need = stacks[p];

        stacks[p] -= need;
        betsInRound[p] += need;
        return true;
    }

    /** Acción de Fold (no modifica apuestas; el GUI/estado externo gestiona la mano). */
    public void actionFold(int p) {
        // Fold sólo se registra fuera. Esta clase no elimina apuestas ya comprometidas.
    }

    /** Acción de Check (siempre válido cuando toCall()==0). */
    public void actionCheck(int p) {
        // No hace nada: check ya está implícito si betsInRound[p]==currentBet.
    }
}
