package de.jeha.photo.mosaic;

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
    public void testAverageColor() throws IOException {
        ColorCalculator.RGBA result =
                ColorCalculator.averageColor(ImageIO.read(new File("src/test/resources/sprd_logo_small.jpg")));

        assertEquals(62.3, result.getR(), 0.1);
        assertEquals(193.2, result.getG(), 0.1);
        assertEquals(183.0, result.getB(), 0.1);
        assertEquals(0.0, result.getA(), 0.1); // no transparency
    }
}
