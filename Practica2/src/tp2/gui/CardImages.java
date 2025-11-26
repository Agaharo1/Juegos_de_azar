package tp2.gui;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Esta clase se encarga de cargar y guardar las imágenes de las cartas.
 * 
 * Cuando se le pide una carta (por ejemplo "Ah" para As de corazones),
 * busca su imagen en la carpeta "resources/cartas/" del proyecto y la devuelve.
 * 
 * Usa una "caché" (una especie de memoria) para no volver a cargar la misma imagen
 * muchas veces, lo que hace que el programa sea más rápido.
 */
public final class CardImages {

    // Aquí se guardan las imágenes ya cargadas.
    // La clave es el nombre de la carta (como "Ah") y el valor es la imagen.
    private static final Map<String, Image> cache = new HashMap<>();

    // Constructor privado para que nadie cree objetos de esta clase.
    // Solo se usa con sus métodos "static" (de clase).
    private CardImages() {}

    /**
     * Este método devuelve la imagen correspondiente al código de carta que se le pasa.
     * Ejemplo: si pides "Ah", devuelve la imagen del As de corazones.
     * 
     * Si la imagen ya se había cargado antes, la saca de la memoria (caché).
     * Si no, la busca en la carpeta de recursos y la guarda en la caché para la próxima vez.
     */
    public static Image get(String code) {
        // computeIfAbsent: si la carta no está en la caché, la carga y la guarda.
        return cache.computeIfAbsent(code, k -> {
            // Busca la imagen dentro del proyecto (en /resources/cartas/)
            var url = CardImages.class.getResource("/cartas/" + k + ".png");

            // Si no se encuentra la imagen, devuelve null.
            if (url == null) {
                System.err.println("No se encontró imagen para: " + k);
            }


            // Si se encuentra, la convierte a un objeto de tipo Image.
            return new ImageIcon(url).getImage();
        });
    }
}
