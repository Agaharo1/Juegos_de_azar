package poker;

import java.util.*;
import java.util.stream.Collectors;

public class Mano {
    private final List<Carta> cartas; // exactamente 5

    public Mano(List<Carta> cartas) {
        if (cartas == null || cartas.size() != 5) {
            throw new IllegalArgumentException("Una mano debe tener exactamente 5 cartas");
        }
        this.cartas = new ArrayList<>(cartas);
    }

    public List<Carta> getCartas() { return Collections.unmodifiableList(cartas); }

    /*========================
      EVALUACIÓN DE LA MANO
     ========================*/
    public static class Resultado {
        public final HandCategory categoria;
        public final List<Integer> desempate; // ranks para desempatar (ordenados)
        public final String descripcion;

        public Resultado(HandCategory categoria, List<Integer> desempate, String descripcion) {
            this.categoria = categoria;
            this.desempate = desempate;
            this.descripcion = descripcion;
        }
    }

    
    public boolean royalFlush(List<Carta> cartas2) {
    	//cartas[i] == 1 return 
    	return true;
    }
    
    public Resultado evaluar() {
    	
    	
    	List<String> ordenColor = new ArrayList<>();
    	List<String> ordenValor = new ArrayList<>();
    	ordenValor =ordenarManoValor(this.cartas);
    	ordenColor=ordenarManoColor(this.cartas);
    	
    	 System.out.println("Cartas ordenadas por color: " + ordenColor);
    	 System.out.println("Cartas ordenadas por valor: " + ordenValor);
    	//Escalera real
    	//if(royalFlush())
    	Resultado res = new Resultado(null, null, null);
        return res;
    }

   

    public List<String> ordenarManoColor(List<Carta> cartas) {
        List<Carta> copia = new ArrayList<>(cartas); // copiamos la lista para no modificar la original

        // ordenamos según el palo
        copia.sort(Comparator.comparing(Carta::getPalo));

        // convertimos cada carta a String
        List<String> resultado = new ArrayList<>();
        for (Carta c : copia) {
            resultado.add(c.toString());
        }

        return resultado;
    }

    public List<String> ordenarManoValor(List<Carta> cartas) {
        List<Carta> copia = new ArrayList<>(cartas); // copiamos la lista para no modificar la original

        // ordenamos por valor
        copia.sort(Comparator.comparingInt(Carta::getValorNumerico));

        // convertimos a String
        List<String> resultado = new ArrayList<>();
        for (Carta c : copia) {
            resultado.add(c.toString());
        }

        return resultado;
    }

	/*========================
      DETECCIÓN DE DRAWS
     ========================*/
    public List<String> detectarDraws() {
        List<String> res = new ArrayList<>();
        if (tieneFlushDraw()) res.add("Flush");
        StraightDraw sd = straightDraw();
        if (sd == StraightDraw.OPEN_ENDED) res.add("Straight Open-Ended");
        else if (sd == StraightDraw.GUTSHOT) res.add("Straight Gutshot");
        return res;
    }

    private boolean tieneFlushDraw() {
        Map<Character, Long> cuenta = cartas.stream()
                .collect(Collectors.groupingBy(Carta::getPalo, Collectors.counting()));
        return cuenta.values().stream().anyMatch(c -> c == 4L);
    }

    private enum StraightDraw { NONE, OPEN_ENDED, GUTSHOT }

    private StraightDraw straightDraw() {
        // Conjuntos de valores (acepta A como 14 y 1)
        Set<Integer> vals = cartas.stream().map(Carta::getValorNumerico).collect(Collectors.toSet());
        if (vals.contains(14)) vals.add(1); // A como bajo

        StraightInfo si = straightInfo(cartas);
        if (si.isStraight) return StraightDraw.NONE; // ya es escalera completa

        // Ventanas posibles de 5 consecutivos
        for (int start = 1; start <= 10; start++) {
            int missing = 0;
            boolean missAtEndLow = !vals.contains(start);
            boolean missAtEndHigh = !vals.contains(start + 4);
            for (int r = start; r <= start + 4; r++) {
                if (!vals.contains(r)) missing++;
            }
            if (missing == 1) {
                if (missAtEndLow || missAtEndHigh) return StraightDraw.OPEN_ENDED;
                else return StraightDraw.GUTSHOT;
            }
        }
        return StraightDraw.NONE;
    }

    /*========================
      HELPERS
     ========================*/
    private static class StraightInfo {
        final boolean isStraight;
        final int topRank; // 5 para A2345, 14 para TJQKA, etc.
        StraightInfo(boolean ok, int top) { this.isStraight = ok; this.topRank = top; }
    }

    private static StraightInfo straightInfo(List<Carta> cards) {
        // únicos y ordenados ascendentemente
        Set<Integer> set = cards.stream().map(Carta::getValorNumerico).collect(Collectors.toCollection(TreeSet::new));
        // Caso A bajo: añade 1 si hay As
        if (set.contains(14)) set.add(1);

        List<Integer> vals = new ArrayList<>(set);
        // busca 5 consecutivos
        int run = 1, bestTop = -1;
        for (int i = 1; i < vals.size(); i++) {
            if (vals.get(i) == vals.get(i - 1) + 1) {
                run++;
                if (run >= 5) bestTop = Math.max(bestTop, vals.get(i)); // top rank
            } else {
                run = 1;
            }
        }
        if (bestTop == -1) return new StraightInfo(false, -1);
        // Ajuste: si top=5 y hay A bajo, representa A2345 -> topRank=5
        return new StraightInfo(true, bestTop == 5 ? 5 : bestTop);
    }

    private static List<Carta> cartasOrdenadasDesc(List<Carta> cs) {
        return cs.stream()
                .sorted(Comparator.comparingInt(Carta::getValorNumerico).reversed())
                .collect(Collectors.toList());
        }

    private static List<Integer> kickerExcluyendo(Map<Integer, List<Carta>> porValor, int... excluir) {
        Set<Integer> ex = Arrays.stream(excluir).boxed().collect(Collectors.toSet());
        return porValor.entrySet().stream()
                .filter(e -> !ex.contains(e.getKey()))
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.getKey(), a.getKey());
                    if (cmp != 0) return cmp;
                    return Integer.compare(b.getValue().size(), a.getValue().size());
                })
                .flatMap(e -> Collections.nCopies(e.getValue().size(), e.getKey()).stream())
                .collect(Collectors.toList());
    }

    private static String rangoToTexto(int r) {
        if (r == 14) return "Ace";
        if (r == 13) return "King";
        if (r == 12) return "Queen";
        if (r == 11) return "Jack";
        if (r == 10) return "Ten";
        return String.valueOf(r);
    }

    private static String pluralValor(int r) {
        switch (r) {
            case 14: return "Aces";
            case 13: return "Kings";
            case 12: return "Queens";
            case 11: return "Jacks";
            case 10: return "Tens";
            default: return r + "s";
        }
    }

    /*========================
      PARSER DE 10 CARACTERES
     ========================*/
    public static Mano desdeString10(String linea) {
        if (linea == null || linea.length() != 10) {
            throw new IllegalArgumentException("La línea debe tener exactamente 10 caracteres (5 cartas)");
        }
        List<Carta> cs = new ArrayList<>(5);
        for (int i = 0; i < 10; i += 2) {
            cs.add(new Carta(linea.charAt(i), linea.charAt(i + 1)));
        }
        return new Mano(cs);
    }
}