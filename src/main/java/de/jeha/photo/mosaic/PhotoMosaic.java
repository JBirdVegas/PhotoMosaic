package de.jeha.photo.mosaic;

import de.jeha.photo.mosaic.concurrents.MosaicThreadPoolHandler;
import de.jeha.photo.mosaic.core.*;
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
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author jenshadlich@googlemail.com
 */
public class PhotoMosaic {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoMosaic.class);

    private static final String FILE_FORMAT_PNG = "png";
    private static final String FILE_FORMAT_JPG = "jpg";
    private static final String FILE_FORMAT_JPEG = "jpeg";
    private static final String OUTPUT_FILE_FORMAT = FILE_FORMAT_PNG;
    private static final Set<String> SUPPORTED_INPUT_FILE_FORMATS;

    static {
        SUPPORTED_INPUT_FILE_FORMATS = new HashSet<>();
        SUPPORTED_INPUT_FILE_FORMATS.add(FILE_FORMAT_PNG);
        SUPPORTED_INPUT_FILE_FORMATS.add(FILE_FORMAT_JPG);
        SUPPORTED_INPUT_FILE_FORMATS.add(FILE_FORMAT_JPEG);
    }

    private final String[] targetFilePaths;
    private final String inputDirectory;
    private final File outputFile;
    private final boolean recursive = false; // TODO
    private final int tileWidth;
    private final int tileHeight;
    private final int threads;
    private final double[] scales;

    private volatile Map<String, Tile> tileMap = new HashMap<>();

    @Deprecated
    public PhotoMosaic(String targetFilePaths, String inputDirectory, String outputFile, int tileWidth,
                       int tileHeight) {
        this(new String[]{targetFilePaths}, inputDirectory, new File(outputFile), tileWidth, tileHeight, 1, new double[]{1D});
    }

    public PhotoMosaic(String[] targetFilePaths, String inputDirectory,
                       File outputFile, int tileWidth, int tileHeight,
                       int threads, double[] scaleArray) {
        this.targetFilePaths = targetFilePaths;
        this.inputDirectory = inputDirectory;
        this.outputFile = outputFile;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.threads = threads;
        this.scales = scaleArray;
    }

    public void create() throws IOException {
        long start = System.currentTimeMillis();
        LOG.info("Target image path: {}", Arrays.toString(targetFilePaths));

        processInputDirectory(threads);

        MosaicThreadPoolHandler threadPoolHandler = new MosaicThreadPoolHandler(threads);
        for (double scale : scales) {
            if (scale != 0D) {
                for (String targetImagePath : targetFilePaths) {
                    CreateTargetImageJob targetImage = new CreateTargetImageJob(new File(targetImagePath), tileMap, tileWidth, tileHeight, scale);
                    threadPoolHandler.addJob(targetImage);
                }
            }
        }
        threadPoolHandler.shutdownWhenDone();

        LOG.info("Total execution time: {}", System.currentTimeMillis() - start);
    }

    private static class CreateTargetImageJob implements Runnable {
        private final Map<String, Tile> tileMap;
        private final int tileWidth;
        private final int tileHeight;
        private final double scale;
        private final File targetImageFile;

        CreateTargetImageJob(File targetImageFile, Map<String, Tile> tileMap, int tileWidth, int tileHeight, double scale) {
            this.targetImageFile = targetImageFile;
            this.tileMap = new HashMap<>(tileMap);
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            this.scale = scale;
        }

        @Override
        public void run() {
            try {
                BufferedImage source = ImageIO.read(targetImageFile);
                LOG.info("source image h={}, w={}, scale={}, postfix={}", source.getHeight(), source.getWidth(),
                        scale, targetImageFile.getAbsolutePath());
                createTargetImage(source,
                        String.format("%s-scaled-%s", FilenameUtils.getBaseName(targetImageFile.getName()), scale),
                        scale);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // scale should be 1.0+ to enlarge or <1.0 to shrink
        private void createTargetImage(BufferedImage source, String outputFileNamePostfix, double sourceScaleFactor)
                throws IOException {
            long start = System.currentTimeMillis();
            final int correctedWidth = (int) (source.getWidth() * sourceScaleFactor / tileWidth) * tileWidth;
            final int correctedHeight = (int) (source.getHeight() * sourceScaleFactor / tileHeight) * tileHeight;
            LOG.info("target image h={}, w={}, postfix={} scale={}", correctedHeight, correctedWidth,
                    outputFileNamePostfix, sourceScaleFactor);
            if (correctedHeight == 0 || correctedWidth == 0) {
                LOG.error("Cannot target image with 0 width or height; postfix: {} scale: {}",
                        outputFileNamePostfix, sourceScaleFactor);
                return;
            }

            BufferedImage resizedSource = new BufferedImage(correctedWidth, correctedHeight, BufferedImage.TYPE_INT_RGB);
            Graphics g = resizedSource.createGraphics();
            g.drawImage(source, 0, 0, correctedWidth, correctedHeight, null);
            g.dispose();

            BufferedImage target = copyImage(resizedSource).getSubimage(0, 0, correctedWidth, correctedHeight);

            for (int x = 0; x < resizedSource.getWidth(); x += tileWidth) {
                for (int y = 0; y < resizedSource.getHeight(); y += tileHeight) {
                    LOG.trace("tile x={}, y={}", x, y);
                    BufferedImage tile = resizedSource.getSubimage(x, y, tileWidth, tileHeight);
                    RGBA averageColor = ColorCalculator.averageColor(tile);
                    LOG.trace("tile average color r={}, g={}, b={}",
                            (int) averageColor.getR(), (int) averageColor.getG(), (int) averageColor.getB());

                    // find an image that matches the average color best
                    final String shortestDistanceTileKey = findShortestDistanceTileKey(averageColor);

                    // write the selected tile into the target image
                    if (shortestDistanceTileKey != null) {
                        final BufferedImage selectedTile = tileMap.get(shortestDistanceTileKey).getImage();
                        LOG.info(String.format("Adding tile to image at coordinates {%d, %d}", x, y));
                        writeTile(selectedTile, target, x, y);
                    }
                }
            }

            File output = addPostfixToFile(targetImageFile, outputFileNamePostfix);
            LOG.info(String.format("Processing took: %d seconds\nFile output: %s",
                    (System.currentTimeMillis() - start) / 1000, output.getAbsolutePath()));
            ImageIO.write(target, OUTPUT_FILE_FORMAT, output);
        }

        private File addPostfixToFile(File file, String postfix) {
            String newName = file.getAbsolutePath();
            int lastIndexOf = newName.lastIndexOf('.');
            newName = String.format("%s-%s.%s", newName.substring(0, lastIndexOf), postfix, OUTPUT_FILE_FORMAT);
            return new File(newName);
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
                double distance = ColorDistance.calculate(needle, entry.getValue().getAverageColor());
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    shortestDistanceTileKey = entry.getKey();
                }
            }
            return shortestDistanceTileKey;
        }

        private BufferedImage copyImage(BufferedImage image) {
            final ColorModel cm = image.getColorModel();
            WritableRaster raster = image.copyData(null);
            return new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
        }
    }

    private void processInputDirectory(int numberOfThreads) throws IOException {
        File root = new File(inputDirectory);
        if (root.exists() && root.canRead()) {
            String[] filenames = root.list();
            if (filenames != null) {

                MosaicThreadPoolHandler threadPoolHandler = new MosaicThreadPoolHandler(numberOfThreads);
                int[] index = {0};
                for (String filename : filenames) {
                    File file = new File(root.getAbsolutePath() + "/" + filename);
                    if (!file.isDirectory()) {
                        if (FilenameUtils.isExtension(filename, SUPPORTED_INPUT_FILE_FORMATS)) {
                            threadPoolHandler.addJob(new ProcessImageJob(this, file, index[0]++, filenames.length));
                        } else {
                            LOG.info("Ignore '{}' as input file: unsupported file extension", filename);
                        }
                    }
                }

                threadPoolHandler.shutdownWhenDone();
                LOG.info("added {} images as tiles", tileMap.size());
            }
        } else {
            LOG.error("can't access directory '{}'", inputDirectory);
        }
    }

    private static class ProcessImageJob implements Runnable {
        private WeakReference<PhotoMosaic> photoMosaicWeakReference;
        private final File imageFile;
        private final int jobsCount;
        private final int position;

        ProcessImageJob(PhotoMosaic mosaic, File file, int currentPosition, int totalJobs) {
            photoMosaicWeakReference = new WeakReference<>(mosaic);
            imageFile = file;
            position = currentPosition;
            jobsCount = totalJobs;
        }

        @Override
        public void run() {
            try {
                PhotoMosaic photoMosaic = photoMosaicWeakReference.get();
                if (photoMosaic == null) {
                    LOG.error("Parent project was collected before job could run... abandoning jobs");
                    return;
                }
                long startFile = System.currentTimeMillis();
                BufferedImage image = ImageIO.read(imageFile);
                BufferedImage scaledImage = ImageScaler.scale(image, photoMosaic.tileWidth, photoMosaic.tileHeight);
                RGBA rgba = ColorCalculator.averageColor(scaledImage);
                photoMosaic.tileMap.put(imageFile.getAbsolutePath(), new Tile(scaledImage, rgba.asColor()));
                LOG.info(String.format("[%d/%d] Image took %d ms to analyse: %s",
                        position,
                        jobsCount,
                        System.currentTimeMillis() - startFile,
                        imageFile.getAbsolutePath()));
            } catch (IOException e) {
                LOG.error("Failed to process image!", e);
            }
        }
    }
}