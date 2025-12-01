package p3.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adaptador para el ejecutable externo "ps-eval" de PokerStove.
 *
 * - Ejecuta ps-eval.exe con manos y board.
 * - Parsea los porcentajes de equity de la salida estándar.
 * - Si el ejecutable no existe o falla → usa RealEquityCalculator como fallback.
 *
 * Requiere ajustar la ruta de PS_EVAL según instalación local.
 */
public final class PokerStoveAdapter {

    /** Ruta del ejecutable ps-eval.exe */
    private static final String PS_EVAL =
            "C:\\pokerstove\\build\\bin\\Release\\ps-eval.exe";

    private PokerStoveAdapter() {}

    /* ========================================================
     *                    LLAMADA PRINCIPAL
     * ======================================================== */

    /**
     * Invoca ps-eval con:
     *  - names: nombres de jugadores
     *  - hands: manos tipo "AhAd", "KcKd", ...
     *  - board: lista con 0 a 5 cartas
     *
     * Devuelve mapa name → equity en %
     */
    public static Map<String, Double> tryPsEval(
            List<String> names,
            List<String> hands,
            List<String> board
    ) throws IOException, InterruptedException {

        // Construcción argumentos
        String playersArg = String.join(":", hands);
        String boardArg = String.join("", board);

        List<String> cmd = new ArrayList<>();
        cmd.add(PS_EVAL);
        cmd.add(playersArg);

        if (!board.isEmpty()) {
            cmd.add("--board");
            cmd.add(boardArg);
        }

        // Lanzar proceso
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process p = pb.start();

        String output = readProcessOutput(p);
        int exit = p.waitFor();

        if (exit != 0)
            throw new IOException("ps-eval exit=" + exit + "\n" + output);

        // Parsear equitys
        List<Double> values = parsePercents(output);

        if (values.size() != names.size())
            throw new IOException("La salida de ps-eval no coincide con nº de jugadores.\n" + output);

        Map<String, Double> result = new LinkedHashMap<>();
        for (int i = 0; i < names.size(); i++)
            result.put(names.get(i), values.get(i));

        return result;
    }


    /* ========================================================
     *                    FALLBACK AUTOMÁTICO
     * ======================================================== */

    /**
     * Intenta ps-eval. Si falla o no existe:
     *  → usa RealEquityCalculator (Monte Carlo interno).
     */
    public static Map<String, Double> computeEquityWithFallback(
            List<String> names,
            List<p3.model.Hand> hands,
            List<String> board,
            int trials,
            long seed
    ) {

        // Si hay manos desconocidas, no usar ps-eval
        boolean hasNullHand = hands.stream().anyMatch(Objects::isNull);

        if (!hasNullHand && new File(PS_EVAL).exists()) {
            try {
                List<String> handStr = new ArrayList<>();
                for (p3.model.Hand h : hands)
                    handStr.add(h.card1() + h.card2());

                return tryPsEval(
                        names,
                        handStr,
                        (board == null ? List.of() : board)
                );

            } catch (Exception ignored) {
                // Si falla → usa Monte Carlo
            }
        }

        return new RealEquityCalculator()
                .calcularEquity(names, hands, board, trials, seed);
    }


    /* ========================================================
     *                    MÉTODOS AUXILIARES
     * ======================================================== */

    /** Lee output completo del proceso. */
    private static String readProcessOutput(Process p) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null)
                sb.append(line).append('\n');
        }

        return sb.toString();
    }

    /** Extrae números tipo "23.5%" o "12%" en orden. */
    private static List<Double> parsePercents(String text) {
        Pattern pct = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*%");
        Matcher m = pct.matcher(text);

        List<Double> values = new ArrayList<>();
        while (m.find())
            values.add(Double.parseDouble(m.group(1)));

        return values;
    }
}
