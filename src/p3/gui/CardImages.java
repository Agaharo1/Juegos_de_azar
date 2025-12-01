package p3.gui;

import java.awt.Image;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ImageIcon;

/**
 * Gestiona la carga y caché de imágenes de cartas.
 * Recibe códigos como "Ah" y devuelve la imagen correspondiente
 * desde /resources/cartas/.
 */
public final class CardImages {

    /** Carpeta donde se guardan las imágenes dentro del jar / proyecto. */
    private static final String CARD_PATH = "/cartas/";

    /** Caché thread-safe para evitar recargar imágenes. */
    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();

    /** Imagen por defecto si no se encuentra la carta. */
    private static final Image DEFAULT_IMAGE = loadDefaultImage();

    private CardImages() {}

    /**
     * Devuelve la imagen asociada al código (ej: "Ah").
     * Si no existe, devuelve una imagen por defecto y escribe un aviso.
     */
    public static Image get(String code) {
        return CACHE.computeIfAbsent(code, CardImages::loadImage);
    }

    // -------------------- METODOS PRIVADOS --------------------

    /** Carga la imagen de carta desde recursos. */
    private static Image loadImage(String code) {
        URL url = CardImages.class.getResource(CARD_PATH + code + ".png");

        if (url == null) {
            System.err.println("⚠ No se encontró imagen para carta: " + code);
            return DEFAULT_IMAGE;
        }

        return new ImageIcon(url).getImage();
    }

    /** Carga una imagen por defecto (joker) si existe. */
    private static Image loadDefaultImage() {
        URL url = CardImages.class.getResource(CARD_PATH + "red_joker.png");

        if (url == null) {
            System.err.println("⚠ No se encontró imagen default (joker). Se devolverá null.");
            return null; // fallback extremo: no hay imagen de respaldo
        }

        return new ImageIcon(url).getImage();
    }
}
