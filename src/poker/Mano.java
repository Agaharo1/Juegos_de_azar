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
        List<Carta> ordenadas = new ArrayList<>(cartas);
        ordenadas.sort(Comparator.comparingInt(Carta::getValorNumerico));

        if (esEscaleraReal(ordenadas)) {
            return new Resultado(HandCategory.ROYAL_FLUSH, Collections.emptyList(),
                    "Royal Flush");
        }
        
        if (esEscaleraDeColor(ordenadas)) {
            int high = ordenadas.get(4).getValorNumerico(); // la última es la más alta
            return new Resultado(HandCategory.STRAIGHT_FLUSH,
                    Arrays.asList(high), "Straight Flush");
        }
        
        if (esPoker(ordenadas)) {
            int[] freq = contarFrecuencias(ordenadas);
            int valorPoker = 0, kicker = 0;
            for (int v = 2; v <= 14; v++) {
                if (freq[v] == 4) valorPoker = v;
                if (freq[v] == 1) kicker = v;
            }
            return new Resultado(HandCategory.FOUR_OF_A_KIND,
                    Arrays.asList(valorPoker, kicker), "Four of a Kind");
        }
        
        if (esFullHouse(ordenadas)) {
            int[] freq = contarFrecuencias(ordenadas);
            int trio = 0, par = 0;
            for (int v = 2; v <= 14; v++) {
                if (freq[v] == 3) trio = v;
                if (freq[v] == 2) par = v;
            }
            return new Resultado(HandCategory.FULL_HOUSE,
                    Arrays.asList(trio, par), "Full House");
        }
        
        if (esColor(ordenadas)) {
            return new Resultado(HandCategory.FLUSH,
                    desempateHighCard(ordenadas), "Flush");
        }
        
        if (esEscalera(ordenadas)) {
            int high = ordenadas.get(4).getValorNumerico();
            // caso especial A-2-3-4-5 → high=5
            if (ordenadas.get(4).getValorNumerico() == 14 &&
                ordenadas.get(0).getValorNumerico() == 2) {
                high = 5;
            }
            return new Resultado(HandCategory.STRAIGHT,
                    Arrays.asList(high), "Straight");
        }
        
        if (esTrio(ordenadas)) {
            int[] freq = contarFrecuencias(ordenadas);
            int trio = 0;
            List<Integer> kickers = new ArrayList<>();
            for (int v = 14; v >= 2; v--) {
                if (freq[v] == 3) trio = v;
                else if (freq[v] == 1) kickers.add(v);
            }
            List<Integer> d = new ArrayList<>();
            d.add(trio);
            d.addAll(kickers);
            return new Resultado(HandCategory.THREE_OF_A_KIND, d, "Three of a Kind");
        }
        
        if (esDoblePareja(ordenadas)) {
            int[] freq = contarFrecuencias(ordenadas);
            List<Integer> pares = new ArrayList<>();
            int kicker = 0;
            for (int v = 14; v >= 2; v--) {
                if (freq[v] == 2) pares.add(v);
                else if (freq[v] == 1) kicker = v;
            }
            List<Integer> d = new ArrayList<>();
            d.addAll(pares); // ya están en orden descendente
            d.add(kicker);
            return new Resultado(HandCategory.TWO_PAIR, d, "Two Pair");
        }
        
        if (esPareja(ordenadas)) {
            int[] freq = contarFrecuencias(ordenadas);
            int par = 0;
            List<Integer> kickers = new ArrayList<>();
            for (int v = 14; v >= 2; v--) {
                if (freq[v] == 2) par = v;
                else if (freq[v] == 1) kickers.add(v);
            }
            List<Integer> d = new ArrayList<>();
            d.add(par);
            d.addAll(kickers);
            return new Resultado(HandCategory.ONE_PAIR, d, "One Pair");
        }

        return new Resultado(HandCategory.HIGH_CARD,
                desempateHighCard(ordenadas), "High Card");
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
    	return tieneGrupoIguales(cs, 4);
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
        return mismoPalo(cs);
    }
    
    public boolean esEscalera(List<Carta> cs) {
    	return esSecuencia5Asc(cs);
    }
    
    public boolean esTrio(List<Carta> cs) {
    	return tieneGrupoIguales(cs, 3);
    }
    
    public boolean esDoblePareja(List<Carta> ordenadas) {
        int[] freq = contarFrecuencias(ordenadas);
        int parejas = 0;
        for (int v = 2; v <= 14; v++) {
            if (freq[v] == 2) {
                parejas++;
            }
        }
        return parejas == 2;
    }
    
    public boolean esPareja(List<Carta> cs) {
    	return tieneGrupoIguales(cs, 2);
    }
    
    private enum StraightDraw { NONE, OPEN_ENDED, GUTSHOT }

    // Método público que devuelve una lista de strings con los draws detectados
    
    public List<String> detectarDraws() {
        List<String> out = new ArrayList<>();
        List<Carta> ord = new ArrayList<>(cartas);
        ord.sort(Comparator.comparingInt(Carta::getValorNumerico));

        boolean royal   = esEscaleraReal(ord);
        boolean sf      = esEscaleraDeColor(ord);
        boolean str     = esEscalera(ord);
        boolean flush   = esColor(cartas);

        if (!royal && !sf && !flush) {
            if (tieneFlushDraw(cartas)) out.add("Flush");
        }

        if (!royal && !sf && !str) {
            int unicos = valoresUnicosAsc(ord).length;

            boolean esPareja = tieneGrupoIguales(ord, 2);
            boolean esTrio   = tieneGrupoIguales(ord, 3);
            boolean esPoker  = tieneGrupoIguales(ord, 4);

            boolean esDoblePareja = false;
            if (unicos == 3 && !esTrio) esDoblePareja = true;

            boolean esHighCard = (unicos == 5);
            boolean esOnePair  = esPareja && !esTrio && !esPoker && !esDoblePareja;

            if (esHighCard || esOnePair) {
                StraightDraw sd = straightDraw(ord);
                if (sd == StraightDraw.OPEN_ENDED) out.add("Straight Open-Ended");
                else if (sd == StraightDraw.GUTSHOT) out.add("Straight Gutshot");
            }
        }

        return out;
    }

    private StraightDraw straightDraw(List<Carta> ordAscPorValor) {
        if (esSecuencia5Asc(ordAscPorValor)) return StraightDraw.NONE;

        int[] base = valoresUnicosAsc(ordAscPorValor);
        StraightDraw d = straightDrawSobreValores(base);
        if (d != StraightDraw.NONE) return d;

        boolean tieneAs = false;
        for (int v : base) if (v == 14) { tieneAs = true; break; }
        if (!tieneAs) return StraightDraw.NONE;

        int[] alt = new int[base.length];
        for (int i = 0; i < base.length; i++) alt[i] = (base[i] == 14) ? 1 : base[i];

        for (int i = 0; i < alt.length; i++)
            for (int j = 0; j < alt.length - 1; j++)
                if (alt[j] > alt[j+1]) { int t=alt[j]; alt[j]=alt[j+1]; alt[j+1]=t; }

        return straightDrawSobreValores(alt);
    }


    private StraightDraw straightDrawSobreValores(int[] vals) {
        for (int start = 1; start <= 10; start++) {
            boolean[] present = new boolean[5];
            int dentro = 0;
            for (int v : vals) {
                if (v >= start && v <= start + 4) {
                    present[v - start] = true;
                    dentro++;
                }
            }
            if (dentro == 4) {
                int miss = -1;
                for (int i = 0; i < 5; i++) if (!present[i]) { miss = i; break; }
                return (miss == 0 || miss == 4) ? StraightDraw.OPEN_ENDED : StraightDraw.GUTSHOT;
            }
        }
        return StraightDraw.NONE;
    }

    private int[] valoresUnicosAsc(List<Carta> ord) {
        int[] tmp = new int[5];
        int n = 0;
        int last = -999;
        for (Carta c : ord) {
            int v = c.getValorNumerico();
            if (n == 0 || v != last) {
                tmp[n++] = v;
                last = v;
            }
        }
        int[] res = new int[n];
        for (int i = 0; i < n; i++) res[i] = tmp[i];
        return res;
    }

    private boolean tieneFlushDraw(List<Carta> cs) {
        int h=0,d=0,c=0,s=0;
        for (Carta x: cs) {
            switch (x.getPalo()) {
                case 'h': h++; break;
                case 'd': d++; break;
                case 'c': c++; break;
                case 's': s++; break;
            }
        }
        return (h==4 || d==4 || c==4 || s==4);
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
    
    private List<Integer> desempateHighCard(List<Carta> ordenadas) {
        List<Integer> out = new ArrayList<>();
        for (int i = ordenadas.size()-1; i >= 0; i--) {
            out.add(ordenadas.get(i).getValorNumerico());
        }
        return out;
    }

}