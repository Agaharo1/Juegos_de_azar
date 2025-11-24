package p3.model;

import java.util.Arrays;
import java.util.List;

/**
 * Mantiene el estado de las apuestas para los 6 jugadores en una ronda.
 * Simplifica la estructura de apuestas para la práctica.
 */
public class BettingState {
    
    // Stack inicial de fichas por jugador
    public static final int STARTING_STACK = 2000;

    // Apuesta mínima a la que se enfrenta el jugador (call/raise)
    private int currentBet = 0;
    
    // Lo que ha contribuido CADA JUGADOR al pozo en la ronda actual.
    private final int[] betsInRound = new int[6]; 
    
    // Fichas totales de cada jugador
    private final int[] stacks = new int[6];
    
    // Pozo total acumulado
    private int totalPot = 0;

    public BettingState() {
        Arrays.fill(stacks, STARTING_STACK);
    }
    
    public void reset() {
        Arrays.fill(betsInRound, 0);
        Arrays.fill(stacks, STARTING_STACK);
        currentBet = 0;
        totalPot = 0;
    }
    
    /**
     * Resetea el estado de las apuestas para una nueva ronda (Flop, Turn, River).
     * Las apuestas de la ronda anterior ya deberían haber pasado a totalPot.
     */
    public void newBettingRound() {
        // Mover las apuestas de la ronda anterior al pozo total (si hubiera una ronda anterior)
        for (int bet : betsInRound) {
            totalPot += bet;
        }
        Arrays.fill(betsInRound, 0);
        currentBet = 0;
    }
    
    // --- Getters y Setters ---

    public int getCurrentBet() { return currentBet; }
    public int getTotalPot() { return totalPot; }
    public int getStack(int playerIndex) { return stacks[playerIndex]; }
    public int getBetInRound(int playerIndex) { return betsInRound[playerIndex]; }
    
    public int toCall(int playerIndex) {
        return currentBet - betsInRound[playerIndex];
    }
    
    public void setStacks(int index, int stack) { stacks[index] = stack; }
    
    /**
     * Aplica la acción de apostar/subir.
     * @param playerIndex Índice del jugador
     * @param amountToRaise Cantidad que AÑADE encima de currentBet
     * @return true si la acción es válida
     */
    public boolean actionBet(int playerIndex, int amountToRaise) {
        int amountToPutIn = toCall(playerIndex) + amountToRaise;
        
        if (amountToPutIn > stacks[playerIndex]) {
            // All-in
            amountToPutIn = stacks[playerIndex];
        }
        
        if (amountToPutIn <= 0) return false;

        stacks[playerIndex] -= amountToPutIn;
        betsInRound[playerIndex] += amountToPutIn;
        currentBet = betsInRound[playerIndex]; 
        
        return true;
    }

    
    /**
     * Aplica la acción de igualar la apuesta.
     * @param playerIndex Índice del jugador
     * @return true si la acción es válida
     */
    public boolean actionCall(int playerIndex) {
        int toPutIn = toCall(playerIndex);
        if (toPutIn <= 0) return true; // Ya ha igualado o es Check
        
        // No puede ser más de lo que tiene
        if (toPutIn > stacks[playerIndex]) toPutIn = stacks[playerIndex]; 
        
        stacks[playerIndex] -= toPutIn;
        betsInRound[playerIndex] += toPutIn;
        
        return true;
    }
    
    /**
     * Simula la acción de Fold (no afecta a las apuestas, solo al GameState).
     */
    public void actionFold(int playerIndex) {
        // Solo para claridad. La lógica de quitar la mano está en GameState/GUI.
    }
    
    /**
     * Simula la acción de Check.
     */
    public void actionCheck(int playerIndex) {
        // Solo posible si currentBet == betsInRound[playerIndex]
    }
}