package com.chessanalysis.util;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ParseConfig {

    private String compressedFilename;
    private boolean findCheckmates = true;
    private boolean parallel = true;
    private int batchSize;
    private int numBatches;

    public String getCompressedFilename() {
        return compressedFilename;
    }

    public void setCompressedFilename(String compressedFilename) {
        this.compressedFilename = compressedFilename;
    }

    public String getPgnFilename() {
        return compressedFilename.replace(".bz2", "");
    }

    public String getJsonFilename() {
        return compressedFilename.replace(".bz2", ".json");
    }

    public boolean isFindCheckmates() {
        return findCheckmates;
    }

    public void setFindCheckmates(boolean findCheckmates) {
        this.findCheckmates = findCheckmates;
    }

    public boolean isParallel() {
        return parallel;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getNumBatches() {
        return numBatches;
    }

    public void setNumBatches(int numBatches) {
        this.numBatches = numBatches;
    }

    public static ParseConfig fromConfig() throws FileNotFoundException {
        Constructor constructor = new Constructor(ParseConfig.class);
        Yaml yaml = new Yaml(constructor);
        if (true) {
            InputStream io = ParseConfig.class.getClassLoader()
                    .getResourceAsStream("config.yaml");
            return yaml.load(io);
        }
        Path configPath = Paths.get(".\\src\\resources\\config.yaml");
        return yaml.load(new FileInputStream(configPath.toFile()));
    }

    public String toString() {
        return String.format("(compressedFilename: %s, batchSize: %d, numBatches: %d, findCheckmates: %b, parallel: %b)",
                compressedFilename, batchSize, numBatches, findCheckmates, parallel);
    }
}
