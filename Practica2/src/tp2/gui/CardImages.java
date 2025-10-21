package tp2.gui;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public final class CardImages {
    private static final Map<String, Image> cache = new HashMap<>();

    private CardImages() {}

    /** Devuelve la imagen asociada al código de carta (ej: "Ah", "Kd"). */
    public static Image get(String code) {
        return cache.computeIfAbsent(code, k -> {
            var url = CardImages.class.getResource("/cartas/" + k + ".png");
            if (url == null) return null;
            return new ImageIcon(url).getImage();
        });
    }
}
