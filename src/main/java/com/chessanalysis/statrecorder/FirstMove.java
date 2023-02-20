package com.chessanalysis.statrecorder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirstMove implements MergeableData<FirstMove> {

    private final String moveSAN;
    private int count;
    private Date firstSeen;
    private PlayerData player;
    private String gameLink;
    private final HashMap<Integer, Integer> moveNumberHistogram;
    private final HashMap<Integer, Integer> eloHistogram;

    public FirstMove(String moveSAN, Date firstSeen, PlayerData player, String gameLink, int moveNumber, int playerElo) {
        this.moveSAN = moveSAN;
        this.count = 1;
        this.firstSeen = firstSeen;
        this.player = player;
        this.gameLink = gameLink;
        moveNumberHistogram = new HashMap<>();
        eloHistogram = new HashMap<>();
        addMoveNumber(moveNumber);
        addMoveElo(playerElo);
    }

    public String getMoveSAN() {
        return moveSAN;
    }

    public Date getFirstSeen() {
        return firstSeen;
    }

    public PlayerData getPlayer() {
        return player;
    }

    public String getGameLink() {
        return gameLink;
    }

    public Map<Integer, Integer> getMoveNumberHistogram() {
        return moveNumberHistogram;
    }

    public Map<Integer, Integer> getEloHistogram() {
        return eloHistogram;
    }

    public String toString() {
        DateFormat format = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
        String date = format.format(firstSeen);
        return moveSAN + " " + count + " " + date + " " + player;
    }

    public int getCount() {
        return count;
    }

    private void addMoveNumber(int moveNumber) {
        moveNumberHistogram.merge(moveNumber, 1, Integer::sum);
    }

    private void addMoveElo(int playerElo) {
        int roundedElo = (int) (Math.round(playerElo / 10.0) * 10);
        eloHistogram.merge(roundedElo, 1, Integer::sum);
    }

    public void addOccurrence(Date date, PlayerData player, String gameLink, int moveNumber, int playerElo) {
        count++;
        addMoveNumber(moveNumber);
        addMoveElo(playerElo);
        if (date == null) {
            return;
        }
        if (date.before(firstSeen)) {
            firstSeen = date;
            this.player = player;
            this.gameLink = gameLink;
        }
    }

    @Override
    public FirstMove merge(FirstMove other) {
        this.count += other.getCount();
        if (other.getFirstSeen().before(this.firstSeen)) {
            this.firstSeen = other.getFirstSeen();
            this.player = other.getPlayer();
            this.gameLink = other.getGameLink();
        }
        for (Map.Entry<Integer, Integer> entry : other.getMoveNumberHistogram().entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            moveNumberHistogram.merge(key, value, Integer::sum);
        }
        for (Map.Entry<Integer, Integer> entry : other.getEloHistogram().entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            eloHistogram.merge(key, value, Integer::sum);
        }
        return this;
    }
}
