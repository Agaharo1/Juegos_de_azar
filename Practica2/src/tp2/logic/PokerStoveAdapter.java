package tp2.logic;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

public class PokerStoveAdapter {

    private static final String PS_EVAL =
        "C:\\pokerstove\\build\\bin\\Release\\ps-eval.exe"; // ajusta si cambias de sitio

    // hands: lista de "AhAd", "KcKd", ... ; board: ["Qs","Jd","2c"] (0..5)
    public static Map<String, Double> tryPsEval(
            List<String> names, List<String> hands, List<String> board) throws IOException, InterruptedException {

        // 1) Monta la línea según la ayuda de tu ps-eval
        //    (ajusta si tu --help indica flags distintos)
        String playersArg = String.join(":", hands);
        String boardArg = String.join("", board);

        List<String> cmd = new ArrayList<>();
        cmd.add(PS_EVAL);
        cmd.add(playersArg);
        if (!board.isEmpty()) {
            cmd.add("--board");
            cmd.add(boardArg);
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        String out;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line; while ((line = br.readLine()) != null) sb.append(line).append('\n');
            out = sb.toString();
        }
        int code = p.waitFor();
        if (code != 0) throw new IOException("ps-eval exit code " + code + "\n" + out);

        // 2) Parseo flexible de porcentajes (captura 0–100 con decimales y %)
        //    Ajusta el patrón si tu salida tiene otro formato.
        Pattern pct = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*%");
        Matcher m = pct.matcher(out);
        List<Double> perc = new ArrayList<>();
        while (m.find()) perc.add(Double.parseDouble(m.group(1)));

        if (perc.size() != names.size())
            throw new IOException("No pude mapear salida a jugadores.\nSalida:\n" + out);

        Map<String, Double> res = new LinkedHashMap<>();
        for (int i = 0; i < names.size(); i++) res.put(names.get(i), perc.get(i));
        return res;
    }

    /** Envoltorio con fallback a tu RealEquityCalculator si ps-eval falla */
    public static Map<String, Double> computeEquityWithFallback(
            List<String> names, List<tp2.model.Hand> hands, List<String> board,
            int trials, long seed) {

        // convierto manos a "AhAd" etc. para ps-eval; si alguna es null, mejor Monte Carlo
        boolean anyUnknown = hands.stream().anyMatch(Objects::isNull);
        if (!anyUnknown && new File(PS_EVAL).exists()) {
            try {
                List<String> handStr = new ArrayList<>();
                for (tp2.model.Hand h : hands) handStr.add(h.card1() + h.card2());
                return tryPsEval(names, handStr, board == null ? List.of() : board);
            } catch (Exception ignored) {
                // cae a MC
            }
        }
        // Fallback Monte Carlo (tu clase actual)
        return new RealEquityCalculator().calcularEquity(names, hands, board, trials, seed);
    }
}
