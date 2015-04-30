package de.jeha.photo.mosaic.core;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author jenshadlich@googlemail.com
 */
public class ColorCalculatorTest {

    @Test
    public void testAverageColor000000() throws IOException {
        ColorCalculator.RGBA result =
                ColorCalculator.averageColor(ImageIO.read(new File("src/test/resources/000000_100x100.png")));

        assertEquals("r", 0.0, result.getR(), 0.1);
        assertEquals("g", 0.0, result.getG(), 0.1);
        assertEquals("b", 0.0, result.getB(), 0.1);
        assertEquals("a", 0.0, result.getA(), 0.1); // no transparency
    }

    @Test
    public void testAverageColorFFFFFF() throws IOException {
        ColorCalculator.RGBA result =
                ColorCalculator.averageColor(ImageIO.read(new File("src/test/resources/FFFFFF_100x100.png")));

        assertEquals("r", 255.0, result.getR(), 0.1);
        assertEquals("g", 255.0, result.getG(), 0.1);
        assertEquals("b", 255.0, result.getB(), 0.1);
        assertEquals("a", 0.0, result.getA(), 0.1); // no transparency
    }

    @Test
    public void testAverageColor00FF00() throws IOException {
        ColorCalculator.RGBA result =
                ColorCalculator.averageColor(ImageIO.read(new File("src/test/resources/00FF00_100x100.png")));

        assertEquals("r", 0.0, result.getR(), 0.1);
        assertEquals("g", 255.0, result.getG(), 0.1);
        assertEquals("b", 0.0, result.getB(), 0.1);
        assertEquals("a", 0.0, result.getA(), 0.1); // no transparency
    }

    @Test
    public void testAverageColor999999() throws IOException {
        ColorCalculator.RGBA result =
                ColorCalculator.averageColor(ImageIO.read(new File("src/test/resources/999999_100x100.png")));

        assertEquals("r", 153.0, result.getR(), 0.1);
        assertEquals("g", 153.0, result.getG(), 0.1);
        assertEquals("b", 153.0, result.getB(), 0.1);
        assertEquals("a", 0.0, result.getA(), 0.1); // no transparency
    }

    @Test
    public void testAverageColorSprdLogo() throws IOException {
        ColorCalculator.RGBA result =
                ColorCalculator.averageColor(ImageIO.read(new File("src/test/resources/sprd_logo_small.jpg")));

        assertEquals("r", 62.3, result.getR(), 0.1);
        assertEquals("g", 193.2, result.getG(), 0.1);
        assertEquals("b", 183.0, result.getB(), 0.1);
        assertEquals("a", 0.0, result.getA(), 0.1); // no transparency
    }
}
