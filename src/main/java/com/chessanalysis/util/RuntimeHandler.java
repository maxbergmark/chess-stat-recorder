package com.chessanalysis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RuntimeHandler {

    private RuntimeHandler() {
        // empty
    }

    private static Logger logger = LoggerFactory.getLogger(RuntimeHandler.class);

    public static void decompressFile(String compressedFilename) throws IOException, InterruptedException {
        logger.info("Decompressing {}", compressedFilename);
        Process decompressProcess = Runtime.getRuntime()
                .exec(String.format("pbzip2 -dk %s", compressedFilename));
        decompressProcess.waitFor();
        logger.info("DONE!");
    }

    public static void deleteFile(String pgnFilename) throws IOException, InterruptedException {
        logger.info("Deleting {}", pgnFilename);
        Process deleteProcess = Runtime.getRuntime()
                .exec(String.format("rm %s", pgnFilename));
        deleteProcess.waitFor();
        logger.info("DONE");
    }
}
