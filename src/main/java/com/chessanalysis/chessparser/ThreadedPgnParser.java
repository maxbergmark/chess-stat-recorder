package com.chessanalysis.chessparser;

import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.pgn.PgnException;
import com.github.bhlangonijr.chesslib.pgn.PgnIterator;
import com.chessanalysis.util.ParseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ThreadedPgnParser {

    private static Logger logger = LoggerFactory.getLogger(ThreadedPgnParser.class);

    private final PgnIterator pgnIterator;
    private final ParseConfig parseConfig;
    private final Iterator<Game> gameIterator;
    private volatile boolean ready;
    private List<List<Game>> batches;

    public ThreadedPgnParser(PgnIterator iterator, ParseConfig config) {
        pgnIterator = iterator;
        parseConfig = config;
        gameIterator = pgnIterator.iterator();
        ready = false;
    }

    private Game getNextGame() {
        if (!gameIterator.hasNext()) {
            return null;
        }
        return gameIterator.next();
    }

    private List<Game> readBatch(int size) {
        List<Game> gameList = new ArrayList<>(size);
        do {
            try {
                Game game = getNextGame();
                if (game == null) {
                    break;
                }
                gameList.add(game);
            } catch (PgnException e) {
                logger.error("Error parsing game in {}", parseConfig.getCompressedFilename());
            }
        } while (gameList.size() < size);

        return gameList;
    }

    public void readNextBatches() {
        ready = false;
        new Thread(this::readBatchesThreaded).start();
    }

    private void readBatchesThreaded() {
        batches = new ArrayList<>();
        for (int i = 0; i < parseConfig.getNumBatches(); i++) {
            batches.add(readBatch(parseConfig.getBatchSize()));
        }
        ready = true;
    }

    public boolean isReady() {
        return ready;
    }

    private void waitForNextBatches() {
        while (!ready) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    public List<List<Game>> getNextBatches() {
        waitForNextBatches();
        return batches;
    }

}
