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
import java.util.HashMap;
import java.util.Map;

/**
 * @author jenshadlich@googlemail.com
 */
public class PhotoMosaic {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoMosaic.class);

    private static final int TILE_WIDTH = 40;
    private static final int TILE_HEIGHT = 30;

    // TODO: collect cmd line arguments with args4j
    private final String targetImageFilename;
    private final String inputDirectory = "/Users/jns/Pictures";
    private final boolean recursive = false;
    private final int tileWidth;
    private final int tileHeight;

    private final Map<String, Tile> tileMap = new HashMap<>();

    public PhotoMosaic(String targetImageFilename, int tileWidth, int tileHeight) {
        this.targetImageFilename = targetImageFilename;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }

    public static void main(String... args) throws IOException {
        System.setProperty("java.awt.headless", "true");

        final String targetImageFilename = "src/test/resources/IMG_20150320_142153.jpg";
        //final String targetImageFilename = "src/test/resources/sprd_logo_small.jpg";

        new PhotoMosaic(targetImageFilename, TILE_WIDTH, TILE_HEIGHT).create();
    }

    private void create() throws IOException {
        BufferedImage source = ImageIO.read(new File(targetImageFilename));
        LOG.info("source image h={}, w={}", source.getHeight(), source.getWidth());

        processInputDirectory();

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

                // find an image that matches the average color best
                double shortestDistance = Double.MAX_VALUE;
                String shortestDistanceTileKey = null;
                Color needle = averageColor.toColor();
                for (Map.Entry<String, Tile> entry : tileMap.entrySet()) {
                    double distance = colorDistance(needle, entry.getValue().getAverageColor());
                    if (distance < shortestDistance) {
                        shortestDistance = distance;
                        shortestDistanceTileKey = entry.getKey();
                    }
                }
                
                // write the selected tile into the target image
                if (shortestDistanceTileKey != null) {
                    BufferedImage selectedTile = tileMap.get(shortestDistanceTileKey).getScaledImage();
                    double array[] = new double[4];
                    for (int i = x, k = 0; i < x + tileWidth; i++, k++) {
                        for (int j = y, l = 0; j < y + tileHeight; j++, l++) {
                            selectedTile.getRaster().getPixel(k, l, array);
                            writableRaster.setPixel(i, j, array);
                        }
                    }
                }

                /*
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
                */
            }
        }

        //LOG.debug("Writing rasterized target image");
        //ImageIO.write(target, "png", new File("target/target_debug_rasterized.png"));

        ImageIO.write(target, "png", new File("target/target.png"));
    }

    private void processInputDirectory() throws IOException {
        final File root = new File(inputDirectory);
        if (root.exists() && root.canRead()) {
            String[] filenames = root.list();
            if (filenames != null) {
                for (String filename : filenames) {
                    // TODO: refactor supported file extensions
                    if (filename.endsWith(".png") || filename.endsWith(".jpg")) {
                        BufferedImage image = ImageIO.read(new File(root.getAbsolutePath() + "/" + filename));
                        BufferedImage scaledImage = ImageScaler.scale(image, tileWidth, tileHeight);
                        ColorCalculator.RGBA rgba = ColorCalculator.averageColor(scaledImage);
                        tileMap.put(filename, new Tile(scaledImage, rgba.toColor()));
                    } else {
                        LOG.info("Ignore '{}' as input file: unsupported file extension", filename);
                    }
                }
                LOG.info("added {} images as tiles", tileMap.size());
            }
        } else {
            LOG.error("can't access directory '{}'", inputDirectory);
        }

    }

    private BufferedImage copyImage(BufferedImage image) {
        final ColorModel cm = image.getColorModel();
        WritableRaster raster = image.copyData(null);
        return new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
    }

    /**
     * Color distance as described by http://www.compuphase.com/cmetric.htm.
     */
    private double colorDistance(Color c1, Color c2) {
        int red1 = c1.getRed();
        int red2 = c2.getRed();
        int redMean = (red1 + red2) >> 1;
        int r = red1 - red2;
        int g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        return Math.sqrt((((512 + redMean) * r * r) >> 8) + 4 * g * g + (((767 - redMean) * b * b) >> 8));
    }

}
