package com.chessanalysis.statrecorder;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.chessanalysis.util.EnPassantDetector;
import com.chessanalysis.util.MoveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StatRecorder {

    Logger logger = LoggerFactory.getLogger(StatRecorder.class);
    private final GameData gameData;
    private final HashMap<String, PlayerData> playerData;

    public StatRecorder() {
        gameData = new GameData();
        playerData = new HashMap<>();
    }

    private boolean moveCausesMate(Board board, Move move) {
        board.doMove(move);
        boolean mate = board.isMated();
        board.undoMove();
        return mate;
    }

    public String cleanMove(String move) {
        String cleanMoveRegex = "(\\?\\?|\\?|\\?!|!\\?|!|!!)";
        return move.replaceAll(cleanMoveRegex, "");
    }

    private void checkEnPassant(Board board, Move move, PlayerData player, SingleGameStats singleGameStats) {
        MoveType moveType = EnPassantDetector.isEnPassant(board, move);
        if (moveType == MoveType.EN_PASSANT_ACCEPTED) {
            singleGameStats.incrementAcceptedEnPassant(1);
            player.incrementAcceptedEnPassant();
            if (moveCausesMate(board, move)) {
                singleGameStats.incrementEnPassantMates(1);
                player.incrementEnPassantMates();
            }
        } else if (moveType == MoveType.EN_PASSANT_DECLINED) {
            singleGameStats.incrementDeclinedEnPassant(1);
            player.incrementDeclinedEnPassant();
        }
    }

    private void checkFirstMove(Game game, Move move, Date utcDateTime,
            PlayerData player, int moveNumber, GameData gameDataCache, int playerElo) {

        String san = cleanMove(move.getSan());
        String gameLink = game.getRound().getEvent().getSite();
        if (!gameDataCache.getMoves().containsKey(san)) {
            FirstMove firstMove = new FirstMove(san, utcDateTime, player, gameLink, moveNumber, playerElo);
            gameDataCache.getMoves().put(san, firstMove);
        } else {
            FirstMove firstMove = gameDataCache.getMoves().get(san);
            firstMove.addOccurrence(utcDateTime, player, gameLink, moveNumber, playerElo);

        }
    }

    public GameData getGameData() {
        return gameData;
    }

    private int checkMissedMates(Board board, Move move, PlayerData player, SingleGameStats singleGameStats) {
        boolean foundMissedMate = false;
        int numMoves = 0;
        for (Move m : board.legalMoves()) {
            numMoves++;
            if (!m.equals(move) && moveCausesMate(board, m)) {
                singleGameStats.incrementMissedCheckmatePossibilities(1);
                player.incrementMissedCheckmatePossibilities();
                foundMissedMate = true;
            }
        }
        if (foundMissedMate) {
            singleGameStats.incrementMissedCheckmates(1);
            singleGameStats.setHasMissedMate(true);
            player.incrementMissedCheckmates();
            player.setHasMissedMate(true);
        }
        return numMoves;
    }

    private Date getGameDate(Game game) {
        DateFormat format = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
        String utcDate = game.getProperty().get("UTCDate");
        String utcTime = game.getProperty().get("UTCTime");
        String utcDateTime = utcDate + "-" + utcTime;
        try {
            return format.parse(utcDateTime);
        } catch (ParseException e) {
            return null;
        }
    }

    private PlayerData getPlayer(String playerName, Map<String, PlayerData> playerDataCache) {
        return playerDataCache.computeIfAbsent(playerName, PlayerData::new);
    }

    public void parseGame(Game game, boolean findCheckmates, GameData gameDataCache, Map<String, PlayerData> playerDataCache) {
        if (game == null) {
            return;
        }
        gameDataCache.incrementTotalGames(1);
        Date gameDate = getGameDate(game);
        MoveList moves = game.getHalfMoves();
        SingleGameStats singleGameStats = new SingleGameStats(game.getRound().getEvent().getSite());

        PlayerData whitePlayer = getPlayer(game.getWhitePlayer().getName(), playerDataCache);
        int whiteElo = game.getWhitePlayer().getElo();
        whitePlayer.incrementTotalGames();
        whitePlayer.addElo(whiteElo);

        PlayerData blackPlayer = getPlayer(game.getBlackPlayer().getName(), playerDataCache);
        int blackElo = game.getWhitePlayer().getElo();
        blackPlayer.incrementTotalGames();
        blackPlayer.addElo(blackElo);

        Board board = new Board();
        int moveNumber = 0;
        for (Move move : moves) {
            PlayerData currentPlayer = moveNumber % 2 == 0 ? whitePlayer : blackPlayer;
            int playerElo = moveNumber % 2 == 0 ? whiteElo : blackElo;
            checkEnPassant(board, move, currentPlayer, singleGameStats);
            checkFirstMove(game, move, gameDate, currentPlayer, moveNumber, gameDataCache, playerElo);
            if (findCheckmates) {
                int numPossibleMoves = checkMissedMates(board, move, currentPlayer, singleGameStats);
                gameDataCache.recordPossibleMoves(moveNumber, numPossibleMoves);
            }
            board.doMove(move);
            moveNumber++;
        }
        gameDataCache.addGame(singleGameStats);
        if (whitePlayer.isHasMissedMate()) {
            whitePlayer.incrementMissedCheckmateGames();
            whitePlayer.setHasMissedMate(false);
        }
        if (blackPlayer.isHasMissedMate()) {
            blackPlayer.incrementMissedCheckmateGames();
            blackPlayer.setHasMissedMate(false);
        }
    }

    private synchronized void saveBatch(GameData gameDataCache, HashMap<String, PlayerData> playerDataCache) {
        gameData.merge(gameDataCache);
        for (Map.Entry<String, PlayerData> entry : playerDataCache.entrySet()) {
            String key = entry.getKey();
            PlayerData player = entry.getValue();
            playerData.merge(key, player, PlayerData::merge);
        }
    }

    public void parseBatch(List<Game> games, boolean findCheckmates) {
        GameData gameDataCache = new GameData();
        HashMap<String, PlayerData> playerDataCache = new HashMap<>();
        for (Game game : games) {
            parseGame(game, findCheckmates, gameDataCache, playerDataCache);
        }
        saveBatch(gameDataCache, playerDataCache);
    }

    public Map<String, PlayerData> getPlayerData() {
        return playerData;
    }

    public void printStats() {
        logger.info("Number of games: {}", gameData.getTotalGames());
        logger.info("Number of distinct moves: {}", gameData.getMoves().size());
        int totalMoves = gameData.getMoves().values().stream()
                .map(FirstMove::getCount)
                .reduce(0, Integer::sum);
        logger.info("Total moves: {}", totalMoves);
        logger.info("Total players: {}", playerData.size());

        for (Map.Entry<String, PlayerData> entry : playerData.entrySet()) {
            PlayerData player = entry.getValue();
            if (player.getMissedCheckmates() > 100) {
                logger.info("{}", player);
            }
        }
        logger.info("Accepted en passant: {}", gameData.getAcceptedEnPassant());
        logger.info("Declined en passant: {}", gameData.getDeclinedEnPassant());
        logger.info("En passant checkmates: {}", gameData.getEnPassantMates());
        logger.info("Missed checkmates: {}", gameData.getMissedCheckmates());
    }
}
