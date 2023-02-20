package com.chessanalysis.chessparser;

import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.pgn.PgnIterator;
import com.github.bhlangonijr.chesslib.util.LargeFile;
import com.google.gson.Gson;
import com.chessanalysis.statrecorder.GameData;
import com.chessanalysis.statrecorder.PlayerData;
import com.chessanalysis.statrecorder.StatRecorder;
import com.chessanalysis.util.ParseConfig;
import com.chessanalysis.util.RuntimeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class Main {

    private StatRecorder statRecorder;
    Logger logger = LoggerFactory.getLogger(Main.class);

    public Main() {
        statRecorder = new StatRecorder();
    }


    private void parseInParallelBatches(PgnIterator games, ParseConfig config) {
        ThreadedPgnParser threadedPgnParser = new ThreadedPgnParser(games, config);
        threadedPgnParser.readNextBatches();
        int batchSize = config.getBatchSize();
        List<List<Game>> batches;
        do {
            batches = threadedPgnParser.getNextBatches();
            threadedPgnParser.readNextBatches();
            batches.stream()
                    .parallel()
                    .forEach(gameList -> statRecorder.parseBatch(gameList, config.isFindCheckmates()));
        } while (batches.get(batches.size()-1).size() == batchSize);
    }

    private void parseFile(InputStream stream, ParseConfig config) {
        LargeFile file = new LargeFile(stream);
        PgnIterator games = new PgnIterator(file);

        long startTime = System.nanoTime();
        if (config.isParallel()) {
            parseInParallelBatches(games, config);
        } else {
            int i = 0;
            GameData cache = new GameData();
            Map<String, PlayerData> playerDataCache = new HashMap<>();
            for (Game game : games) {
                statRecorder.parseGame(game, config.isFindCheckmates(), cache, playerDataCache);
                i++;
                if (i % 10000 == 0) {
                    logger.info("{}", i);
                }
            }
            statRecorder.getGameData().merge(cache);
            for (Map.Entry<String, PlayerData> entry : playerDataCache.entrySet()) {
                String key = entry.getKey();
                PlayerData player = entry.getValue();
                statRecorder.getPlayerData().merge(key, player, PlayerData::merge);
            }
        }
        long endTime = System.nanoTime();
        int n = statRecorder.getGameData().getTotalGames();
        double gamesPerSecond = 1e9 * n / (double) (endTime - startTime);
        logger.info("new batch: {}", statRecorder.getGameData().getTotalGames());
        logger.info("Games per second: {}", gamesPerSecond);
    }

    protected void parseAsFile(ParseConfig config) throws FileNotFoundException {
        File file = new File(config.getPgnFilename());
        statRecorder = new StatRecorder();
        InputStream stream = new FileInputStream(file);
        parseFile(stream, config);

        String jsonFilename = config.getJsonFilename();
        logger.info("Writing to {}", jsonFilename);
        Gson gson = new Gson();
        String json = gson.toJson(statRecorder);
        try (PrintWriter out = new PrintWriter(jsonFilename)) {
            out.println(json);
        }
        logger.info("DONE");
    }

    protected void parseCompressedFile(ParseConfig config) {
        String compressedFilename = config.getCompressedFilename();
        String pgnFilename = config.getPgnFilename();

        try {
            RuntimeHandler.decompressFile(compressedFilename);
            parseAsFile(config);
            RuntimeHandler.deleteFile(pgnFilename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseSingleFile() {

        ParseConfig config;
        try {
            config = ParseConfig.fromConfig();
            logger.info("found config file: {}", config);
        } catch (FileNotFoundException e) {
            logger.info("could not find file");
            return;
        }
        try {
            parseAsFile(config);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void readFromQueue() {
        QueueHandler handler = new QueueHandler(this);
        try {
            handler.createConnection();
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        if (args.length > 0 && args[0].equals("-s")) {
            main.parseSingleFile();
        } else {
            main.readFromQueue();
        }
    }
}

