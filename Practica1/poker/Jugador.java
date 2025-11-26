package poker;

import java.util.ArrayList;
import java.util.List;

public class Jugador {
    public final String id;              // p.ej. "J1", "J2", ...
    public final List<Carta> hole;       // sus 2 cartas
    public Mano.Resultado mejor;         // mejor resultado (se rellenará en el apartado 3)
    public List<Carta> mejorCinco;       // las 5 cartas que logran 'mejor'

    public Jugador(String id, List<Carta> hole) {
        this.id = id;
        this.hole = new ArrayList<>(hole);
    }
}
