package de.jeha.photo.mosaic;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author jenshadlich@googlemail.com
 */
public class ImageScaler {

    public static BufferedImage scale(BufferedImage input, int width, int height) {
        final Image scaled = input.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        final BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics g = output.getGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();

        return output;
    }
}
