package de.jeha.photo.mosaic;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author jenshadlich@googlemail.com
 */
public class PhotoMosaic {

    private static final int TILE_WIDTH = 40;
    private static final int TILE_HEIGHT = 30;

    public static void main(String... args) throws IOException {
        BufferedImage image = ImageIO.read(new File("src/test/resources/sprd_logo_small.jpg"));

        BufferedImage scaledImage = ImageScaler.scale(image, TILE_WIDTH, TILE_HEIGHT);
        ImageIO.write(scaledImage, "png", new File("target/sprd_logo_small.png"));
    }

}
