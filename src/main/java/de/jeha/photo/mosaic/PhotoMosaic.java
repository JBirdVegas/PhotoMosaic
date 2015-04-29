package de.jeha.photo.mosaic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
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

        BufferedImage source = ImageIO.read(new File("src/test/resources/IMG_20150320_142153.jpg"));
        //BufferedImage source = ImageIO.read(new File("src/test/resources/sprd_logo_small.jpg"));
        LOG.info("source image h={}, w={}", source.getHeight(), source.getWidth());

        // TODO: process the input directory to collect tiles and their average colors for the target image

        final int correctedWidth = (source.getWidth() / tileWidth) * tileWidth;
        final int correctedHeight = (source.getHeight() / tileHeight) * tileHeight;
        LOG.info("target image h={}, w={}", correctedHeight, correctedWidth);

        BufferedImage target = copyImage(source).getSubimage(0, 0, correctedWidth, correctedHeight);

        for (int x = 0; x < source.getWidth() - tileWidth; x += tileWidth) {
            for (int y = 0; y < source.getHeight() - tileHeight; y += tileHeight) {
                LOG.trace("tile x={}, y={}", x, y);
                BufferedImage tile = source.getSubimage(x, y, tileWidth, tileHeight);
                ColorCalculator.RGBA averageColor = ColorCalculator.averageColor(tile);
                LOG.trace("tile average color r={}, g={}, b={}",
                        (int) averageColor.getR(), (int) averageColor.getG(), (int) averageColor.getB());
                WritableRaster writableRaster = target.getWritableTile(x, y);

                // TODO: find an image that matches the average color best

                // just write the average color (= rasterization) for debug
                double array[] = new double[4];
                array[0] = averageColor.getR();
                array[1] = averageColor.getG();
                array[2] = averageColor.getB();
                array[3] = 0.0;

                for (int i = x; i < x + tileWidth; i++) {
                    for (int j = y; j < y + tileHeight; j++) {
                        writableRaster.setPixel(i, j, array);
                    }
                }
            }
        }

        LOG.debug("Writing rasterized target image");
        ImageIO.write(target, "png", new File("target/target_debug_rasterized.png"));
    }

    private static BufferedImage copyImage(BufferedImage image) {
        final ColorModel cm = image.getColorModel();
        WritableRaster raster = image.copyData(null);
        return new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
    }

    /**
     * Color distance as described by http://www.compuphase.com/cmetric.htm.
     */
    private static double colorDistance(Color c1, Color c2) {
        int red1 = c1.getRed();
        int red2 = c2.getRed();
        int redMean = (red1 + red2) >> 1;
        int r = red1 - red2;
        int g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        return Math.sqrt((((512 + redMean) * r * r) >> 8) + 4 * g * g + (((767 - redMean) * b * b) >> 8));
    }

}
