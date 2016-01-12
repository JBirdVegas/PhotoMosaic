package de.jeha.photo.mosaic.core;

import java.awt.*;

/**
 * Color distance as described by http://www.compuphase.com/cmetric.htm.
 *
 * @author jenshadlich@googlemail.com
 */
public class ColorDistance {

    /**
     * @param c1 The first color
     * @param c2 The other color to calculate the distance between the first color
     * @return distance
     */
    public static double calculate(Color c1, Color c2) {
        int red1 = c1.getRed();
        int red2 = c2.getRed();
        int redMean = (red1 + red2) >> 1;
        int r = red1 - red2;
        int g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        return Math.sqrt((((512 + redMean) * r * r) >> 8) + 4 * g * g + (((767 - redMean) * b * b) >> 8));
    }

}
