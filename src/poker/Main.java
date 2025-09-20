package poker;

public class Main {
    public static void main(String[] args) {
        Mano m = Mano.desdeString10("AhKhQhJhTh");
        Mano.Resultado r = m.evaluar();
        System.out.println("Best hand: " + r.descripcion);
        for (String d : m.detectarDraws()) {
            System.out.println("Draw: " + d);
        }
    }
}