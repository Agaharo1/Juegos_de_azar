package tp2.model;

import java.util.regex.Pattern;

final class CardValidator {
    // RANGO: 2-9,T,J,Q,K,A — PALO: h,d,c,s
    private static final Pattern CODE = Pattern.compile("^[2-9TJQKA][hdcs]$");

    private CardValidator() {}

    static boolean isValidCode(String code) {
        return code != null && CODE.matcher(code).matches();
    }
}
