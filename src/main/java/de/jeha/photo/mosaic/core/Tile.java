package de.jeha.photo.mosaic.core;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author jenshadlich@googlemail.com
 */
public class Tile {

    private final BufferedImage scaledImage;
    private final Color averageColor;

    public Tile(BufferedImage scaledImage, Color averageColor) {
        this.scaledImage = scaledImage;
        this.averageColor = averageColor;
    }

    public BufferedImage getScaledImage() {
        return scaledImage;
    }

    public Color getAverageColor() {
        return averageColor;
    }
}
