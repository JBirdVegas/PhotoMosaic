package de.jeha.photo.mosaic;

import de.jeha.photo.mosaic.core.ColorCalculator;
import de.jeha.photo.mosaic.core.ImageScaler;
import de.jeha.photo.mosaic.core.RGBA;
import de.jeha.photo.mosaic.core.Tile;
import org.apache.commons.io.FilenameUtils;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author jenshadlich@googlemail.com
 */
public class PhotoMosaic {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoMosaic.class);
    private static final String FILE_FORMAT_PNG = "png";
    private static final String FILE_FORMAT_JPG = "jpg";
    private static final String OUTPUT_FILE_FORMAT = FILE_FORMAT_PNG;
    private static final Set<String> SUPPORTED_INPUT_FILE_FORMATS;

    static {
        SUPPORTED_INPUT_FILE_FORMATS = new HashSet<>();
        SUPPORTED_INPUT_FILE_FORMATS.add(FILE_FORMAT_PNG);
        SUPPORTED_INPUT_FILE_FORMATS.add(FILE_FORMAT_JPG);
    }

    private final String targetFilename;
    private final String inputDirectory;
    private final String outputFilename;
    private final boolean recursive = false;
    private final int tileWidth;
    private final int tileHeight;

    private final Map<String, Tile> tileMap = new HashMap<>();

    public PhotoMosaic(String targetFilename, String inputDirectory, String outputFilename, int tileWidth,
                       int tileHeight) {
        this.targetFilename = targetFilename;
        this.inputDirectory = inputDirectory;
        this.outputFilename = outputFilename;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }

    public void create() throws IOException {
        BufferedImage source = ImageIO.read(new File(targetFilename));
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
                RGBA averageColor = ColorCalculator.averageColor(tile);
                LOG.trace("tile average color r={}, g={}, b={}",
                        (int) averageColor.getR(), (int) averageColor.getG(), (int) averageColor.getB());

                // find an image that matches the average color best
                final String shortestDistanceTileKey = findShortestDistanceTileKey(averageColor);

                // write the selected tile into the target image
                if (shortestDistanceTileKey != null) {
                    final BufferedImage selectedTile = tileMap.get(shortestDistanceTileKey).getImage();
                    writeTile(selectedTile, target, x, y);
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

        ImageIO.write(target, OUTPUT_FILE_FORMAT, new File(outputFilename));
    }

    private void writeTile(BufferedImage selectedTile, BufferedImage target, int x, int y) {
        final WritableRaster writableRaster = target.getWritableTile(x, y);
        double array[] = new double[4];
        for (int i = x, k = 0; i < x + tileWidth; i++, k++) {
            for (int j = y, l = 0; j < y + tileHeight; j++, l++) {
                selectedTile.getRaster().getPixel(k, l, array);
                writableRaster.setPixel(i, j, array);
            }
        }
    }

    private String findShortestDistanceTileKey(RGBA color) {
        double shortestDistance = Double.MAX_VALUE;
        String shortestDistanceTileKey = null;
        Color needle = color.asColor();
        for (Map.Entry<String, Tile> entry : tileMap.entrySet()) {
            double distance = colorDistance(needle, entry.getValue().getAverageColor());
            if (distance < shortestDistance) {
                shortestDistance = distance;
                shortestDistanceTileKey = entry.getKey();
            }
        }
        return shortestDistanceTileKey;
    }

    private void processInputDirectory() throws IOException {
        final File root = new File(inputDirectory);
        if (root.exists() && root.canRead()) {
            String[] filenames = root.list();
            if (filenames != null) {
                for (String filename : filenames) {
                    final File file = new File(root.getAbsolutePath() + "/" + filename);
                    if (!file.isDirectory()) {
                        if (FilenameUtils.isExtension(filename, SUPPORTED_INPUT_FILE_FORMATS)) {
                            BufferedImage image = ImageIO.read(file);
                            BufferedImage scaledImage = ImageScaler.scale(image, tileWidth, tileHeight);
                            RGBA rgba = ColorCalculator.averageColor(scaledImage);
                            tileMap.put(filename, new Tile(scaledImage, rgba.asColor()));
                        } else {
                            LOG.info("Ignore '{}' as input file: unsupported file extension", filename);
                        }
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
