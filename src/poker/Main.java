package poker;

public class Main {
    public static void main(String[] args) {
        Mano m = Mano.desdeString10("Ah2d7hJd3h");
        Mano.Resultado r = m.evaluar();

        System.out.println("Best hand: " + r.descripcion);
        for (String d : m.detectarDraws()) {
            System.out.println("Draw: " + d);
        }
    }
}