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
    


    
    
    public Resultado evaluar() {
        // Ordenamos una vez y lo usamos en todo
        List<Carta> cartasOrdenadasValor = new ArrayList<>(cartas);
        cartasOrdenadasValor.sort(Comparator.comparingInt(Carta::getValorNumerico));
        List<Carta> cartasOrdenadasColor = new ArrayList<>(cartas);
        cartasOrdenadasColor.sort(Comparator.comparing(Carta::getPalo));

        // Prints de debug (puedes quitarlos luego)
        System.out.print("Cartas ordenadas por valor: ");
        for (Carta c : cartasOrdenadasValor) System.out.print(c + " ");
        System.out.println();

        System.out.print("Cartas ordenadas por color: ");
        for (Carta c : cartasOrdenadasColor) System.out.print(c + " ");
        System.out.println();

        // Ahora comprobamos manos de mejor a peor
        if (esEscaleraReal(cartasOrdenadasValor)) {
            return new Resultado(
                HandCategory.ROYAL_FLUSH,
                Collections.emptyList(),
                "Royal Flush (10-J-Q-K-A del mismo palo)"
            );
        }

        if (esEscaleraDeColor(cartasOrdenadasValor)) {
            return new Resultado(
                HandCategory.STRAIGHT_FLUSH,
                Collections.emptyList(),
                "Straight Flush (cinco consecutivas del mismo palo)"
            );
        }

        // Aquí seguiríamos con Four of a Kind, Full House, Flush, etc.
        return new Resultado(
            HandCategory.HIGH_CARD,
            Collections.emptyList(),
            "High Card"
        );
    }
    
    private boolean mismoPalo(List<Carta> cs) {
        char p = cs.get(0).getPalo();
        for (int i = 1; i < cs.size(); i++) {
            if (cs.get(i).getPalo() != p) return false;
        }
        return true;
    }

    

    // 3) ¿son 5 consecutivas? Soporta A-bajo (A=14 vale como 1)
    private boolean esSecuencia5Asc(List<Carta> ord) {
        // Caso especial A-2-3-4-5: si hay As (14), intentamos tratarlo como 1
        boolean tieneAs = false;
        for (Carta c: ord) if (c.getValorNumerico() == 14) { tieneAs = true; break; }

        // Intento normal (A alto)
        boolean consecutiva = true;
        for (int i = 1; i < ord.size(); i++) {
            int prev = ord.get(i-1).getValorNumerico();
            int curr = ord.get(i).getValorNumerico();
            if (curr != prev + 1) { consecutiva = false; break; }
        }
        if (consecutiva) return true;

        // Intento A-bajo: mapeamos As=14 -> 1 y re-checamos
        if (tieneAs) {
            int[] vals = new int[ord.size()];
            for (int i = 0; i < ord.size(); i++) {
                int v = ord.get(i).getValorNumerico();
                vals[i] = (v == 14) ? 1 : v;
            }
            // ordenamos ese array de 5 valores
            for (int i = 0; i < vals.length; i++) {
                for (int j = 0; j < vals.length - 1; j++) {
                    if (vals[j] > vals[j+1]) {
                        int t = vals[j]; vals[j] = vals[j+1]; vals[j+1] = t;
                    }
                }
            }
            boolean ok = true;
            for (int i = 1; i < vals.length; i++) {
                if (vals[i] != vals[i-1] + 1) { ok = false; break; }
            }
            return ok;
        }
        return false;
    }
    
    public boolean tieneGrupoIguales(List<Carta> ordenadas, int x) {
        int contador = 1;
        for (int i = 1; i < ordenadas.size(); i++) {
            if (ordenadas.get(i).getValorNumerico() == ordenadas.get(i - 1).getValorNumerico()) {
                contador++;
                if (contador == x) return true;
            } else {
                contador = 1;
            }
        }
        return false;
    }

    public boolean esEscaleraReal(List<Carta> cs) {
        if (!mismoPalo(cs)) return false;
        int[] target = {10, 11, 12, 13, 14};
        for (int i = 0; i < 5; i++) {
            if (cs.get(i).getValorNumerico() != target[i]) return false;
        }
        return true;
    }

    public boolean esEscaleraDeColor(List<Carta> cs) {
        if (!mismoPalo(cs)) return false;
        return esSecuencia5Asc(cs);
    }
    
    public boolean esPoker(List<Carta> cs) {
    	if(tieneGrupoIguales(cs, 4)) return true;
    	else return false;
    }
    
    private int[] contarFrecuencias(List<Carta> ordenadas) {
        int[] frecuencias = new int[15];
        for (Carta c : ordenadas) {
            int v = c.getValorNumerico();
            frecuencias[v]++;
        }
        return frecuencias;
    }
    
    public boolean esFullHouse(List<Carta> cs) {
        int[] freq = contarFrecuencias(cs);
        boolean tieneTrio = false;
        boolean tienePareja = false;

        for (int v = 2; v <= 14; v++) {
            if (freq[v] == 3) tieneTrio = true;
            else if (freq[v] == 2) tienePareja = true;
        }

        return tieneTrio && tienePareja;
    }
    
    public boolean esColor(List<Carta> cs) {
        if(mismoPalo(cs)) return true;
        else return false;
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