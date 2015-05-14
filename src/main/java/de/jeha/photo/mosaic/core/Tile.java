package de.jeha.photo.mosaic.core;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author jenshadlich@googlemail.com
 */
public class Tile {

    private final BufferedImage image;
    private final Color averageColor;

    public Tile(BufferedImage image, Color averageColor) {
        this.image = image;
        this.averageColor = averageColor;
    }

    public BufferedImage getImage() {
        return image;
    }

    public Color getAverageColor() {
        return averageColor;
    }
}
