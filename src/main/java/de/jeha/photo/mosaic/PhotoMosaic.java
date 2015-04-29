package de.jeha.photo.mosaic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author jenshadlich@googlemail.com
 */
public class PhotoMosaic {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoMosaic.class);

    private static final int TILE_WIDTH = 40;
    private static final int TILE_HEIGHT = 30;

    // TODO: collect cmd line arguments with args4j
    private String targetFile;
    private String inputDirectory;
    private boolean recursive = false;

    public static void main(String... args) throws IOException {
        System.setProperty("java.awt.headless", "true");

        final int tileWidth = TILE_WIDTH;
        final int tileHeight = TILE_HEIGHT;

        //BufferedImage image = ImageIO.read(new File("src/test/resources/IMG_20150320_142153.jpg"));
        BufferedImage image = ImageIO.read(new File("src/test/resources/sprd_logo_small.jpg"));
        LOG.info("h={}, w={}", image.getHeight(), image.getWidth());

        for (int x = 0; x < image.getWidth() - tileWidth; x += tileWidth) {
            for (int y = 0; y < image.getHeight() - tileHeight; y += tileHeight) {
                LOG.debug("tile x={}, y={}", x, y);
                BufferedImage tile = image.getSubimage(x, y, tileWidth, tileHeight);
                ColorCalculator.RGBA averageColor = ColorCalculator.averageColor(tile);
                LOG.debug("tile average color r={}, g={}, b={}",
                        (int) averageColor.getR(), (int) averageColor.getG(), (int) averageColor.getB());
            }
        }
    }

}
