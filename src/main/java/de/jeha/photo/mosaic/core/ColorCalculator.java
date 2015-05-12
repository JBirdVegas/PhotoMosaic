package de.jeha.photo.mosaic.core;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

/**
 * @author jenshadlich@googlemail.com
 */
public class ColorCalculator {

    /**
     * Calculates the average color (r,g,b,a) values for a given image.
     *
     * @param image input image
     * @return average r,g,b,a values
     */
    public static RGBA averageColor(BufferedImage image) {
        final Raster raster = image.getRaster();
        double array[] = new double[4];
        double r = .0;
        double g = .0;
        double b = .0;
        double a = .0;

        for (int x = 0; x < raster.getWidth(); x++) {
            for (int y = 0; y < raster.getHeight(); y++) {
                raster.getPixel(x, y, array);
                r += array[0];
                g += array[1];
                b += array[2];
                a += array[3];
            }
        }

        final double total = raster.getHeight() * raster.getWidth();
        r /= total;
        g /= total;
        b /= total;
        a /= total;

        return new RGBA(r, g, b, a);
    }

    /**
     * Color representation in RGB color space plus alpha (Red Green Blue Alpha). Stores the values as double
     * (java.awt.Color uses int instead).
     */
    public static class RGBA {

        private final Double r;
        private final Double g;
        private final Double b;
        private final Double a;
        private final Color color;
        private final String hexString;

        public RGBA(double r, double g, double b, double a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.color = new Color((int) r, (int) g, (int) b, (int) a);
            this.hexString = String.format("#%02X%02X%02X", (int) r, (int) g, (int) b);
        }

        public double getR() {
            return r;
        }

        public double getG() {
            return g;
        }

        public double getB() {
            return b;
        }

        public double getA() {
            return a;
        }

        public Color asColor() {
            return color;
        }

        public String asHexString() {
            return hexString;
        }

    }
}
