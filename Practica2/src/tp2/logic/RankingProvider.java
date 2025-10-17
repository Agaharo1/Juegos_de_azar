package tp2.logic;

import java.util.*;

public class RankingProvider {
    private static final List<String> ranking = Arrays.asList(
        "AA","KK","QQ","JJ","AKs","AQs","AJs","KQs",
        "TT","99","88","AKo","AQo","AJo","KQo"
    );
    public static int getPosicion(String mano){
        int idx = ranking.indexOf(mano);
        return (idx == -1) ? 999 : idx + 1;
    }
    public static List<String> getRanking(){ return new ArrayList<>(ranking); }
}
