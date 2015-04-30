package de.jeha.photo.mosaic;

import java.io.IOException;

/**
 * @author jenshadlich@googlemail.com
 */
public class Main {

    private static final int TILE_WIDTH = 40;
    private static final int TILE_HEIGHT = 30;

    public static void main(String... args) throws IOException {
        System.setProperty("java.awt.headless", "true");

        final String targetImageFilename = "src/test/resources/IMG_20150320_142153.jpg";
        //final String targetImageFilename = "src/test/resources/sprd_logo_small.jpg";

        new PhotoMosaic(targetImageFilename, TILE_WIDTH, TILE_HEIGHT).create();
    }

}
