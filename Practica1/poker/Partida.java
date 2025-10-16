package poker;

import java.util.*;

public class Partida {
    public final int n;
    public final List<String> ids;             // "J1", "J2", ...
    public final List<List<Carta>> manos;      // las 2 cartas de cada jugador
    public final List<Carta> comunes;          // las 5 comunes

    // estos se rellenan más tarde cuando calculemos las mejores manos
    public final List<Mano.Resultado> resultados = new ArrayList<>();
    public final List<List<Carta>> mejoresCinco = new ArrayList<>();

    public Partida(int n, List<String> ids, List<List<Carta>> manos, List<Carta> comunes) {
        this.n = n;
        this.ids = ids;
        this.manos = manos;
        this.comunes = comunes;
    }
}
