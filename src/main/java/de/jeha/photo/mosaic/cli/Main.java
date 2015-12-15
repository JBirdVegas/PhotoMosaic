package de.jeha.photo.mosaic.cli;

import de.jeha.photo.mosaic.PhotoMosaic;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.Locale;

/**
 * @author jenshadlich@googlemail.com
 */
public class Main {

    private static final int DEFAULT_TILE_WIDTH = 40;
    private static final int DEFAULT_TILE_HEIGHT = 30;

    @Option(name = "-d", usage = "directory of the image collection to use", required = true)
    private String inputDirectory;

    @Option(name = "-o", usage = "name of output file")
    private String outputFilename = "out.png";

    // TODO: hide this option until it's properly implemented
    @Option(name = "-t", usage = "number of threads", hidden = true)
    private Integer threads = 1;

    @Option(name = "-w", usage = "tile width")
    private int tileWidth = DEFAULT_TILE_WIDTH;

    @Option(name = "-h", usage = "tile height")
    private int tileHeight = DEFAULT_TILE_HEIGHT;

    @Argument(required = true)
    private String targetImageFilename;

    public static void main(String... args) throws IOException {
        System.setProperty("java.awt.headless", "true");
        Locale.setDefault(Locale.ENGLISH);

        new Main().run(args);
    }

    private void run(String... args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java -jar photo-mosaic.jar [options...] <filename of target image>");
            parser.printUsage(System.err);
            System.err.println();
            return;
        }

        new PhotoMosaic(
                targetImageFilename,
                inputDirectory,
                outputFilename,
                tileWidth,
                tileHeight
        ).create();

        System.out.println("File '" + outputFilename + "' created successfully.");
    }

}
