package poker;

public class Carta {
    private char valor; // A, K, Q, J, T, 9...2
    private char palo;  // h (corazones), d (diamantes), c (tréboles), s (picas)

    public Carta(char valor, char palo) {
        this.valor = valor;
        this.palo = palo;
    }

    public char getValor() {
        return valor;
    }

    public char getPalo() {
        return palo;
    }

    // Método auxiliar: convierte el valor de la carta a número para comparaciones
    public int getValorNumerico() {
        switch (valor) {
            case 'A': return 14;
            case 'K': return 13;
            case 'Q': return 12;
            case 'J': return 11;
            case 'T': return 10;
            default: return Character.getNumericValue(valor); // 2-9
        }
    }

    @Override
    public String toString() {
        return "" + valor + palo;
    }
}
