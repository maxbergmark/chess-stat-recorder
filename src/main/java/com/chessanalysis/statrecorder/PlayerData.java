package com.chessanalysis.statrecorder;

import java.util.HashMap;
import java.util.Map;

public class PlayerData {

    private final String name;
    private int acceptedEnPassant;
    private int declinedEnPassant;
    private int minElo;
    private int maxElo;
    private int enPassantMates;
    private int totalGames;
    private int missedCheckmatePossibilities;
    private int missedCheckmateGames;
    private int missedCheckmates;
    private boolean hasMissedMate;
    private final HashMap<String, FirstMove> moves;

    public PlayerData(String name) {
        this.name = name;
        this.minElo = Integer.MAX_VALUE;
        this.maxElo = Integer.MIN_VALUE;
        this.acceptedEnPassant = 0;
        this.declinedEnPassant = 0;
        this.enPassantMates = 0;
        this.totalGames = 0;
        this.missedCheckmates = 0;
        this.moves = new HashMap<>();
    }

    public void incrementAcceptedEnPassant() {
        acceptedEnPassant++;
    }

    public void incrementDeclinedEnPassant() {
        declinedEnPassant++;
    }

    public void incrementEnPassantMates() {
        enPassantMates++;
    }

    public int getMissedCheckmates() {
        return missedCheckmates;
    }

    public void incrementMissedCheckmatePossibilities() {
        missedCheckmatePossibilities++;
    }
    public void incrementMissedCheckmates() {
        missedCheckmates++;
    }
    public void incrementMissedCheckmateGames() {
        missedCheckmateGames++;
    }
    public void setHasMissedMate(boolean value) {
        hasMissedMate = value;
    }

    public void incrementTotalGames() {
        totalGames++;
    }

    public int getAcceptedEnPassant() {
        return acceptedEnPassant;
    }

    public int getDeclinedEnPassant() {
        return declinedEnPassant;
    }

    public int getEnPassantMates() {
        return enPassantMates;
    }

    public int getTotalGames() {
        return totalGames;
    }

    public int getMissedCheckmatePossibilities() {
        return missedCheckmatePossibilities;
    }

    public int getMissedCheckmateGames() {
        return missedCheckmateGames;
    }

    public boolean isHasMissedMate() {
        return hasMissedMate;
    }

    public void addElo(int elo) {
        this.maxElo = Math.max(this.maxElo, elo);
        this.minElo = Math.min(this.minElo, elo);
    }

    public Map<String, FirstMove> getMoves() {
        return moves;
    }

    public PlayerData merge(PlayerData other) {
        this.acceptedEnPassant += other.getAcceptedEnPassant();
        this.declinedEnPassant += other.getDeclinedEnPassant();
        this.totalGames += other.getTotalGames();
        this.enPassantMates += other.getEnPassantMates();
        this.missedCheckmates += other.getMissedCheckmates();
        for (Map.Entry<String, FirstMove> entry : other.getMoves().entrySet()) {
            String key = entry.getKey();
            FirstMove move = entry.getValue();
            moves.merge(key, move, FirstMove::merge);
        }
        return this;
    }

    public String toString() {
        return String.format("(%s, %d, %d, %d, %d, %d)",
                name, acceptedEnPassant, declinedEnPassant,
                enPassantMates, totalGames, missedCheckmates);
    }
}
