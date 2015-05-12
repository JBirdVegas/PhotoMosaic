package de.jeha.photo.mosaic.core;

import java.awt.*;

/**
 * Color representation in RGB color space plus alpha (Red Green Blue Alpha). Stores the values as double
 * (java.awt.Color uses int instead).
 */
public class RGBA {

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
